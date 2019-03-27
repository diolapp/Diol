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

package app.diol.dialer.assisteddialing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Optional;

import app.diol.dialer.common.LogUtil;

// TODO(erfanian): Improve definition of roaming and home country in finalized API.

/**
 * LocationDetector is responsible for determining the Roaming location of the User, in addition to
 * User's home country.
 */
final class LocationDetector {

    private final TelephonyManager telephonyManager;
    private final String userProvidedHomeCountry;

    LocationDetector(
            @NonNull TelephonyManager telephonyManager, @Nullable String userProvidedHomeCountry) {
        if (telephonyManager == null) {
            throw new NullPointerException("Provided TelephonyManager was null");
        }

        this.telephonyManager = telephonyManager;
        this.userProvidedHomeCountry = userProvidedHomeCountry;
    }

    // TODO(erfanian):  confirm this is based on ISO 3166-1 alpha-2. libphonenumber expects Unicode's
    // CLDR
    // TODO(erfanian):  confirm these are still valid in a multi-sim environment.

    /**
     * Returns what we believe to be the User's home country. This should resolve to
     * PROPERTY_ICC_OPERATOR_ISO_COUNTRY
     */
    Optional<String> getUpperCaseUserHomeCountry() {

        if (!TextUtils.isEmpty(userProvidedHomeCountry)) {
            LogUtil.i(
                    "LocationDetector.getUpperCaseUserRoamingCountry", "user provided home country code");
            return Optional.of(userProvidedHomeCountry.toUpperCase(Locale.US));
        }

        String simCountryIso = telephonyManager.getSimCountryIso();
        if (simCountryIso != null) {
            LogUtil.i("LocationDetector.getUpperCaseUserRoamingCountry", "using sim country iso");
            return Optional.of(telephonyManager.getSimCountryIso().toUpperCase(Locale.US));
        }
        LogUtil.i("LocationDetector.getUpperCaseUserHomeCountry", "user home country was null");
        return Optional.empty();
    }

    /**
     * Returns what we believe to be the User's current (roaming) country
     */
    Optional<String> getUpperCaseUserRoamingCountry() {
        // TODO Increase coverage of location resolution??
        String networkCountryIso = telephonyManager.getNetworkCountryIso();
        if (networkCountryIso != null) {
            return Optional.of(telephonyManager.getNetworkCountryIso().toUpperCase(Locale.US));
        }
        LogUtil.i("LocationDetector.getUpperCaseUserRoamingCountry", "user roaming country was null");
        return Optional.empty();
    }
}
