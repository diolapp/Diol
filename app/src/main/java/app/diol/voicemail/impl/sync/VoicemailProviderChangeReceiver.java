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

import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.settings.VisualVoicemailSettingsUtil;

/**
 * Receives changes to the voicemail provider so they can be sent to the
 * voicemail server.
 */
public class VoicemailProviderChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!VoicemailComponent.get(context).getVoicemailClient().isVoicemailModuleEnabled()) {
            return;
        }
        boolean isSelfChanged = intent.getBooleanExtra(VoicemailContract.EXTRA_SELF_CHANGE, false);
        if (!isSelfChanged) {
            for (PhoneAccountHandle phoneAccount : VvmAccountManager.getActiveAccounts(context)) {
                if (!VisualVoicemailSettingsUtil.isEnabled(context, phoneAccount)) {
                    continue;
                }
                UploadTask.start(context, phoneAccount);
            }
        }
    }
}
