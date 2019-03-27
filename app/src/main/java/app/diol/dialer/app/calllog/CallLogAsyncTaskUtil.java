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

package app.diol.dialer.app.calllog;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.provider.VoicemailContract.Voicemails;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.AsyncTaskExecutor;
import app.diol.dialer.common.concurrent.AsyncTaskExecutors;
import app.diol.dialer.util.PermissionsUtil;
import app.diol.voicemail.VoicemailClient;

/**
 * TODO(calderwoodra): documentation
 */
public class CallLogAsyncTaskUtil {

    private static final String TAG = "CallLogAsyncTaskUtil";
    private static AsyncTaskExecutor asyncTaskExecutor;

    private static void initTaskExecutor() {
        asyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
    }

    public static void markVoicemailAsRead(
            @NonNull final Context context, @NonNull final Uri voicemailUri) {
        LogUtil.enterBlock("CallLogAsyncTaskUtil.markVoicemailAsRead, voicemailUri: " + voicemailUri);
        if (asyncTaskExecutor == null) {
            initTaskExecutor();
        }

        asyncTaskExecutor.submit(
                Tasks.MARK_VOICEMAIL_READ,
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... params) {
                        ContentValues values = new ContentValues();
                        values.put(Voicemails.IS_READ, true);
                        // "External" changes to the database will be automatically marked as dirty, but this
                        // voicemail might be from dialer so it need to be marked manually.
                        values.put(Voicemails.DIRTY, 1);
                        if (context
                                .getContentResolver()
                                .update(voicemailUri, values, Voicemails.IS_READ + " = 0", null)
                                > 0) {
                            uploadVoicemailLocalChangesToServer(context);
                            CallLogNotificationsService.markAllNewVoicemailsAsOld(context);
                        }
                        return null;
                    }
                });
    }

    public static void deleteVoicemail(
            @NonNull final Context context,
            final Uri voicemailUri,
            @Nullable final CallLogAsyncTaskListener callLogAsyncTaskListener) {
        if (asyncTaskExecutor == null) {
            initTaskExecutor();
        }

        asyncTaskExecutor.submit(
                Tasks.DELETE_VOICEMAIL,
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... params) {
                        deleteVoicemailSynchronous(context, voicemailUri);
                        return null;
                    }

                    @Override
                    public void onPostExecute(Void result) {
                        if (callLogAsyncTaskListener != null) {
                            callLogAsyncTaskListener.onDeleteVoicemail();
                        }
                    }
                });
    }

    public static void deleteVoicemailSynchronous(Context context, Uri voicemailUri) {
        ContentValues values = new ContentValues();
        values.put(Voicemails.DELETED, "1");
        context.getContentResolver().update(voicemailUri, values, null, null);
        // TODO(a bug): check which source package is changed. Don't need
        // to upload changes on foreign voicemails, they will get a PROVIDER_CHANGED
        uploadVoicemailLocalChangesToServer(context);
    }

    public static void markCallAsRead(@NonNull final Context context, @NonNull final long[] callIds) {
        if (!PermissionsUtil.hasPhonePermissions(context)
                || !PermissionsUtil.hasCallLogWritePermissions(context)) {
            return;
        }
        if (asyncTaskExecutor == null) {
            initTaskExecutor();
        }

        asyncTaskExecutor.submit(
                Tasks.MARK_CALL_READ,
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... params) {

                        StringBuilder where = new StringBuilder();
                        where.append(CallLog.Calls.TYPE).append(" = ").append(CallLog.Calls.MISSED_TYPE);
                        where.append(" AND ");

                        Long[] callIdLongs = new Long[callIds.length];
                        for (int i = 0; i < callIds.length; i++) {
                            callIdLongs[i] = callIds[i];
                        }
                        where
                                .append(CallLog.Calls._ID)
                                .append(" IN (" + TextUtils.join(",", callIdLongs) + ")");

                        ContentValues values = new ContentValues(1);
                        values.put(CallLog.Calls.IS_READ, "1");
                        context
                                .getContentResolver()
                                .update(CallLog.Calls.CONTENT_URI, values, where.toString(), null);
                        return null;
                    }
                });
    }

    private static void uploadVoicemailLocalChangesToServer(Context context) {
        Intent intent = new Intent(VoicemailClient.ACTION_UPLOAD);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    /**
     * The enumeration of {@link AsyncTask} objects used in this class.
     */
    public enum Tasks {
        DELETE_VOICEMAIL,
        DELETE_CALL,
        MARK_VOICEMAIL_READ,
        MARK_CALL_READ,
        GET_CALL_DETAILS,
        UPDATE_DURATION,
    }

    /**
     * TODO(calderwoodra): documentation
     */
    public interface CallLogAsyncTaskListener {
        void onDeleteVoicemail();
    }
}
