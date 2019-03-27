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

package app.diol.voicemail.impl.protocol;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

import app.diol.voicemail.impl.ActivationTask;
import app.diol.voicemail.impl.DefaultOmtpEventHandler;
import app.diol.voicemail.impl.OmtpConstants;
import app.diol.voicemail.impl.OmtpEvents;
import app.diol.voicemail.impl.OmtpVvmCarrierConfigHelper;
import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.sms.OmtpMessageSender;
import app.diol.voicemail.impl.sms.StatusMessage;

public abstract class VisualVoicemailProtocol {

    /**
     * Activation should cause the carrier to respond with a STATUS SMS.
     */
    public void startActivation(OmtpVvmCarrierConfigHelper config, PendingIntent sentIntent) {
        OmtpMessageSender messageSender = ProtocolHelper.getMessageSender(this, config);
        if (messageSender != null) {
            messageSender.requestVvmActivation(sentIntent);
        }
    }

    public void startDeactivation(OmtpVvmCarrierConfigHelper config) {
        OmtpMessageSender messageSender = ProtocolHelper.getMessageSender(this, config);
        if (messageSender != null) {
            messageSender.requestVvmDeactivation(null);
        }
    }

    public boolean supportsProvisioning() {
        return false;
    }

    public void startProvisioning(ActivationTask task, PhoneAccountHandle handle, OmtpVvmCarrierConfigHelper config,
                                  VoicemailStatus.Editor editor, StatusMessage message, Bundle data, boolean isCarrierInitiated) {
        // Do nothing
    }

    public void requestStatus(OmtpVvmCarrierConfigHelper config, @Nullable PendingIntent sentIntent) {
        OmtpMessageSender messageSender = ProtocolHelper.getMessageSender(this, config);
        if (messageSender != null) {
            messageSender.requestVvmStatus(sentIntent);
        }
    }

    public abstract OmtpMessageSender createMessageSender(Context context, PhoneAccountHandle phoneAccountHandle,
                                                          short applicationPort, String destinationNumber);

    /**
     * Translate an OMTP IMAP command to the protocol specific one. For example,
     * changing the TUI password on OMTP is XCHANGE_TUI_PWD, but on CVVM and VVM3 it
     * is CHANGE_TUI_PWD.
     *
     * @param command A String command in {@link OmtpConstants}, the exact instance
     *                should be used instead of its' value.
     * @returns Translated command, or {@code null} if not available in this
     * protocol
     */
    public String getCommand(String command) {
        return command;
    }

    public void handleEvent(Context context, OmtpVvmCarrierConfigHelper config, VoicemailStatus.Editor status,
                            OmtpEvents event) {
        DefaultOmtpEventHandler.handleEvent(context, config, status, event);
    }

    /**
     * Given an VVM SMS with an unknown {@code event}, let the protocol attempt to
     * translate it into an equivalent STATUS SMS. Returns {@code null} if it cannot
     * be translated.
     */
    @Nullable
    public Bundle translateStatusSmsBundle(OmtpVvmCarrierConfigHelper config, String event, Bundle data) {
        return null;
    }
}
