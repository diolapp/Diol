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

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.ThreadUtil;

/**
 * Handles asynchronous queries to the call log for voicemail.
 */
public class VoicemailQueryHandler extends AsyncQueryHandler {

    /**
     * The token for the query to mark all new voicemails as old.
     */
    private static final int UPDATE_MARK_VOICEMAILS_AS_OLD_TOKEN = 50;

    @MainThread
    private VoicemailQueryHandler(ContentResolver contentResolver) {
        super(contentResolver);
        Assert.isMainThread();
    }

    @WorkerThread
    public static void markAllNewVoicemailsAsOld(final @NonNull Context context) {
        ThreadUtil.postOnUiThread(
                () -> {
                    new VoicemailQueryHandler(context.getContentResolver())
                            .markNewVoicemailsAsOld(context, null);
                });
    }

    @WorkerThread
    public static void markSingleNewVoicemailAsOld(
            final @NonNull Context context, final Uri voicemailUri) {
        if (voicemailUri == null) {
            LogUtil.e("VoicemailQueryHandler.markSingleNewVoicemailAsOld", "voicemail URI is null");
            return;
        }
        ThreadUtil.postOnUiThread(
                () -> {
                    new VoicemailQueryHandler(context.getContentResolver())
                            .markNewVoicemailsAsOld(context, voicemailUri);
                });
    }

    /**
     * Updates all new voicemails to mark them as old.
     */
    private void markNewVoicemailsAsOld(Context context, @Nullable Uri voicemailUri) {
        // Mark all "new" voicemails as not new anymore.
        StringBuilder where = new StringBuilder();
        where.append(Calls.NEW);
        where.append(" = 1 AND ");
        where.append(Calls.TYPE);
        where.append(" = ?");

        if (voicemailUri != null) {
            where.append(" AND ").append(Calls.VOICEMAIL_URI).append(" = ?");
        }

        ContentValues values = new ContentValues(1);
        values.put(Calls.NEW, "0");

        startUpdate(
                UPDATE_MARK_VOICEMAILS_AS_OLD_TOKEN,
                null,
                Calls.CONTENT_URI_WITH_VOICEMAIL,
                values,
                where.toString(),
                voicemailUri == null
                        ? new String[]{Integer.toString(Calls.VOICEMAIL_TYPE)}
                        : new String[]{Integer.toString(Calls.VOICEMAIL_TYPE), voicemailUri.toString()});

        // No more notifications, stop monitoring the voicemail provider
        VoicemailNotificationJobService.cancelJob(context);
    }
}
