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

package app.diol.dialer.app.contactinfo;

import android.text.TextUtils;

/**
 * Stores a phone number of a call with the country code where it originally occurred. This object
 * is used as a key in the {@code ContactInfoCache}.
 *
 * <p>The country does not necessarily specify the country of the phone number itself, but rather it
 * is the country in which the user was in when the call was placed or received.
 */
public final class NumberWithCountryIso {

    public final String number;
    public final String countryIso;

    public NumberWithCountryIso(String number, String countryIso) {
        this.number = number;
        this.countryIso = countryIso;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof NumberWithCountryIso)) {
            return false;
        }
        NumberWithCountryIso other = (NumberWithCountryIso) o;
        return TextUtils.equals(number, other.number) && TextUtils.equals(countryIso, other.countryIso);
    }

    @Override
    public int hashCode() {
        int numberHashCode = number == null ? 0 : number.hashCode();
        int countryHashCode = countryIso == null ? 0 : countryIso.hashCode();

        return numberHashCode ^ countryHashCode;
    }
}
