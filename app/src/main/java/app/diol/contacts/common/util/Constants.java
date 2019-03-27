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

package app.diol.contacts.common.util;

public class Constants {

    /**
     * Log tag for performance measurement. To enable: adb shell setprop log.tag.ContactsPerf VERBOSE
     */
    public static final String PERFORMANCE_TAG = "ContactsPerf";

    // Used for lookup URI that contains an encoded JSON string.
    public static final String LOOKUP_URI_ENCODED = "encoded";
}
