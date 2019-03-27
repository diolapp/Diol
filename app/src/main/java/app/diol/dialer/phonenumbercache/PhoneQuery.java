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

package app.diol.dialer.phonenumbercache;

import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;

/**
 * The queries to look up the {@link ContactInfo} for a given number in the Call Log.
 */
final class PhoneQuery {

    static final int PERSON_ID = 0;
    static final int NAME = 1;
    static final int PHONE_TYPE = 2;
    static final int LABEL = 3;
    static final int MATCHED_NUMBER = 4;
    static final int NORMALIZED_NUMBER = 5;
    static final int PHOTO_ID = 6;
    static final int LOOKUP_KEY = 7;
    static final int PHOTO_URI = 8;
    /**
     * Projection to look up a contact's DISPLAY_NAME_ALTERNATIVE
     */
    static final String[] DISPLAY_NAME_ALTERNATIVE_PROJECTION =
            new String[]{
                    Contacts.DISPLAY_NAME_ALTERNATIVE,
            };

    static final int NAME_ALTERNATIVE = 0;

    static final String[] ADDITIONAL_CONTACT_INFO_PROJECTION =
            new String[]{Phone.DISPLAY_NAME_ALTERNATIVE, Phone.CARRIER_PRESENCE};
    static final int ADDITIONAL_CONTACT_INFO_DISPLAY_NAME_ALTERNATIVE = 0;
    static final int ADDITIONAL_CONTACT_INFO_CARRIER_PRESENCE = 1;

    /**
     * Projection to look up the ContactInfo. Does not include DISPLAY_NAME_ALTERNATIVE as that column
     * isn't available in ContactsCommon.PhoneLookup. We should always use this projection starting
     * from NYC onward.
     */
    private static final String[] PHONE_LOOKUP_PROJECTION =
            new String[]{
                    PhoneLookup.CONTACT_ID,
                    PhoneLookup.DISPLAY_NAME,
                    PhoneLookup.TYPE,
                    PhoneLookup.LABEL,
                    PhoneLookup.NUMBER,
                    PhoneLookup.NORMALIZED_NUMBER,
                    PhoneLookup.PHOTO_ID,
                    PhoneLookup.LOOKUP_KEY,
                    PhoneLookup.PHOTO_URI
            };

    static String[] getPhoneLookupProjection() {
        return PHONE_LOOKUP_PROJECTION;
    }
}
