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
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import app.diol.voicemail.impl.NeededForTesting;
import app.diol.voicemail.impl.OmtpConstants;

/**
 * Structured data representation of an OMTP SYNC message.
 *
 * <p>
 * Getters will return null if the field was not set in the message body or it
 * could not be parsed.
 */
public class SyncMessage {
    // Sync event that triggered this message.
    private final String syncTriggerEvent;
    // Total number of new messages on the server.
    private final int newMessageCount;
    // UID of the new message.
    private final String messageId;
    // Length of the message.
    private final int messageLength;
    // Content type (voice, video, fax...) of the new message.
    private final String contentType;
    // Sender of the new message.
    private final String sender;
    // Timestamp (in millis) of the new message.
    private final long msgTimeMillis;

    public SyncMessage(Bundle wrappedData) {
        syncTriggerEvent = getString(wrappedData, OmtpConstants.SYNC_TRIGGER_EVENT);
        messageId = getString(wrappedData, OmtpConstants.MESSAGE_UID);
        messageLength = getInt(wrappedData, OmtpConstants.MESSAGE_LENGTH);
        contentType = getString(wrappedData, OmtpConstants.CONTENT_TYPE);
        sender = getString(wrappedData, OmtpConstants.SENDER);
        newMessageCount = getInt(wrappedData, OmtpConstants.NUM_MESSAGE_COUNT);
        msgTimeMillis = parseTime(wrappedData.getString(OmtpConstants.TIME));
    }

    private static long parseTime(@Nullable String value) {
        if (value == null) {
            return 0L;
        }
        try {
            return new SimpleDateFormat(OmtpConstants.DATE_TIME_FORMAT, Locale.US).parse(value).getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }

    private static int getInt(Bundle wrappedData, String key) {
        String value = wrappedData.getString(key);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String getString(Bundle wrappedData, String key) {
        String value = wrappedData.getString(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    public String toString() {
        return "SyncMessage [mSyncTriggerEvent=" + syncTriggerEvent + ", mNewMessageCount=" + newMessageCount
                + ", mMessageId=" + messageId + ", mMessageLength=" + messageLength + ", mContentType=" + contentType
                + ", mSender=" + sender + ", mMsgTimeMillis=" + msgTimeMillis + "]";
    }

    /**
     * @return the event that triggered the sync message. This is a mandatory field
     * and must always be set.
     */
    public String getSyncTriggerEvent() {
        return syncTriggerEvent;
    }

    /**
     * @return the number of new messages stored on the voicemail server.
     */
    @NeededForTesting
    public int getNewMessageCount() {
        return newMessageCount;
    }

    /**
     * @return the message ID of the new message.
     * <p>
     * Expected to be set only for {@link OmtpConstants#NEW_MESSAGE}
     */
    public String getId() {
        return messageId;
    }

    /**
     * @return the content type of the new message.
     * <p>
     * Expected to be set only for {@link OmtpConstants#NEW_MESSAGE}
     */
    @NeededForTesting
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the message length of the new message.
     * <p>
     * Expected to be set only for {@link OmtpConstants#NEW_MESSAGE}
     */
    public int getLength() {
        return messageLength;
    }

    /**
     * @return the sender's phone number of the new message specified as MSISDN.
     * <p>
     * Expected to be set only for {@link OmtpConstants#NEW_MESSAGE}
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the timestamp as milliseconds for the new message.
     * <p>
     * Expected to be set only for {@link OmtpConstants#NEW_MESSAGE}
     */
    public long getTimestampMillis() {
        return msgTimeMillis;
    }
}
