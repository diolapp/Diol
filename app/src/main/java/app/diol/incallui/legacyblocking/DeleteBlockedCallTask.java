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

package app.diol.incallui.legacyblocking;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.support.v4.content.ContextCompat;

import java.util.Objects;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.telecom.TelecomUtil;

/**
 * Deletes a blocked call from the call log. This is only used on Android Marshmallow. On later
 * versions of the OS, call blocking is implemented in the system and there's no need to mess with
 * the call log.
 */
public class DeleteBlockedCallTask extends AsyncTask<Void, Void, Long> {

    public static final String IDENTIFIER = "DeleteBlockedCallTask";

    // Try to identify if a call log entry corresponds to a number which was blocked. We match by
    // by comparing its creation time to the time it was added in the InCallUi and seeing if they
    // fall within a certain threshold.
    private static final int MATCH_BLOCKED_CALL_THRESHOLD_MS = 3000;

    private final Context context;
    private final Listener listener;
    private final String number;
    private final long timeAddedMillis;

    /**
     * Creates the task to delete the new {@link CallLog} entry from the given blocked number.
     *
     * @param number          The blocked number.
     * @param timeAddedMillis The time at which the call from the blocked number was placed.
     */
    public DeleteBlockedCallTask(
            Context context, Listener listener, String number, long timeAddedMillis) {
        this.context = Objects.requireNonNull(context);
        this.listener = Objects.requireNonNull(listener);
        this.number = number;
        this.timeAddedMillis = timeAddedMillis;
    }

    @Override
    public Long doInBackground(Void... params) {
        if (ContextCompat.checkSelfPermission(context, permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, permission.WRITE_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            LogUtil.i("DeleteBlockedCallTask.doInBackground", "missing call log permissions");
            return -1L;
        }

        // First, lookup the call log entry of the most recent call with this number.
        try (Cursor cursor =
                     context
                             .getContentResolver()
                             .query(
                                     TelecomUtil.getCallLogUri(context),
                                     CallLogDeleteBlockedCallQuery.PROJECTION,
                                     CallLog.Calls.NUMBER + "= ?",
                                     new String[]{number},
                                     CallLog.Calls.DATE + " DESC LIMIT 1")) {

            // If match is found, delete this call log entry and return the call log entry id.
            if (cursor != null && cursor.moveToFirst()) {
                long creationTime = cursor.getLong(CallLogDeleteBlockedCallQuery.DATE_COLUMN_INDEX);
                if (timeAddedMillis > creationTime
                        && timeAddedMillis - creationTime < MATCH_BLOCKED_CALL_THRESHOLD_MS) {
                    long callLogEntryId = cursor.getLong(CallLogDeleteBlockedCallQuery.ID_COLUMN_INDEX);
                    context
                            .getContentResolver()
                            .delete(
                                    TelecomUtil.getCallLogUri(context),
                                    CallLog.Calls._ID + " IN (" + callLogEntryId + ")",
                                    null);
                    return callLogEntryId;
                }
            }
        }
        return -1L;
    }

    @Override
    public void onPostExecute(Long callLogEntryId) {
        listener.onDeleteBlockedCallTaskComplete(callLogEntryId >= 0);
    }

    /**
     * Callback invoked when delete is complete.
     */
    public interface Listener {

        void onDeleteBlockedCallTaskComplete(boolean didFindEntry);
    }

    private static class CallLogDeleteBlockedCallQuery {

        static final String[] PROJECTION = new String[]{CallLog.Calls._ID, CallLog.Calls.DATE};

        static final int ID_COLUMN_INDEX = 0;
        static final int DATE_COLUMN_INDEX = 1;
    }
}
