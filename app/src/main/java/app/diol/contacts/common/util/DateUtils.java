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

import android.text.format.Time;

/**
 * Utility methods for processing dates.
 */
public class DateUtils {

    /**
     * Determine the difference, in days between two dates. Uses similar logic as the {@link
     * android.text.format.DateUtils.getRelativeTimeSpanString} method.
     *
     * @param time  Instance of time object to use for calculations.
     * @param date1 First date to check.
     * @param date2 Second date to check.
     * @return The absolute difference in days between the two dates.
     */
    public static int getDayDifference(Time time, long date1, long date2) {
        time.set(date1);
        int startDay = Time.getJulianDay(date1, time.gmtoff);

        time.set(date2);
        int currentDay = Time.getJulianDay(date2, time.gmtoff);

        return Math.abs(currentDay - startDay);
    }
}
