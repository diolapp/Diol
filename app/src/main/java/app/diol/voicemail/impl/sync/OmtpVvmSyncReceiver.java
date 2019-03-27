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

package app.diol.voicemail.impl.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.VoicemailContract;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import java.util.List;

import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.ActivationTask;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.settings.VisualVoicemailSettingsUtil;

public class OmtpVvmSyncReceiver extends BroadcastReceiver {

    private static final String TAG = "OmtpVvmSyncReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!VoicemailComponent.get(context).getVoicemailClient().isVoicemailModuleEnabled()) {
            // ACTION_SYNC_VOICEMAIL is available pre-O, ignore if received.
            return;
        }

        if (VoicemailContract.ACTION_SYNC_VOICEMAIL.equals(intent.getAction())) {
            VvmLog.v(TAG, "Sync intent received");

            List<PhoneAccountHandle> accounts = context.getSystemService(TelecomManager.class).getCallCapablePhoneAccounts();
            for (PhoneAccountHandle phoneAccount : accounts) {
                if (!VisualVoicemailSettingsUtil.isEnabled(context, phoneAccount)) {
                    continue;
                }
                if (!VvmAccountManager.isAccountActivated(context, phoneAccount)) {
                    VvmLog.i(TAG, "Unactivated account " + phoneAccount + " found, activating");
                    ActivationTask.start(context, phoneAccount, null);
                } else {
                    SyncTask.start(context, phoneAccount);
                }
            }
        }
    }
}
