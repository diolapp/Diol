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
package app.diol.voicemail.impl.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

import app.diol.voicemail.impl.OmtpConstants;

/**
 * An implementation of the OmtpMessageSender for T-Mobile.
 */
public class OmtpCvvmMessageSender extends OmtpMessageSender {
    public OmtpCvvmMessageSender(Context context, PhoneAccountHandle phoneAccountHandle, short applicationPort,
                                 String destinationNumber) {
        super(context, phoneAccountHandle, applicationPort, destinationNumber);
    }

    @Override
    public void requestVvmActivation(@Nullable PendingIntent sentIntent) {
        sendCvvmMessage(OmtpConstants.ACTIVATE_REQUEST, sentIntent);
    }

    @Override
    public void requestVvmDeactivation(@Nullable PendingIntent sentIntent) {
        sendCvvmMessage(OmtpConstants.DEACTIVATE_REQUEST, sentIntent);
    }

    @Override
    public void requestVvmStatus(@Nullable PendingIntent sentIntent) {
        sendCvvmMessage(OmtpConstants.STATUS_REQUEST, sentIntent);
    }

    private void sendCvvmMessage(String request, PendingIntent sentIntent) {
        StringBuilder sb = new StringBuilder().append(request);
        sb.append(OmtpConstants.SMS_PREFIX_SEPARATOR);
        appendField(sb, "dt" /* device type */, "6" /* no VTT (transcription) support */);
        sendSms(sb.toString(), sentIntent);
    }
}
