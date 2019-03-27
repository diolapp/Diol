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

package app.diol.dialer.phonenumbergeoutil.stub;

import android.content.Context;

import javax.inject.Inject;

import app.diol.dialer.phonenumbergeoutil.PhoneNumberGeoUtil;

/**
 * Stub implementation of {@link PhoneNumberGeoUtil}.
 */
public final class PhoneNumberGeoUtilStub implements PhoneNumberGeoUtil {
    @Inject
    public PhoneNumberGeoUtilStub() {
    }

    @Override
    public String getGeoDescription(Context context, String number, String countryIso) {
        return null;
    }
}
