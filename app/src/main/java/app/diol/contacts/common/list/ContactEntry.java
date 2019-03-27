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

package app.diol.contacts.common.list;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract.PinnedPositions;

import app.diol.dialer.contacts.ContactsComponent;

/**
 * Class to hold contact information
 */
public class ContactEntry {

    public static final ContactEntry BLANK_ENTRY = new ContactEntry();
    /**
     * Primary name for a Contact
     */
    public String namePrimary;
    /**
     * Alternative name for a Contact, e.g. last name first
     */
    public String nameAlternative;

    public String phoneLabel;
    public String phoneNumber;
    public Uri photoUri;
    public Uri lookupUri;
    public String lookupKey;
    public long id;
    public int pinned = PinnedPositions.UNPINNED;
    public boolean isFavorite = false;
    public boolean isDefaultNumber = false;

    public String getPreferredDisplayName(Context context) {
        return ContactsComponent.get(context)
                .contactDisplayPreferences()
                .getDisplayName(namePrimary, nameAlternative);
    }
}
