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

package app.diol.dialer.smartdial.util;

import java.util.ArrayList;

import app.diol.dialer.common.LogUtil;

/**
 * Stores information about a range of characters matched in a display name The integers start and
 * end indicate that the range start to end (exclusive) correspond to some characters in the query.
 * Used to highlight certain parts of the contact's display name to indicate that those ranges
 * matched the user's query.
 */
public class SmartDialMatchPosition {

    private static final String TAG = SmartDialMatchPosition.class.getSimpleName();

    public int start;
    public int end;

    public SmartDialMatchPosition(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Used by {@link SmartDialNameMatcher} to advance the positions of a match position found in a
     * sub query.
     *
     * @param inList    ArrayList of SmartDialMatchPositions to modify.
     * @param toAdvance Offset to modify by.
     */
    public static void advanceMatchPositions(
            ArrayList<SmartDialMatchPosition> inList, int toAdvance) {
        for (int i = 0; i < inList.size(); i++) {
            inList.get(i).advance(toAdvance);
        }
    }

    /**
     * Used mainly for debug purposes. Displays contents of an ArrayList of SmartDialMatchPositions.
     *
     * @param list ArrayList of SmartDialMatchPositions to print out in a human readable fashion.
     */
    public static void print(ArrayList<SmartDialMatchPosition> list) {
        for (int i = 0; i < list.size(); i++) {
            SmartDialMatchPosition m = list.get(i);
            LogUtil.d(TAG, "[" + m.start + "," + m.end + "]");
        }
    }

    private void advance(int toAdvance) {
        this.start += toAdvance;
        this.end += toAdvance;
    }
}
