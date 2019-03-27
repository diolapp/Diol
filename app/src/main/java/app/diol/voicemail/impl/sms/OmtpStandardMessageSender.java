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
import android.text.TextUtils;

import app.diol.voicemail.impl.OmtpConstants;

/**
 * A implementation of the OmtpMessageSender using the standard OMTP sms
 * protocol.
 */
public class OmtpStandardMessageSender extends OmtpMessageSender {
    private final String clientType;
    private final String protocolVersion;
    private final String clientPrefix;

    /**
     * Creates a new instance of OmtpStandardMessageSender.
     *
     * @param applicationPort   If set to a value > 0 then a binary sms is sent to
     *                          this port number. Otherwise, a standard text SMS is
     *                          sent.
     * @param destinationNumber Destination number to be used.
     * @param clientType        The "ct" field to be set in the MO message. This is
     *                          the value used by the VVM server to identify the
     *                          client. Certain VVM servers require a specific
     *                          agreed value for this field.
     * @param protocolVersion   OMTP protocol version.
     * @param clientPrefix      The client prefix requested to be used by the server
     *                          in its MT messages.
     */
    public OmtpStandardMessageSender(Context context, PhoneAccountHandle phoneAccountHandle, short applicationPort,
                                     String destinationNumber, String clientType, String protocolVersion, String clientPrefix) {
        super(context, phoneAccountHandle, applicationPort, destinationNumber);
        this.clientType = clientType;
        this.protocolVersion = protocolVersion;
        this.clientPrefix = clientPrefix;
    }

    // Activate message:
    // V1.1: Activate:pv=<value>;ct=<value>
    // V1.2: Activate:pv=<value>;ct=<value>;pt=<value>;<Clientprefix>
    // V1.3: Activate:pv=<value>;ct=<value>;pt=<value>;<Clientprefix>
    @Override
    public void requestVvmActivation(@Nullable PendingIntent sentIntent) {
        StringBuilder sb = new StringBuilder().append(OmtpConstants.ACTIVATE_REQUEST);

        appendProtocolVersionAndClientType(sb);
        if (TextUtils.equals(protocolVersion, OmtpConstants.PROTOCOL_VERSION1_2)
                || TextUtils.equals(protocolVersion, OmtpConstants.PROTOCOL_VERSION1_3)) {
            appendApplicationPort(sb);
            appendClientPrefix(sb);
        }

        sendSms(sb.toString(), sentIntent);
    }

    // Deactivate message:
    // V1.1: Deactivate:pv=<value>;ct=<string>
    // V1.2: Deactivate:pv=<value>;ct=<string>
    // V1.3: Deactivate:pv=<value>;ct=<string>
    @Override
    public void requestVvmDeactivation(@Nullable PendingIntent sentIntent) {
        StringBuilder sb = new StringBuilder().append(OmtpConstants.DEACTIVATE_REQUEST);
        appendProtocolVersionAndClientType(sb);

        sendSms(sb.toString(), sentIntent);
    }

    // Status message:
    // V1.1: STATUS
    // V1.2: STATUS
    // V1.3: STATUS:pv=<value>;ct=<value>;pt=<value>;<Clientprefix>
    @Override
    public void requestVvmStatus(@Nullable PendingIntent sentIntent) {
        StringBuilder sb = new StringBuilder().append(OmtpConstants.STATUS_REQUEST);

        if (TextUtils.equals(protocolVersion, OmtpConstants.PROTOCOL_VERSION1_3)) {
            appendProtocolVersionAndClientType(sb);
            appendApplicationPort(sb);
            appendClientPrefix(sb);
        }

        sendSms(sb.toString(), sentIntent);
    }

    private void appendProtocolVersionAndClientType(StringBuilder sb) {
        sb.append(OmtpConstants.SMS_PREFIX_SEPARATOR);
        appendField(sb, OmtpConstants.PROTOCOL_VERSION, protocolVersion);
        sb.append(OmtpConstants.SMS_FIELD_SEPARATOR);
        appendField(sb, OmtpConstants.CLIENT_TYPE, clientType);
    }

    private void appendApplicationPort(StringBuilder sb) {
        sb.append(OmtpConstants.SMS_FIELD_SEPARATOR);
        appendField(sb, OmtpConstants.APPLICATION_PORT, applicationPort);
    }

    private void appendClientPrefix(StringBuilder sb) {
        sb.append(OmtpConstants.SMS_FIELD_SEPARATOR);
        sb.append(clientPrefix);
    }
}
