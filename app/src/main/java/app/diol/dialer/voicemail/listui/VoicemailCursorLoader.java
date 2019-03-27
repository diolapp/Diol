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

package app.diol.dialer.voicemail.listui;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.NumberAttributes;
import app.diol.dialer.calllog.database.contract.AnnotatedCallLogContract.AnnotatedCallLog;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.voicemail.model.VoicemailEntry;

/**
 * CursorLoader for the annotated call log (voicemails only).
 */
final class VoicemailCursorLoader extends CursorLoader {

    // When adding columns be sure to update {@link #VoicemailCursorLoader.toVoicemailEntry}.
    public static final String[] VOICEMAIL_COLUMNS =
            new String[]{
                    AnnotatedCallLog._ID,
                    AnnotatedCallLog.TIMESTAMP,
                    AnnotatedCallLog.NUMBER,
                    AnnotatedCallLog.FORMATTED_NUMBER,
                    AnnotatedCallLog.DURATION,
                    AnnotatedCallLog.GEOCODED_LOCATION,
                    AnnotatedCallLog.CALL_TYPE,
                    AnnotatedCallLog.TRANSCRIPTION,
                    AnnotatedCallLog.VOICEMAIL_URI,
                    AnnotatedCallLog.IS_READ,
                    AnnotatedCallLog.NUMBER_ATTRIBUTES,
                    AnnotatedCallLog.TRANSCRIPTION_STATE,
                    AnnotatedCallLog.PHONE_ACCOUNT_COMPONENT_NAME,
                    AnnotatedCallLog.PHONE_ACCOUNT_ID,
            };

    // Indexes for VOICEMAIL_COLUMNS
    private static final int ID = 0;
    private static final int TIMESTAMP = 1;
    private static final int NUMBER = 2;
    private static final int FORMATTED_NUMBER = 3;
    private static final int DURATION = 4;
    private static final int GEOCODED_LOCATION = 5;
    private static final int CALL_TYPE = 6;
    private static final int TRANSCRIPTION = 7;
    private static final int VOICEMAIL_URI = 8;
    private static final int IS_READ = 9;
    private static final int NUMBER_ATTRIBUTES = 10;
    private static final int TRANSCRIPTION_STATE = 11;
    private static final int PHONE_ACCOUNT_COMPONENT_NAME = 12;
    private static final int PHONE_ACCOUNT_ID = 13;

    // TODO(zachh): Optimize indexes
    VoicemailCursorLoader(Context context) {
        super(
                context,
                AnnotatedCallLog.CONTENT_URI,
                VOICEMAIL_COLUMNS,
                AnnotatedCallLog.CALL_TYPE + " = ?",
                new String[]{Integer.toString(Calls.VOICEMAIL_TYPE)},
                AnnotatedCallLog.TIMESTAMP + " DESC");
    }

    /**
     * Creates a new {@link VoicemailEntry} from the provided cursor using the current position.
     */
    static VoicemailEntry toVoicemailEntry(Cursor cursor) {
        DialerPhoneNumber number;
        try {
            number = DialerPhoneNumber.parseFrom(cursor.getBlob(NUMBER));
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("Couldn't parse DialerPhoneNumber bytes");
        }
        NumberAttributes numberAttributes;
        try {
            numberAttributes = NumberAttributes.parseFrom(cursor.getBlob(NUMBER_ATTRIBUTES));
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("Couldn't parse NumberAttributes bytes");
        }

        // Voicemail numbers should always be valid so the CP2 information should never be incomplete,
        // and there should be no need to query PhoneLookup at render time.
        Assert.checkArgument(
                !numberAttributes.getIsCp2InfoIncomplete(),
                "CP2 info incomplete for number: %s",
                LogUtil.sanitizePii(number.getNormalizedNumber()));

        VoicemailEntry.Builder voicemailEntryBuilder =
                VoicemailEntry.newBuilder()
                        .setId(cursor.getInt(ID))
                        .setTimestamp(cursor.getLong(TIMESTAMP))
                        .setNumber(number)
                        .setDuration(cursor.getLong(DURATION))
                        .setCallType(cursor.getInt(CALL_TYPE))
                        .setIsRead(cursor.getInt(IS_READ))
                        .setNumberAttributes(numberAttributes)
                        .setTranscriptionState(cursor.getInt(TRANSCRIPTION_STATE));

        String formattedNumber = cursor.getString(FORMATTED_NUMBER);
        if (!TextUtils.isEmpty(formattedNumber)) {
            voicemailEntryBuilder.setFormattedNumber(formattedNumber);
        }

        String geocodedLocation = cursor.getString(GEOCODED_LOCATION);
        if (!TextUtils.isEmpty(geocodedLocation)) {
            voicemailEntryBuilder.setGeocodedLocation(geocodedLocation);
        }

        String transcription = cursor.getString(TRANSCRIPTION);
        if (!TextUtils.isEmpty(transcription)) {
            voicemailEntryBuilder.setTranscription(transcription);
        }

        String voicemailUri = cursor.getString(VOICEMAIL_URI);
        if (!TextUtils.isEmpty(voicemailUri)) {
            voicemailEntryBuilder.setVoicemailUri(voicemailUri);
        }

        String phoneAccountComponentName = cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME);
        if (!TextUtils.isEmpty(phoneAccountComponentName)) {
            voicemailEntryBuilder.setPhoneAccountComponentName(phoneAccountComponentName);
        }

        String phoneAccountId = cursor.getString(PHONE_ACCOUNT_ID);
        if (!TextUtils.isEmpty(phoneAccountId)) {
            voicemailEntryBuilder.setPhoneAccountId(phoneAccountId);
        }

        return voicemailEntryBuilder.build();
    }

    static long getTimestamp(Cursor cursor) {
        return cursor.getLong(TIMESTAMP);
    }
}
