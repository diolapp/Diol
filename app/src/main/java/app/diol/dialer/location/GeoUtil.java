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

package app.diol.dialer.location;

import android.content.Context;
import android.os.Trace;

/**
 * Static methods related to Geo.
 */
public class GeoUtil {

    /**
     * Return the ISO 3166-1 two letters country code of the country the user is in.
     *
     * <p>WARNING: {@link CountryDetector} caches TelephonyManager and other system services in a
     * static. {@link CountryDetector#instance} must be reset in tests.
     */
    public static String getCurrentCountryIso(Context context) {
        // The {@link CountryDetector} should never return null so this is safe to return as-is.
        Trace.beginSection("GeoUtil.getCurrentCountryIso");
        String countryIso = CountryDetector.getInstance(context).getCurrentCountryIso();
        Trace.endSection();
        return countryIso;
    }
}
