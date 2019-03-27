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

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Common date utilities.
 */
public class CommonDateUtils {

    // All the SimpleDateFormats in this class use the UTC timezone
    public static final SimpleDateFormat NO_YEAR_DATE_FORMAT =
            new SimpleDateFormat("--MM-dd", Locale.US);
    public static final SimpleDateFormat FULL_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final SimpleDateFormat DATE_AND_TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    public static final SimpleDateFormat NO_YEAR_DATE_AND_TIME_FORMAT =
            new SimpleDateFormat("--MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    /**
     * Exchange requires 8:00 for birthdays
     */
    public static final int DEFAULT_HOUR = 8;
}
