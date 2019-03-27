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

import android.os.Bundle;

import app.diol.voicemail.impl.NeededForTesting;
import app.diol.voicemail.impl.OmtpConstants;
import app.diol.voicemail.impl.VisualVoicemailPreferences;
import app.diol.voicemail.impl.VvmLog;

/**
 * Structured data representation of OMTP STATUS message.
 *
 * <p>
 * The getters will return null if the field was not set in the message body or
 * it could not be parsed.
 */
public class StatusMessage {
    // NOTE: Following Status SMS fields are not yet parsed, as they do not seem
    // to be useful for initial omtp source implementation.
    // lang, g_len, vs_len, pw_len, pm, gm, vtc, vt

    private final String provisioningStatus;
    private final String statusReturnCode;
    private final String subscriptionUrl;
    private final String serverAddress;
    private final String tuiAccessNumber;
    private final String clientSmsDestinationNumber;
    private final String imapPort;
    private final String imapUserName;
    private final String imapPassword;
    private final String smtpPort;
    private final String smtpUserName;
    private final String smtpPassword;
    private final String tuiPasswordLength;

    public StatusMessage(Bundle wrappedData) {
        provisioningStatus = unquote(getString(wrappedData, OmtpConstants.PROVISIONING_STATUS));
        statusReturnCode = getString(wrappedData, OmtpConstants.RETURN_CODE);
        subscriptionUrl = getString(wrappedData, OmtpConstants.SUBSCRIPTION_URL);
        serverAddress = getString(wrappedData, OmtpConstants.SERVER_ADDRESS);
        tuiAccessNumber = getString(wrappedData, OmtpConstants.TUI_ACCESS_NUMBER);
        clientSmsDestinationNumber = getString(wrappedData, OmtpConstants.CLIENT_SMS_DESTINATION_NUMBER);
        imapPort = getString(wrappedData, OmtpConstants.IMAP_PORT);
        imapUserName = getString(wrappedData, OmtpConstants.IMAP_USER_NAME);
        imapPassword = getString(wrappedData, OmtpConstants.IMAP_PASSWORD);
        smtpPort = getString(wrappedData, OmtpConstants.SMTP_PORT);
        smtpUserName = getString(wrappedData, OmtpConstants.SMTP_USER_NAME);
        smtpPassword = getString(wrappedData, OmtpConstants.SMTP_PASSWORD);
        tuiPasswordLength = getString(wrappedData, OmtpConstants.TUI_PASSWORD_LENGTH);
    }

    private static String unquote(String string) {
        if (string.length() < 2) {
            return string;
        }
        if (string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    private static String getString(Bundle bundle, String key) {
        String value = bundle.getString(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    public String toString() {
        return "StatusMessage [mProvisioningStatus=" + provisioningStatus + ", mStatusReturnCode=" + statusReturnCode
                + ", mSubscriptionUrl=" + subscriptionUrl + ", mServerAddress=" + serverAddress + ", mTuiAccessNumber="
                + tuiAccessNumber + ", mClientSmsDestinationNumber=" + clientSmsDestinationNumber + ", mImapPort=" + imapPort
                + ", mImapUserName=" + imapUserName + ", mImapPassword=" + VvmLog.pii(imapPassword) + ", mSmtpPort=" + smtpPort
                + ", mSmtpUserName=" + smtpUserName + ", mSmtpPassword=" + VvmLog.pii(smtpPassword) + ", mTuiPasswordLength="
                + tuiPasswordLength + "]";
    }

    /**
     * @return the subscriber's VVM provisioning status.
     */
    public String getProvisioningStatus() {
        return provisioningStatus;
    }

    /**
     * @return the return-code of the status SMS.
     */
    public String getReturnCode() {
        return statusReturnCode;
    }

    /**
     * @return the URL of the voicemail server. This is the URL to send the users to
     * for subscribing to the visual voicemail service.
     */
    @NeededForTesting
    public String getSubscriptionUrl() {
        return subscriptionUrl;
    }

    /**
     * @return the voicemail server address. Either server IP address or fully
     * qualified domain name.
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * @return the Telephony User Interface number to call to access voicemails
     * directly from the IVR.
     */
    @NeededForTesting
    public String getTuiAccessNumber() {
        return tuiAccessNumber;
    }

    /**
     * @return the number to which client originated SMSes should be sent to.
     */
    @NeededForTesting
    public String getClientSmsDestinationNumber() {
        return clientSmsDestinationNumber;
    }

    /**
     * @return the IMAP server port to talk to.
     */
    public String getImapPort() {
        return imapPort;
    }

    /**
     * @return the IMAP user name to be used for authentication.
     */
    public String getImapUserName() {
        return imapUserName;
    }

    /**
     * @return the IMAP password to be used for authentication.
     */
    public String getImapPassword() {
        return imapPassword;
    }

    /**
     * @return the SMTP server port to talk to.
     */
    @NeededForTesting
    public String getSmtpPort() {
        return smtpPort;
    }

    /**
     * @return the SMTP user name to be used for SMTP authentication.
     */
    @NeededForTesting
    public String getSmtpUserName() {
        return smtpUserName;
    }

    /**
     * @return the SMTP password to be used for SMTP authentication.
     */
    @NeededForTesting
    public String getSmtpPassword() {
        return smtpPassword;
    }

    public String getTuiPasswordLength() {
        return tuiPasswordLength;
    }

    /**
     * Saves a StatusMessage to the {@link VisualVoicemailPreferences}. Not all
     * fields are saved.
     */
    public VisualVoicemailPreferences.Editor putStatus(VisualVoicemailPreferences.Editor editor) {
        return editor.putString(OmtpConstants.IMAP_PORT, getImapPort())
                .putString(OmtpConstants.SERVER_ADDRESS, getServerAddress())
                .putString(OmtpConstants.IMAP_USER_NAME, getImapUserName())
                .putString(OmtpConstants.IMAP_PASSWORD, getImapPassword())
                .putString(OmtpConstants.TUI_PASSWORD_LENGTH, getTuiPasswordLength());
    }
}
