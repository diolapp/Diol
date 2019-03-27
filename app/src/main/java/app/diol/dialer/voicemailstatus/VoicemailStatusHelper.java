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

package app.diol.dialer.voicemailstatus;

import android.database.Cursor;
import android.provider.VoicemailContract.Status;

/**
 * Utility used by the call log UI to determine what user message, if any, related to voicemail
 * source status needs to be shown. The messages are returned in the order of importance.
 *
 * <p>This class interacts with the voicemail content provider to fetch statuses of all the
 * registered voicemail sources and determines if any status message needs to be shown. The user of
 * this class must observe/listen to provider changes and invoke this class to check if any message
 * needs to be shown.
 */
public final class VoicemailStatusHelper {

    private VoicemailStatusHelper() {
    }

    /**
     * Returns the number of active voicemail sources installed.
     *
     * <p>The number of sources is counted by querying the voicemail status table.
     *
     * @param cursor The caller is responsible for the life cycle of the cursor and resetting the
     *               position
     */
    public static int getNumberActivityVoicemailSources(Cursor cursor) {
        int count = 0;
        if (!cursor.moveToFirst()) {
            return 0;
        }
        do {
            if (isVoicemailSourceActive(cursor)) {
                ++count;
            }
        } while (cursor.moveToNext());
        return count;
    }

    /**
     * Returns whether the source status in the cursor corresponds to an active source. A source is
     * active if its' configuration state is not NOT_CONFIGURED. For most voicemail sources, only OK
     * and NOT_CONFIGURED are used. The OMTP visual voicemail client has the same behavior pre-NMR1.
     * NMR1 visual voicemail will only set it to NOT_CONFIGURED when it is deactivated. As soon as
     * activation is attempted, it will transition into CONFIGURING then into OK or other error state,
     * NOT_CONFIGURED is never set through an error.
     */
    private static boolean isVoicemailSourceActive(Cursor cursor) {
        return cursor.getString(VoicemailStatusQuery.SOURCE_PACKAGE_INDEX) != null
                // getInt() returns 0 when null
                && !cursor.isNull(VoicemailStatusQuery.CONFIGURATION_STATE_INDEX)
                && cursor.getInt(VoicemailStatusQuery.CONFIGURATION_STATE_INDEX)
                != Status.CONFIGURATION_STATE_NOT_CONFIGURED;
    }
}
