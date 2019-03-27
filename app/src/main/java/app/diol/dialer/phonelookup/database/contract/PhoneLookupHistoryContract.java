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

package app.diol.dialer.phonelookup.database.contract;

import android.net.Uri;

import app.diol.dialer.constants.Constants;

/**
 * Contract for the PhoneLookupHistory content provider.
 */
public class PhoneLookupHistoryContract {
    public static final String AUTHORITY = Constants.get().getPhoneLookupHistoryProviderAuthority();

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * PhoneLookupHistory table.
     */
    public static final class PhoneLookupHistory {

        public static final String TABLE = "PhoneLookupHistory";

        public static final String NUMBER_QUERY_PARAM = "number";

        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(PhoneLookupHistoryContract.CONTENT_URI, TABLE);
        /**
         * The MIME type of a {@link android.content.ContentProvider#getType(Uri)} single entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone_lookup_history";
        /**
         * The phone number's E164 representation if it has one, or otherwise normalized number if it
         * cannot be normalized to E164. Required, primary key for the table.
         *
         * <p>Type: TEXT
         */
        public static final String NORMALIZED_NUMBER = "normalized_number";
        /**
         * The {@link app.diol.dialer.phonelookup.PhoneLookupInfo} proto for the number. Required.
         *
         * <p>Type: BLOB
         */
        public static final String PHONE_LOOKUP_INFO = "phone_lookup_info";
        /**
         * Epoch time in milliseconds this entry was last modified. Required.
         *
         * <p>Type: INTEGER (long)
         */
        public static final String LAST_MODIFIED = "last_modified";

        /**
         * Returns a URI for a specific normalized number
         */
        public static Uri contentUriForNumber(String normalizedNumber) {
            return CONTENT_URI
                    .buildUpon()
                    .appendQueryParameter(NUMBER_QUERY_PARAM, Uri.encode(normalizedNumber))
                    .build();
        }
    }
}
