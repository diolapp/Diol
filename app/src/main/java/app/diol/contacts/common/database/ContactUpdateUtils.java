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

package app.diol.contacts.common.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Static methods to update contact information.
 */
public class ContactUpdateUtils {

    private static final String TAG = ContactUpdateUtils.class.getSimpleName();

    public static void setSuperPrimary(Context context, long dataId) {
        if (dataId == -1) {
            Log.e(TAG, "Invalid arguments for setSuperPrimary request");
            return;
        }

        // Update the primary values in the data record.
        ContentValues values = new ContentValues(2);
        values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
        values.put(ContactsContract.Data.IS_PRIMARY, 1);

        context
                .getContentResolver()
                .update(
                        ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, dataId),
                        values,
                        null,
                        null);
    }
}
