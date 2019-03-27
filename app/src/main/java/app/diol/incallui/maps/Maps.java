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

package app.diol.incallui.maps;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Used to create a fragment that can display a static map at the given location.
 */
public interface Maps {
    /**
     * Used to check if maps is available. This will return false if Dialer was compiled without
     * support for Google Play Services.
     */
    boolean isAvailable();

    @NonNull
    Fragment createStaticMapFragment(@NonNull Location location);
}
