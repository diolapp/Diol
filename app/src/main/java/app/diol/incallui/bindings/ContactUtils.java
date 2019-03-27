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

package app.diol.incallui.bindings;

import android.location.Address;
import android.util.Pair;

import java.util.Calendar;
import java.util.List;

/**
 * Utility functions to help manipulate contact data.
 */
public interface ContactUtils {

    boolean retrieveContactInteractionsFromLookupKey(String lookupKey, Listener listener);

    interface Listener {

        void onContactInteractionsFound(Address address, List<Pair<Calendar, Calendar>> openingHours);
    }
}
