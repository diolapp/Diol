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

package app.diol.dialer.rtt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.WorkerThread;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.InvalidProtocolBufferException;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.common.database.Selection;
import app.diol.dialer.rtt.RttTranscriptContract.RttTranscriptColumn;

/**
 * Util class to save and load RTT transcript.
 */
public final class RttTranscriptUtil {

    public static ListenableFuture<ImmutableSet<String>> getAvailableRttTranscriptIds(
            Context context, ImmutableSet<String> transcriptIds) {
        return DialerExecutorComponent.get(context)
                .backgroundExecutor()
                .submit(() -> checkRttTranscriptAvailability(context, transcriptIds));
    }

    @WorkerThread
    private static ImmutableSet<String> checkRttTranscriptAvailability(
            Context context, ImmutableSet<String> transcriptIds) {
        Assert.isWorkerThread();
        RttTranscriptDatabaseHelper databaseHelper = new RttTranscriptDatabaseHelper(context);
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        Selection selection =
                Selection.builder()
                        .and(Selection.column(RttTranscriptColumn.TRANSCRIPT_ID).in(transcriptIds))
                        .build();

        try (Cursor cursor =
                     databaseHelper
                             .getReadableDatabase()
                             .query(
                                     RttTranscriptDatabaseHelper.TABLE,
                                     new String[]{RttTranscriptColumn.TRANSCRIPT_ID},
                                     selection.getSelection(),
                                     selection.getSelectionArgs(),
                                     null,
                                     null,
                                     null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    builder.add(cursor.getString(0));
                }
            }
        }
        databaseHelper.close();
        return builder.build();
    }

    static ListenableFuture<RttTranscript> loadRttTranscript(Context context, String transcriptId) {
        return DialerExecutorComponent.get(context)
                .lightweightExecutor()
                .submit(() -> getRttTranscript(context, transcriptId));
    }

    @WorkerThread
    private static RttTranscript getRttTranscript(Context context, String transcriptId) {
        Assert.isWorkerThread();
        RttTranscriptDatabaseHelper databaseHelper = new RttTranscriptDatabaseHelper(context);
        try (Cursor cursor =
                     databaseHelper
                             .getReadableDatabase()
                             .query(
                                     RttTranscriptDatabaseHelper.TABLE,
                                     new String[]{RttTranscriptColumn.TRANSCRIPT_DATA},
                                     RttTranscriptColumn.TRANSCRIPT_ID + " = ?",
                                     new String[]{transcriptId},
                                     null,
                                     null,
                                     null)) {
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    return RttTranscript.parseFrom(cursor.getBlob(0));
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException("Parse failed for RTT transcript", e);
                }
            } else {
                return null;
            }
        } finally {
            databaseHelper.close();
        }
    }

    public static ListenableFuture<Void> saveRttTranscript(
            Context context, RttTranscript rttTranscript) {
        return DialerExecutorComponent.get(context)
                .backgroundExecutor()
                .submit(
                        () -> {
                            save(context, rttTranscript);
                            return null;
                        });
    }

    @WorkerThread
    private static void save(Context context, RttTranscript rttTranscript) {
        Assert.isWorkerThread();
        RttTranscriptDatabaseHelper databaseHelper = new RttTranscriptDatabaseHelper(context);
        ContentValues value = new ContentValues();
        value.put(RttTranscriptColumn.TRANSCRIPT_ID, rttTranscript.getId());
        value.put(RttTranscriptColumn.TRANSCRIPT_DATA, rttTranscript.toByteArray());
        long id =
                databaseHelper.getWritableDatabase().insert(RttTranscriptDatabaseHelper.TABLE, null, value);
        databaseHelper.close();
        if (id < 0) {
            throw new RuntimeException("Failed to save RTT transcript");
        }
    }
}
