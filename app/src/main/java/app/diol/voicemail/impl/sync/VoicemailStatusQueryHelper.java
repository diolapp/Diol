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
package app.diol.voicemail.impl.sync;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.VoicemailContract;
import android.provider.VoicemailContract.Status;
import android.telecom.PhoneAccountHandle;

/**
 * Construct queries to interact with the voicemail status table.
 */
public class VoicemailStatusQueryHelper {

    public static final int _ID = 0;
    public static final int CONFIGURATION_STATE = 1;
    public static final int NOTIFICATION_CHANNEL_STATE = 2;
    public static final int SOURCE_PACKAGE = 3;
    static final String[] PROJECTION = new String[]{Status._ID, // 0
            Status.CONFIGURATION_STATE, // 1
            Status.NOTIFICATION_CHANNEL_STATE, // 2
            Status.SOURCE_PACKAGE // 3
    };
    private Context context;
    private ContentResolver contentResolver;
    private Uri sourceUri;

    public VoicemailStatusQueryHelper(Context context) {
        this.context = context;
        contentResolver = context.getContentResolver();
        sourceUri = VoicemailContract.Status.buildSourceUri(this.context.getPackageName());
    }

    /**
     * Check if the configuration state for the voicemail source is "ok", meaning
     * that the source is set up.
     *
     * @param phoneAccount The phone account for the voicemail source to check.
     * @return {@code true} if the voicemail source is configured, {@code} false
     * otherwise, including if the voicemail source is not registered in the
     * table.
     */
    public boolean isVoicemailSourceConfigured(PhoneAccountHandle phoneAccount) {
        return isFieldEqualTo(phoneAccount, CONFIGURATION_STATE, Status.CONFIGURATION_STATE_OK);
    }

    /**
     * Check if the notifications channel of a voicemail source is active. That is,
     * when a new voicemail is available, if the server able to notify the device.
     *
     * @return {@code true} if notifications channel is active, {@code false}
     * otherwise.
     */
    public boolean isNotificationsChannelActive(PhoneAccountHandle phoneAccount) {
        return isFieldEqualTo(phoneAccount, NOTIFICATION_CHANNEL_STATE, Status.NOTIFICATION_CHANNEL_STATE_OK);
    }

    /**
     * Check if a field for an entry in the status table is equal to a specific
     * value.
     *
     * @param phoneAccount The phone account of the voicemail source to query for.
     * @param columnIndex  The column index of the field in the returned query.
     * @param value        The value to compare against.
     * @return {@code true} if the stored value is equal to the provided value.
     * {@code false} otherwise.
     */
    private boolean isFieldEqualTo(PhoneAccountHandle phoneAccount, int columnIndex, int value) {
        Cursor cursor = null;
        if (phoneAccount != null) {
            String phoneAccountComponentName = phoneAccount.getComponentName().flattenToString();
            String phoneAccountId = phoneAccount.getId();
            if (phoneAccountComponentName == null || phoneAccountId == null) {
                return false;
            }
            try {
                String whereClause = Status.PHONE_ACCOUNT_COMPONENT_NAME + "=? AND " + Status.PHONE_ACCOUNT_ID + "=? AND "
                        + Status.SOURCE_PACKAGE + "=?";
                String[] whereArgs = {phoneAccountComponentName, phoneAccountId, context.getPackageName()};
                cursor = contentResolver.query(sourceUri, PROJECTION, whereClause, whereArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getInt(columnIndex) == value;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return false;
    }
}
