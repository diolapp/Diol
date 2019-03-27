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

package app.diol.dialer.calllog.constants;

/**
 * A class containing names for call log intents.
 */
public final class IntentNames {

    public static final String ACTION_REFRESH_ANNOTATED_CALL_LOG = "refresh_annotated_call_log";

    public static final String ACTION_CANCEL_REFRESHING_ANNOTATED_CALL_LOG =
            "cancel_refreshing_annotated_call_log";

    public static final String EXTRA_CHECK_DIRTY = "check_dirty";

    private IntentNames() {
    }
}
