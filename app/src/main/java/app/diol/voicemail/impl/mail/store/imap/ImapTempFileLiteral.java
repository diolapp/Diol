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
package app.diol.voicemail.impl.mail.store.imap;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import app.diol.voicemail.impl.mail.FixedLengthInputStream;
import app.diol.voicemail.impl.mail.TempDirectory;
import app.diol.voicemail.impl.mail.utils.LogUtils;
import app.diol.voicemail.impl.mail.utils.Utility;

/**
 * Subclass of {@link ImapString} used for literals backed by a temp file.
 */
public class ImapTempFileLiteral extends ImapString {
    /* package for test */ final File file;
    private final String TAG = "ImapTempFileLiteral";
    /**
     * Size is purely for toString()
     */
    private final int size;

    /* package */ ImapTempFileLiteral(FixedLengthInputStream stream) throws IOException {
        size = stream.getLength();
        file = File.createTempFile("imap", ".tmp", TempDirectory.getTempDirectory());

        // Unfortunately, we can't really use deleteOnExit(), because temp filenames are
        // random
        // so it'd simply cause a memory leak.
        // deleteOnExit() simply adds filenames to a static list and the list will never
        // shrink.
        // mFile.deleteOnExit();
        OutputStream out = new FileOutputStream(file);
        IOUtils.copy(stream, out);
        out.close();
    }

    /**
     * Make sure we delete the temp file.
     *
     * <p>
     * We should always be calling {@link ImapResponse#destroy()}, but it's here as
     * a last resort.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }

    @Override
    public InputStream getAsStream() {
        checkNotDestroyed();
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // It's probably possible if we're low on storage and the system clears the
            // cache dir.
            LogUtils.w(TAG, "ImapTempFileLiteral: Temp file not found");

            // Return 0 byte stream as a dummy...
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public String getString() {
        checkNotDestroyed();
        try {
            byte[] bytes = IOUtils.toByteArray(getAsStream());
            // Prevent crash from OOM; we've seen this, but only rarely and not reproducibly
            if (bytes.length > ImapResponseParser.LITERAL_KEEP_IN_MEMORY_THRESHOLD) {
                throw new IOException();
            }
            return Utility.fromAscii(bytes);
        } catch (IOException e) {
            LogUtils.w(TAG, "ImapTempFileLiteral: Error while reading temp file", e);
            return "";
        }
    }

    @Override
    public void destroy() {
        try {
            if (!isDestroyed() && file.exists()) {
                file.delete();
            }
        } catch (RuntimeException re) {
            // Just log and ignore.
            LogUtils.w(TAG, "Failed to remove temp file: " + re.getMessage());
        }
        super.destroy();
    }

    @Override
    public String toString() {
        return String.format("{%d byte literal(file)}", size);
    }

    public boolean tempFileExistsForTest() {
        return file.exists();
    }
}
