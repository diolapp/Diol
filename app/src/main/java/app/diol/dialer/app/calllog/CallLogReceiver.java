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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.VoicemailContract;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.database.CallLogQueryHandler;
import app.diol.dialer.voicemail.listui.error.VoicemailStatusCorruptionHandler;
import app.diol.dialer.voicemail.listui.error.VoicemailStatusCorruptionHandler.Source;

/**
 * Receiver for call log events.
 *
 * <p>It is currently used to handle {@link VoicemailContract#ACTION_NEW_VOICEMAIL} and {@link
 * Intent#ACTION_BOOT_COMPLETED}.
 */
public class CallLogReceiver extends BroadcastReceiver {

    private static void checkVoicemailStatus(Context context) {
        new CallLogQueryHandler(
                context,
                context.getContentResolver(),
                new CallLogQueryHandler.Listener() {
                    @Override
                    public void onVoicemailStatusFetched(Cursor statusCursor) {
                        VoicemailStatusCorruptionHandler.maybeFixVoicemailStatus(
                                context, statusCursor, Source.Notification);
                    }

                    @Override
                    public void onVoicemailUnreadCountFetched(Cursor cursor) {
                        // Do nothing
                    }

                    @Override
                    public void onMissedCallsUnreadCountFetched(Cursor cursor) {
                        // Do nothing
                    }

                    @Override
                    public boolean onCallsFetched(Cursor combinedCursor) {
                        return false;
                    }
                })
                .fetchVoicemailStatus();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (VoicemailContract.ACTION_NEW_VOICEMAIL.equals(intent.getAction())) {
            checkVoicemailStatus(context);
            PendingResult pendingResult = goAsync();
            VisualVoicemailUpdateTask.scheduleTask(context, pendingResult::finish);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            PendingResult pendingResult = goAsync();
            VisualVoicemailUpdateTask.scheduleTask(context, pendingResult::finish);
        } else {
            LogUtil.w("CallLogReceiver.onReceive", "could not handle: " + intent);
        }
    }
}
