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

package app.diol.dialer.calllog.database;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import app.diol.dialer.calllog.database.contract.AnnotatedCallLogContract;
import app.diol.dialer.calllog.database.contract.AnnotatedCallLogContract.AnnotatedCallLog;
import app.diol.dialer.calllog.datasources.CallLogMutations;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;

/**
 * Applies {@link CallLogMutations} to the annotated call log.
 */
public class MutationApplier {

    private final ListeningExecutorService backgroundExecutorService;

    @Inject
    public MutationApplier(@BackgroundExecutor ListeningExecutorService backgroundExecutorService) {
        this.backgroundExecutorService = backgroundExecutorService;
    }

    /**
     * Applies the provided {@link CallLogMutations} to the annotated call log.
     */
    public ListenableFuture<Void> applyToDatabase(CallLogMutations mutations, Context appContext) {
        if (mutations.isEmpty()) {
            return Futures.immediateFuture(null);
        }
        return backgroundExecutorService.submit(
                () -> {
                    applyToDatabaseInternal(mutations, appContext);
                    return null;
                });
    }

    @WorkerThread
    private void applyToDatabaseInternal(CallLogMutations mutations, Context appContext)
            throws RemoteException, OperationApplicationException {
        Assert.isWorkerThread();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        if (!mutations.getInserts().isEmpty()) {
            LogUtil.i(
                    "MutationApplier.applyToDatabase", "inserting %d rows", mutations.getInserts().size());
            for (Entry<Long, ContentValues> entry : mutations.getInserts().entrySet()) {
                long id = entry.getKey();
                ContentValues contentValues = entry.getValue();
                operations.add(
                        ContentProviderOperation.newInsert(
                                ContentUris.withAppendedId(AnnotatedCallLog.CONTENT_URI, id))
                                .withValues(contentValues)
                                .build());
            }
        }

        if (!mutations.getUpdates().isEmpty()) {
            LogUtil.i(
                    "MutationApplier.applyToDatabase", "updating %d rows", mutations.getUpdates().size());
            for (Entry<Long, ContentValues> entry : mutations.getUpdates().entrySet()) {
                long id = entry.getKey();
                ContentValues contentValues = entry.getValue();
                operations.add(
                        ContentProviderOperation.newUpdate(
                                ContentUris.withAppendedId(AnnotatedCallLog.CONTENT_URI, id))
                                .withValues(contentValues)
                                .build());
            }
        }

        if (!mutations.getDeletes().isEmpty()) {
            LogUtil.i(
                    "MutationApplier.applyToDatabase", "deleting %d rows", mutations.getDeletes().size());

            // Batch the deletes into chunks of 999, the maximum size for SQLite selection args.
            Iterable<List<Long>> batches = Iterables.partition(mutations.getDeletes(), 999);
            for (List<Long> idsInBatch : batches) {
                String[] questionMarks = new String[idsInBatch.size()];
                Arrays.fill(questionMarks, "?");

                String whereClause =
                        (AnnotatedCallLog._ID + " in (") + TextUtils.join(",", questionMarks) + ")";

                String[] whereArgs = new String[idsInBatch.size()];
                int i = 0;
                for (long id : idsInBatch) {
                    whereArgs[i++] = String.valueOf(id);
                }

                operations.add(
                        ContentProviderOperation.newDelete(AnnotatedCallLog.CONTENT_URI)
                                .withSelection(whereClause, whereArgs)
                                .build());
            }
        }

        appContext.getContentResolver().applyBatch(AnnotatedCallLogContract.AUTHORITY, operations);
    }
}
