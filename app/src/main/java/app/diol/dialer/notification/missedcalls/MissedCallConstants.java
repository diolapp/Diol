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

package app.diol.dialer.notification.missedcalls;

/**
 * Constants related to missed call notifications.
 */
public final class MissedCallConstants {

    /**
     * Prefix used to generate a unique tag for each missed call notification.
     */
    public static final String NOTIFICATION_TAG_PREFIX = "MissedCall_";

    /**
     * Common ID for all missed call notifications.
     */
    public static final int NOTIFICATION_ID = 1;

    /**
     * Tag for the group summary notification.
     */
    public static final String GROUP_SUMMARY_NOTIFICATION_TAG = "GroupSummary_MissedCall";

    /**
     * Key used to associate all missed call notifications and the summary as belonging to a single
     * group.
     */
    public static final String GROUP_KEY = "MissedCallGroup";
}
