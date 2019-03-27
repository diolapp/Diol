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

import android.content.Context;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;

import app.diol.voicemail.impl.sync.SyncTask;
import app.diol.voicemail.impl.sync.VoicemailStatusQueryHelper;
import app.diol.voicemail.impl.sync.VvmAccountManager;

/**
 * Check if service is lost and indicate this in the voicemail status. TODO(a bug): Not used
 * for now, restore it.
 */
public class VvmPhoneStateListener extends PhoneStateListener {

    private static final String TAG = "VvmPhoneStateListener";

    private PhoneAccountHandle phoneAccount;
    private Context context;
    private int previousState = -1;

    public VvmPhoneStateListener(Context context, PhoneAccountHandle accountHandle) {
        // TODO(twyen): a bug too much trouble to call super constructor through reflection,
        // just use non-phoneAccountHandle version for now.
        super();
        this.context = context;
        phoneAccount = accountHandle;
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        if (phoneAccount == null) {
            VvmLog.e(
                    TAG,
                    "onServiceStateChanged on phoneAccount "
                            + phoneAccount
                            + " with invalid phoneAccountHandle, ignoring");
            return;
        }

        int state = serviceState.getState();
        if (state == previousState
                || (state != ServiceState.STATE_IN_SERVICE
                && previousState != ServiceState.STATE_IN_SERVICE)) {
            // Only interested in state changes or transitioning into or out of "in service".
            // Otherwise just quit.
            previousState = state;
            return;
        }

        OmtpVvmCarrierConfigHelper helper = new OmtpVvmCarrierConfigHelper(context, phoneAccount);

        if (state == ServiceState.STATE_IN_SERVICE) {
            VoicemailStatusQueryHelper voicemailStatusQueryHelper =
                    new VoicemailStatusQueryHelper(context);
            if (voicemailStatusQueryHelper.isVoicemailSourceConfigured(phoneAccount)) {
                if (!voicemailStatusQueryHelper.isNotificationsChannelActive(phoneAccount)) {
                    VvmLog.v(TAG, "Notifications channel is active for " + phoneAccount);
                    helper.handleEvent(
                            VoicemailStatus.edit(context, phoneAccount), OmtpEvents.NOTIFICATION_IN_SERVICE);
                }
            }

            if (VvmAccountManager.isAccountActivated(context, phoneAccount)) {
                VvmLog.v(TAG, "Signal returned: requesting resync for " + phoneAccount);
                // If the source is already registered, run a full sync in case something was missed
                // while signal was down.
                SyncTask.start(context, phoneAccount);
            } else {
                VvmLog.v(TAG, "Signal returned: reattempting activation for " + phoneAccount);
                // Otherwise initiate an activation because this means that an OMTP source was
                // recognized but either the activation text was not successfully sent or a response
                // was not received.
                helper.startActivation();
            }
        } else {
            VvmLog.v(TAG, "Notifications channel is inactive for " + phoneAccount);

            if (!VvmAccountManager.isAccountActivated(context, phoneAccount)) {
                return;
            }
            helper.handleEvent(
                    VoicemailStatus.edit(context, phoneAccount), OmtpEvents.NOTIFICATION_SERVICE_LOST);
        }
        previousState = state;
    }
}
