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

/**
 * Superclass for a helper class to get the current location and distance to other locations.
 */
public interface DistanceHelper {

    float DISTANCE_NOT_FOUND = -1;
    float MILES_PER_METER = (float) 0.000621371192;
    float KILOMETERS_PER_METER = (float) 0.001;

    void cleanUp();

    float calculateDistance(Address address);

    interface Listener {

        void onLocationReady();
    }
}
