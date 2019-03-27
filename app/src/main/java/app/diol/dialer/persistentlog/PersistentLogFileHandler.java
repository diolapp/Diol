/*
 *  Copyright (C) 2019  The Diol App Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.diol.dialer.persistentlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.os.UserManagerCompat;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.diol.dialer.common.LogUtil;

/**
 * Handles serialization of byte arrays and read/write them to multiple rotating files. If a logText
 * file exceeds {@code fileSizeLimit} after a write, a new file will be used. if the total number of
 * files exceeds {@code fileCountLimit} the oldest ones will be deleted. The logs are stored in the
 * cache but the file index is stored in the data (clearing data will also clear the cache). The
 * logs will be stored under /cache_dir/persistent_log/{@code subfolder}, so multiple independent
 * logs can be created.
 *
 * <p>This class is NOT thread safe. All methods expect the constructor must be called on the same
 * worker thread.
 */
final class PersistentLogFileHandler {

    private static final String LOG_DIRECTORY = "persistent_log";
    private static final String NEXT_FILE_INDEX_PREFIX = "persistent_long_next_file_index_";

    private static final byte[] ENTRY_PREFIX = {'P'};
    private static final byte[] ENTRY_POSTFIX = {'L'};
    private final String subfolder;
    private final int fileSizeLimit;
    private final int fileCountLimit;
    private File logDirectory;
    private SharedPreferences sharedPreferences;
    private File outputFile;
    private Context context;
    @MainThread
    PersistentLogFileHandler(String subfolder, int fileSizeLimit, int fileCountLimit) {
        this.subfolder = subfolder;
        this.fileSizeLimit = fileSizeLimit;
        this.fileCountLimit = fileCountLimit;
    }

    private static int getTotalSize(File[] files) {
        int sum = 0;
        for (File file : files) {
            sum += (int) file.length();
        }
        return sum;
    }

    @NonNull
    @WorkerThread
    private static byte[] readAllBytes(File file) throws IOException {
        byte[] result = new byte[(int) file.length()];
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            randomAccessFile.readFully(result);
        }
        return result;
    }

    /**
     * Must be called right after the logger thread is created.
     */
    @WorkerThread
    void initialize(Context context) {
        this.context = context;
        logDirectory = new File(new File(context.getCacheDir(), LOG_DIRECTORY), subfolder);
        initializeSharedPreference(context);
    }

    @WorkerThread
    private boolean initializeSharedPreference(Context context) {
        if (sharedPreferences == null && UserManagerCompat.isUserUnlocked(context)) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return true;
        }
        return sharedPreferences != null;
    }

    /**
     * Write the list of byte arrays to the current log file, prefixing each entry with its' length. A
     * new file will only be selected when the batch is completed, so the resulting file might be
     * larger then {@code fileSizeLimit}
     */
    @WorkerThread
    void writeLogs(List<byte[]> logs) throws IOException {
        if (outputFile == null) {
            selectNextFileToWrite();
        }
        outputFile.createNewFile();
        try (DataOutputStream outputStream =
                     new DataOutputStream(new FileOutputStream(outputFile, true))) {
            for (byte[] log : logs) {
                outputStream.write(ENTRY_PREFIX);
                outputStream.writeInt(log.length);
                outputStream.write(log);
                outputStream.write(ENTRY_POSTFIX);
            }
            outputStream.close();
            if (outputFile.length() > fileSizeLimit) {
                selectNextFileToWrite();
            }
        }
    }

    void writeRawLogsForTest(byte[] data) throws IOException {
        if (outputFile == null) {
            selectNextFileToWrite();
        }
        outputFile.createNewFile();
        try (DataOutputStream outputStream =
                     new DataOutputStream(new FileOutputStream(outputFile, true))) {
            outputStream.write(data);
            outputStream.close();
            if (outputFile.length() > fileSizeLimit) {
                selectNextFileToWrite();
            }
        }
    }

    /**
     * Concatenate all log files in chronicle order and return a byte array.
     */
    @WorkerThread
    @NonNull
    private byte[] readBlob() throws IOException {
        File[] files = getLogFiles();

        ByteBuffer byteBuffer = ByteBuffer.allocate(getTotalSize(files));
        for (File file : files) {
            byteBuffer.put(readAllBytes(file));
        }
        return byteBuffer.array();
    }

    /**
     * Parses the content of all files back to individual byte arrays.
     */
    @WorkerThread
    @NonNull
    List<byte[]> getLogs() throws IOException {
        byte[] blob = readBlob();
        List<byte[]> logs = new ArrayList<>();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(blob))) {
            byte[] log = readLog(input);
            while (log != null) {
                logs.add(log);
                log = readLog(input);
            }
        } catch (LogCorruptionException e) {
            LogUtil.e("PersistentLogFileHandler.getLogs", "logs corrupted, deleting", e);
            deleteLogs();
            return new ArrayList<>();
        }
        return logs;
    }

    private void deleteLogs() throws IOException {
        for (File file : getLogFiles()) {
            file.delete();
        }
        selectNextFileToWrite();
    }

    @WorkerThread
    private void selectNextFileToWrite() throws IOException {
        File[] files = getLogFiles();

        if (files.length == 0 || files[files.length - 1].length() > fileSizeLimit) {
            if (files.length >= fileCountLimit) {
                for (int i = 0; i <= files.length - fileCountLimit; i++) {
                    files[i].delete();
                }
            }
            outputFile = new File(logDirectory, String.valueOf(getAndIncrementNextFileIndex()));
        } else {
            outputFile = files[files.length - 1];
        }
    }

    @NonNull
    @WorkerThread
    private File[] getLogFiles() {
        logDirectory.mkdirs();
        File[] files = logDirectory.listFiles();
        if (files == null) {
            files = new File[0];
        }
        Arrays.sort(
                files,
                (File lhs, File rhs) ->
                        Long.compare(Long.valueOf(lhs.getName()), Long.valueOf(rhs.getName())));
        return files;
    }

    @Nullable
    @WorkerThread
    private byte[] readLog(DataInputStream inputStream) throws IOException, LogCorruptionException {
        try {
            byte[] prefix = new byte[ENTRY_PREFIX.length];
            if (inputStream.read(prefix) == -1) {
                // EOF
                return null;
            }
            if (!Arrays.equals(prefix, ENTRY_PREFIX)) {
                throw new LogCorruptionException("entry prefix mismatch");
            }
            int dataLength = inputStream.readInt();
            if (dataLength > fileSizeLimit) {
                throw new LogCorruptionException("data length over max size");
            }
            byte[] data = new byte[dataLength];
            inputStream.read(data);

            byte[] postfix = new byte[ENTRY_POSTFIX.length];
            inputStream.read(postfix);
            if (!Arrays.equals(postfix, ENTRY_POSTFIX)) {
                throw new LogCorruptionException("entry postfix mismatch");
            }
            return data;
        } catch (EOFException e) {
            return null;
        }
    }

    @WorkerThread
    private int getAndIncrementNextFileIndex() throws IOException {
        if (!initializeSharedPreference(context)) {
            throw new IOException("Shared preference is not available");
        }

        int index = sharedPreferences.getInt(getNextFileKey(), 0);
        sharedPreferences.edit().putInt(getNextFileKey(), index + 1).commit();
        return index;
    }

    @AnyThread
    private String getNextFileKey() {
        return NEXT_FILE_INDEX_PREFIX + subfolder;
    }

    private static class LogCorruptionException extends Exception {

        public LogCorruptionException(String message) {
            super(message);
        }
    }
}
