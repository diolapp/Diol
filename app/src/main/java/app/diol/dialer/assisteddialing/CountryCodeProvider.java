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

import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProvider;

/**
 * A class to provide the appropriate country codes related to assisted dialing.
 */
public final class CountryCodeProvider {

    // TODO(erfanian): Ensure the below standard is consistent between libphonenumber and the
    // platform.
    // ISO 3166-1 alpha-2 Country Codes that are eligible for assisted dialing.
    @VisibleForTesting
    static final List<String> DEFAULT_COUNTRY_CODES =
            Arrays.asList(
                    "CA" /* Canada */,
                    "GB" /* United Kingdom */,
                    "JP" /* Japan */,
                    "MX" /* Mexico */,
                    "US" /* United States */);

    private final Set<String> supportedCountryCodes;

    CountryCodeProvider(ConfigProvider configProvider) {
        supportedCountryCodes =
                parseConfigProviderCountryCodes(
                        configProvider.getString("assisted_dialing_csv_country_codes", ""))
                        .stream()
                        .map(v -> v.toUpperCase(Locale.US))
                        .collect(Collectors.toCollection(ArraySet::new));
        LogUtil.i(
                "CountryCodeProvider.CountryCodeProvider", "Using country codes: " + supportedCountryCodes);
    }

    /**
     * Checks whether a supplied country code is supported.
     */
    public boolean isSupportedCountryCode(String countryCode) {
        return supportedCountryCodes.contains(countryCode);
    }

    private List<String> parseConfigProviderCountryCodes(String configProviderCountryCodes) {
        if (TextUtils.isEmpty(configProviderCountryCodes)) {
            LogUtil.i(
                    "Constraints.parseConfigProviderCountryCodes",
                    "configProviderCountryCodes was empty, returning default");
            return DEFAULT_COUNTRY_CODES;
        }

        StringTokenizer tokenizer = new StringTokenizer(configProviderCountryCodes, ",");

        if (tokenizer.countTokens() < 1) {
            LogUtil.i(
                    "Constraints.parseConfigProviderCountryCodes", "insufficient provided country codes");
            return DEFAULT_COUNTRY_CODES;
        }

        List<String> parsedCountryCodes = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            String foundLocale = tokenizer.nextToken();
            if (foundLocale == null) {
                LogUtil.i(
                        "Constraints.parseConfigProviderCountryCodes",
                        "Unexpected empty value, returning default.");
                return DEFAULT_COUNTRY_CODES;
            }

            if (foundLocale.length() != 2) {
                LogUtil.i(
                        "Constraints.parseConfigProviderCountryCodes",
                        "Unexpected locale %s, returning default",
                        foundLocale);
                return DEFAULT_COUNTRY_CODES;
            }

            parsedCountryCodes.add(foundLocale);
        }
        return parsedCountryCodes;
    }
}
