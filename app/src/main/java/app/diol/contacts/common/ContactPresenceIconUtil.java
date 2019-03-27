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
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.StatusUpdates;
import android.support.v4.content.ContextCompat;

/**
 * Define the contact present show policy in Contacts
 */
public class ContactPresenceIconUtil {

    /**
     * Get the presence icon resource according the status.
     *
     * @return null means don't show the status icon.
     */
    public static Drawable getPresenceIcon(Context context, int status) {
        // We don't show the offline status in Contacts
        switch (status) {
            case StatusUpdates.AVAILABLE:
            case StatusUpdates.IDLE:
            case StatusUpdates.AWAY:
            case StatusUpdates.DO_NOT_DISTURB:
            case StatusUpdates.INVISIBLE:
                return ContextCompat.getDrawable(context, StatusUpdates.getPresenceIconResourceId(status));
            case StatusUpdates.OFFLINE:
                // The undefined status is treated as OFFLINE in getPresenceIconResourceId();
            default:
                return null;
        }
    }
}
