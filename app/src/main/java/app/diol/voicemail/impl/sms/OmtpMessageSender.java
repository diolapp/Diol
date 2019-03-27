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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import app.diol.voicemail.impl.OmtpConstants;
import app.diol.voicemail.impl.VvmLog;

/**
 * Send client originated OMTP messages to the OMTP server.
 *
 * <p>
 * Uses {@link PendingIntent} instead of a call back to notify when the message
 * is sent. This is primarily to keep the implementation simple and reuse what
 * the underlying {@link SmsManager} interface provides.
 *
 * <p>
 * Provides simple APIs to send different types of mobile originated OMTP SMS to
 * the VVM server.
 */
@TargetApi(VERSION_CODES.O)
public abstract class OmtpMessageSender {
    protected static final String TAG = "OmtpMessageSender";
    protected final Context context;
    protected final PhoneAccountHandle phoneAccountHandle;
    protected final short applicationPort;
    protected final String destinationNumber;

    public OmtpMessageSender(Context context, PhoneAccountHandle phoneAccountHandle, short applicationPort,
                             String destinationNumber) {
        this.context = context;
        this.phoneAccountHandle = phoneAccountHandle;
        this.applicationPort = applicationPort;
        this.destinationNumber = destinationNumber;
    }

    /**
     * Sends a request to the VVM server to activate VVM for the current subscriber.
     *
     * @param sentIntent If not NULL this PendingIntent is broadcast when the
     *                   message is successfully sent, or failed.
     */
    public void requestVvmActivation(@Nullable PendingIntent sentIntent) {
    }

    /**
     * Sends a request to the VVM server to deactivate VVM for the current
     * subscriber.
     *
     * @param sentIntent If not NULL this PendingIntent is broadcast when the
     *                   message is successfully sent, or failed.
     */
    public void requestVvmDeactivation(@Nullable PendingIntent sentIntent) {
    }

    /**
     * Send a request to the VVM server to get account status of the current
     * subscriber.
     *
     * @param sentIntent If not NULL this PendingIntent is broadcast when the
     *                   message is successfully sent, or failed.
     */
    public void requestVvmStatus(@Nullable PendingIntent sentIntent) {
    }

    protected void sendSms(String text, PendingIntent sentIntent) {

        VvmLog.v(TAG, String.format("Sending sms '%s' to %s:%d", text, destinationNumber, applicationPort));

        context.getSystemService(TelephonyManager.class).createForPhoneAccountHandle(phoneAccountHandle)
                .sendVisualVoicemailSms(destinationNumber, applicationPort, text, sentIntent);
    }

    protected void appendField(StringBuilder sb, String field, Object value) {
        sb.append(field).append(OmtpConstants.SMS_KEY_VALUE_SEPARATOR).append(value);
    }
}
