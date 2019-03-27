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
import android.content.res.Resources;
import android.provider.ContactsContract.StatusUpdates;

import app.diol.R;

/**
 * Provides static function to get default contact status message.
 */
public class ContactStatusUtil {

    public static String getStatusString(Context context, int presence) {
        Resources resources = context.getResources();
        switch (presence) {
            case StatusUpdates.AVAILABLE:
                return resources.getString(R.string.status_available);
            case StatusUpdates.IDLE:
            case StatusUpdates.AWAY:
                return resources.getString(R.string.status_away);
            case StatusUpdates.DO_NOT_DISTURB:
                return resources.getString(R.string.status_busy);
            case StatusUpdates.OFFLINE:
            case StatusUpdates.INVISIBLE:
            default:
                return null;
        }
    }
}
