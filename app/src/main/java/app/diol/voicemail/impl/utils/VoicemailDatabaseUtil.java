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

package app.diol.voicemail.impl.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.VoicemailContract.Voicemails;
import android.telecom.PhoneAccountHandle;

import java.util.List;

import app.diol.voicemail.impl.Voicemail;

public class VoicemailDatabaseUtil {

    /**
     * Inserts a new voicemail into the voicemail content provider.
     *
     * @param context   The context of the app doing the inserting
     * @param voicemail Data to be inserted
     * @return {@link Uri} of the newly inserted {@link Voicemail}
     * @hide
     */
    public static Uri insert(Context context, Voicemail voicemail) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = getContentValues(voicemail);
        return contentResolver.insert(Voicemails.buildSourceUri(context.getPackageName()), contentValues);
    }

    /**
     * Inserts a list of voicemails into the voicemail content provider.
     *
     * @param context    The context of the app doing the inserting
     * @param voicemails Data to be inserted
     * @return the number of voicemails inserted
     * @hide
     */
    public static int insert(Context context, List<Voicemail> voicemails) {
        for (Voicemail voicemail : voicemails) {
            insert(context, voicemail);
        }
        return voicemails.size();
    }

    /**
     * Maps structured {@link Voicemail} to {@link ContentValues} in content
     * provider.
     */
    private static ContentValues getContentValues(Voicemail voicemail) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Voicemails.DATE, String.valueOf(voicemail.getTimestampMillis()));
        contentValues.put(Voicemails.NUMBER, voicemail.getNumber());
        contentValues.put(Voicemails.DURATION, String.valueOf(voicemail.getDuration()));
        contentValues.put(Voicemails.SOURCE_PACKAGE, voicemail.getSourcePackage());
        contentValues.put(Voicemails.SOURCE_DATA, voicemail.getSourceData());
        contentValues.put(Voicemails.IS_READ, voicemail.isRead() ? 1 : 0);
        contentValues.put(Voicemails.IS_OMTP_VOICEMAIL, 1);

        PhoneAccountHandle phoneAccount = voicemail.getPhoneAccount();
        if (phoneAccount != null) {
            contentValues.put(Voicemails.PHONE_ACCOUNT_COMPONENT_NAME, phoneAccount.getComponentName().flattenToString());
            contentValues.put(Voicemails.PHONE_ACCOUNT_ID, phoneAccount.getId());
        }

        if (voicemail.getTranscription() != null) {
            contentValues.put(Voicemails.TRANSCRIPTION, voicemail.getTranscription());
        }

        return contentValues;
    }
}
