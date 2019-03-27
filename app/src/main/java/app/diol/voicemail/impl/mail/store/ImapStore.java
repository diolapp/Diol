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

package app.diol.voicemail.impl.mail.store;

import android.content.Context;
import android.net.Network;

import org.apache.james.mime4j.MimeException;

import java.io.IOException;
import java.io.InputStream;

import app.diol.voicemail.impl.imap.ImapHelper;
import app.diol.voicemail.impl.mail.MailTransport;
import app.diol.voicemail.impl.mail.Message;
import app.diol.voicemail.impl.mail.MessagingException;
import app.diol.voicemail.impl.mail.internet.MimeMessage;

public class ImapStore {
    /**
     * A global suggestion to Store implementors on how much of the body should be
     * returned on FetchProfile.Item.BODY_SANE requests. We'll use 125k now.
     */
    public static final int FETCH_BODY_SANE_SUGGESTED_SIZE = (125 * 1024);
    public static final int FLAG_NONE = 0x00; // No flags
    public static final int FLAG_SSL = 0x01; // Use SSL
    public static final int FLAG_TLS = 0x02; // Use TLS
    public static final int FLAG_AUTHENTICATE = 0x04; // Use name/password for authentication
    public static final int FLAG_TRUST_ALL = 0x08; // Trust all certificates
    public static final int FLAG_OAUTH = 0x10; // Use OAuth for authentication
    private final Context context;
    private final ImapHelper helper;
    private final String username;
    private final String password;
    private final MailTransport transport;
    private ImapConnection connection;

    /**
     * Contains all the information necessary to log into an imap server
     */
    public ImapStore(Context context, ImapHelper helper, String username, String password, int port, String serverName,
                     int flags, Network network) {
        this.context = context;
        this.helper = helper;
        this.username = username;
        this.password = password;
        transport = new MailTransport(context, this.getImapHelper(), network, serverName, port, flags);
    }

    /**
     * Returns UIDs of Messages joined with "," as the separator.
     */
    static String joinMessageUids(Message[] messages) {
        StringBuilder sb = new StringBuilder();
        boolean notFirst = false;
        for (Message m : messages) {
            if (notFirst) {
                sb.append(',');
            }
            sb.append(m.getUid());
            notFirst = true;
        }
        return sb.toString();
    }

    public Context getContext() {
        return context;
    }

    public ImapHelper getImapHelper() {
        return helper;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Returns a clone of the transport associated with this store.
     */
    MailTransport cloneTransport() {
        return transport.clone();
    }

    public void closeConnection() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public ImapConnection getConnection() {
        if (connection == null) {
            connection = new ImapConnection(this);
        }
        return connection;
    }

    static class ImapMessage extends MimeMessage {
        private ImapFolder folder;

        ImapMessage(String uid, ImapFolder folder) {
            this.uid = uid;
            this.folder = folder;
        }

        public void setSize(int size) {
            this.size = size;
        }

        @Override
        public void parse(InputStream in) throws IOException, MessagingException, MimeException {
            super.parse(in);
        }

        public void setFlagInternal(String flag, boolean set) throws MessagingException {
            super.setFlag(flag, set);
        }

        @Override
        public void setFlag(String flag, boolean set) throws MessagingException {
            super.setFlag(flag, set);
            folder.setFlags(new Message[]{this}, new String[]{flag}, set);
        }
    }

    static class ImapException extends MessagingException {
        private static final long serialVersionUID = 1L;

        private final String status;
        private final String statusMessage;
        private final String alertText;
        private final String responseCode;

        public ImapException(String message, String status, String statusMessage, String alertText, String responseCode) {
            super(message);
            this.status = status;
            this.statusMessage = statusMessage;
            this.alertText = alertText;
            this.responseCode = responseCode;
        }

        public String getStatus() {
            return status;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public String getAlertText() {
            return alertText;
        }

        public String getResponseCode() {
            return responseCode;
        }
    }
}
