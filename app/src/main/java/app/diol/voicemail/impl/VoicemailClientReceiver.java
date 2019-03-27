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

package app.diol.voicemail.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.voicemail.VoicemailClient;
import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.sync.UploadTask;
import app.diol.voicemail.impl.sync.VvmAccountManager;

/**
 * Receiver for broadcasts in {@link VoicemailClient#ACTION_UPLOAD}
 */
public class VoicemailClientReceiver extends BroadcastReceiver {

    /**
     * Upload local database changes to the server.
     */
    private static void doUpload(Context context) {
        LogUtil.i("VoicemailClientReceiver.onReceive", "ACTION_UPLOAD received");
        for (PhoneAccountHandle phoneAccountHandle : VvmAccountManager.getActiveAccounts(context)) {
            UploadTask.start(context, phoneAccountHandle);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!VoicemailComponent.get(context).getVoicemailClient().isVoicemailModuleEnabled()) {
            LogUtil.i("VoicemailClientReceiver.onReceive", "module disabled, ignoring " + intent.getAction());
            return;
        }
        switch (intent.getAction()) {
            case VoicemailClient.ACTION_UPLOAD:
                doUpload(context);
                break;
            default:
                Assert.fail("Unexpected action " + intent.getAction());
                break;
        }
    }
}
