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

package app.diol.contacts.common;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.VisibleForTesting;

/**
 * Used to create {@link CursorLoader} which finds contacts information from the strequents table.
 *
 * <p>Only returns contacts with phone numbers.
 */
public final class ContactTileLoaderFactory {

    /**
     * The _ID field returned for strequent items actually contains data._id instead of contacts._id
     * because the query is performed on the data table. In order to obtain the contact id for
     * strequent items, use Phone.contact_id instead.
     */
    @VisibleForTesting
    public static final String[] COLUMNS_PHONE_ONLY =
            new String[]{
                    Contacts._ID,
                    Contacts.DISPLAY_NAME_PRIMARY,
                    Contacts.STARRED,
                    Contacts.PHOTO_URI,
                    Contacts.LOOKUP_KEY,
                    Phone.NUMBER,
                    Phone.TYPE,
                    Phone.LABEL,
                    Phone.IS_SUPER_PRIMARY,
                    Contacts.PINNED,
                    Phone.CONTACT_ID,
                    Contacts.DISPLAY_NAME_ALTERNATIVE,
            };

    public static CursorLoader createStrequentPhoneOnlyLoader(Context context) {
        Uri uri =
                Contacts.CONTENT_STREQUENT_URI
                        .buildUpon()
                        .appendQueryParameter(ContactsContract.STREQUENT_PHONE_ONLY, "true")
                        .build();

        return new CursorLoader(context, uri, COLUMNS_PHONE_ONLY, null, null, null);
    }
}
