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
package app.diol.voicemail.impl.mail;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.Date;
import java.util.HashSet;

public abstract class Message implements Part, Body {
    public static final Message[] EMPTY_ARRAY = new Message[0];

    public static final String RECIPIENT_TYPE_TO = "to";
    public static final String RECIPIENT_TYPE_CC = "cc";
    public static final String RECIPIENT_TYPE_BCC = "bcc";
    protected String uid;
    protected Date internalDate;
    private HashSet<String> flags = null;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public abstract String getSubject() throws MessagingException;

    public abstract void setSubject(String subject) throws MessagingException;

    public Date getInternalDate() {
        return internalDate;
    }

    public void setInternalDate(Date internalDate) {
        this.internalDate = internalDate;
    }

    public abstract Date getReceivedDate() throws MessagingException;

    public abstract Date getSentDate() throws MessagingException;

    public abstract void setSentDate(Date sentDate) throws MessagingException;

    @Nullable
    public abstract Long getDuration() throws MessagingException;

    public abstract Address[] getRecipients(String type) throws MessagingException;

    public abstract void setRecipients(String type, Address[] addresses) throws MessagingException;

    public void setRecipient(String type, Address address) throws MessagingException {
        setRecipients(type, new Address[]{address});
    }

    public abstract Address[] getFrom() throws MessagingException;

    public abstract void setFrom(Address from) throws MessagingException;

    public abstract Address[] getReplyTo() throws MessagingException;

    public abstract void setReplyTo(Address[] from) throws MessagingException;

    public abstract String getMessageId() throws MessagingException;

    // Always use these instead of getHeader("Message-ID") or
    // setHeader("Message-ID");
    public abstract void setMessageId(String messageId) throws MessagingException;

    @Override
    public boolean isMimeType(String mimeType) throws MessagingException {
        return getContentType().startsWith(mimeType);
    }

    private HashSet<String> getFlagSet() {
        if (flags == null) {
            flags = new HashSet<String>();
        }
        return flags;
    }

    /*
     * TODO Refactor Flags at some point to be able to store user defined flags.
     */
    public String[] getFlags() {
        return getFlagSet().toArray(new String[]{});
    }

    /**
     * Set/clear a flag directly, without involving overrides of {@link #setFlag} in
     * subclasses. Only used for testing.
     */
    @VisibleForTesting
    private final void setFlagDirectlyForTest(String flag, boolean set) throws MessagingException {
        if (set) {
            getFlagSet().add(flag);
        } else {
            getFlagSet().remove(flag);
        }
    }

    public void setFlag(String flag, boolean set) throws MessagingException {
        setFlagDirectlyForTest(flag, set);
    }

    /**
     * This method calls setFlag(String, boolean)
     *
     * @param flags
     * @param set
     */
    public void setFlags(String[] flags, boolean set) throws MessagingException {
        for (String flag : flags) {
            setFlag(flag, set);
        }
    }

    public boolean isSet(String flag) {
        return getFlagSet().contains(flag);
    }

    public abstract void saveChanges() throws MessagingException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + ':' + uid;
    }

    public enum RecipientType {
        TO, CC, BCC,
    }
}
