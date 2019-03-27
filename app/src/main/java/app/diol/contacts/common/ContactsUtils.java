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

import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ContactsUtils {

    // Telecomm related schemes are in CallUtil
    public static final long USER_TYPE_CURRENT = 0;
    public static final long USER_TYPE_WORK = 1;

    /**
     * Determine UserType from directory id and contact id.
     *
     * <p>3 types of query
     *
     * <p>1. 2 profile query: content://com.android.contacts/phone_lookup_enterprise/1234567890
     * personal and work contact are mixed into one cursor. no directory id. contact_id indicates if
     * it's work contact
     *
     * <p>2. work local query:
     * content://com.android.contacts/phone_lookup_enterprise/1234567890?directory=1000000000 either
     * directory_id or contact_id is enough to identify work contact
     *
     * <p>3. work remote query:
     * content://com.android.contacts/phone_lookup_enterprise/1234567890?directory=1000000003
     * contact_id is random. only directory_id is available
     *
     * <p>Summary: If directory_id is not null, always use directory_id to identify work contact.
     * (which is the case here) Otherwise, use contact_id.
     *
     * @param directoryId directory id of ContactsProvider query
     * @param contactId   contact id
     * @return UserType indicates the user type of the contact. A directory id or contact id larger
     * than a thredshold indicates that the contact is stored in Work Profile, but not in current
     * user. It's a contract by ContactsProvider and check by Contacts.isEnterpriseDirectoryId and
     * Contacts.isEnterpriseContactId. Currently, only 2 kinds of users can be detected from the
     * directoryId and contactId as ContactsProvider can only access current and work user's
     * contacts
     */
    public static @UserType
    long determineUserType(Long directoryId, Long contactId) {
        // First check directory id
        if (directoryId != null) {
            return Directory.isEnterpriseDirectoryId(directoryId) ? USER_TYPE_WORK : USER_TYPE_CURRENT;
        }
        // Only check contact id if directory id is null
        if (contactId != null && contactId != 0L && Contacts.isEnterpriseContactId(contactId)) {
            return USER_TYPE_WORK;
        } else {
            return USER_TYPE_CURRENT;
        }
    }

    /**
     * UserType indicates the user type of the contact. If the contact is from Work User (Work Profile
     * in Android Multi-User System), it's {@link #USER_TYPE_WORK}, otherwise, {@link
     * #USER_TYPE_CURRENT}. Please note that current user can be in work profile, where the dialer is
     * running inside Work Profile.
     */
    @Retention(RetentionPolicy.SOURCE)
    // TODO: Switch to @LongDef when @LongDef is available in the support library
    @IntDef({(int) USER_TYPE_CURRENT, (int) USER_TYPE_WORK})
    public @interface UserType {
    }
}
