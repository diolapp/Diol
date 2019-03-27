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

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Network;
import android.os.Build.VERSION_CODES;
import android.support.annotation.WorkerThread;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.common.Assert;
import app.diol.voicemail.PinChanger;
import app.diol.voicemail.impl.imap.ImapHelper;
import app.diol.voicemail.impl.imap.ImapHelper.InitializingException;
import app.diol.voicemail.impl.mail.MessagingException;
import app.diol.voicemail.impl.sync.VvmNetworkRequest;
import app.diol.voicemail.impl.sync.VvmNetworkRequest.NetworkWrapper;
import app.diol.voicemail.impl.sync.VvmNetworkRequest.RequestFailedException;

@TargetApi(VERSION_CODES.O)
class PinChangerImpl implements PinChanger {

    private static final String KEY_SCRAMBLED_PIN = "default_old_pin"; // legacy name, DO NOT CHANGE
    private final Context context;
    private final PhoneAccountHandle phoneAccountHandle;

    PinChangerImpl(Context context, PhoneAccountHandle phoneAccountHandle) {
        this.context = context;
        this.phoneAccountHandle = phoneAccountHandle;
    }

    @WorkerThread
    @Override
    @ChangePinResult
    public int changePin(String oldPin, String newPin) {
        Assert.isWorkerThread();
        OmtpVvmCarrierConfigHelper config = new OmtpVvmCarrierConfigHelper(context, phoneAccountHandle);
        VoicemailStatus.Editor status = VoicemailStatus.edit(context, phoneAccountHandle);
        try (NetworkWrapper networkWrapper = VvmNetworkRequest.getNetwork(config, phoneAccountHandle, status)) {
            Network network = networkWrapper.get();
            try (ImapHelper helper = new ImapHelper(context, phoneAccountHandle, network, status)) {
                return helper.changePin(oldPin, newPin);
            } catch (InitializingException | MessagingException e) {
                VvmLog.e("VoicemailClientImpl.changePin", "ChangePinNetworkRequestCallback: onAvailable: " + e);
                return PinChanger.CHANGE_PIN_SYSTEM_ERROR;
            }

        } catch (RequestFailedException e) {
            return PinChanger.CHANGE_PIN_SYSTEM_ERROR;
        }
    }

    @Override
    public String getScrambledPin() {
        return new VisualVoicemailPreferences(context, phoneAccountHandle).getString(KEY_SCRAMBLED_PIN);
    }

    @Override
    public void setScrambledPin(String pin) {
        new VisualVoicemailPreferences(context, phoneAccountHandle).edit().putString(KEY_SCRAMBLED_PIN, pin).apply();
        if (pin == null) {
            new OmtpVvmCarrierConfigHelper(context, phoneAccountHandle)
                    .handleEvent(VoicemailStatus.edit(context, phoneAccountHandle), OmtpEvents.CONFIG_PIN_SET);
        }
    }

    @Override
    public PinSpecification getPinSpecification() {
        PinSpecification result = new PinSpecification();
        VisualVoicemailPreferences preferences = new VisualVoicemailPreferences(context, phoneAccountHandle);
        // The OMTP pin length format is {min}-{max}
        String[] lengths = preferences.getString(OmtpConstants.TUI_PASSWORD_LENGTH, "").split("-");
        if (lengths.length == 2) {
            try {
                result.minLength = Integer.parseInt(lengths[0]);
                result.maxLength = Integer.parseInt(lengths[1]);
            } catch (NumberFormatException e) {
                // do nothing, return default value;
            }
        }
        return result;
    }
}
