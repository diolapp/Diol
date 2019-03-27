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

package app.diol.dialer.callintent;

/**
 * Constants used to construct and parse call intents. These should never be made public.
 */
/* package */ class Constants {
    // This is a Dialer extra that is set for outgoing calls and used by the InCallUI.
    /* package */ static final String EXTRA_CALL_SPECIFIC_APP_DATA =
            "app.diol.dialer.callintent.CALL_SPECIFIC_APP_DATA";

    // This is a hidden system extra. For outgoing calls Dialer sets it and parses it but for incoming
    // calls Telecom sets it and Dialer parses it.
    /* package */ static final String EXTRA_CALL_CREATED_TIME_MILLIS =
            "android.telecom.extra.CALL_CREATED_TIME_MILLIS";

    private Constants() {
    }
}
