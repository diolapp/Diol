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

package app.diol.dialer.phonenumbergeoutil.impl;

import android.content.Context;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;

import java.util.Locale;

import javax.inject.Inject;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.i18n.LocaleUtils;
import app.diol.dialer.phonenumbergeoutil.PhoneNumberGeoUtil;

/**
 * Implementation of {@link PhoneNumberGeoUtil}.
 */
public class PhoneNumberGeoUtilImpl implements PhoneNumberGeoUtil {

    @Inject
    public PhoneNumberGeoUtilImpl() {
    }

    @Override
    public String getGeoDescription(Context context, String number, String countryIso) {
        LogUtil.v("PhoneNumberGeoUtilImpl.getGeoDescription", "" + LogUtil.sanitizePii(number));

        if (TextUtils.isEmpty(number)) {
            return null;
        }

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();

        Locale locale = LocaleUtils.getLocale(context);
        Phonenumber.PhoneNumber pn = null;
        try {
            LogUtil.v(
                    "PhoneNumberGeoUtilImpl.getGeoDescription",
                    "parsing '" + LogUtil.sanitizePii(number) + "' for countryIso '" + countryIso + "'...");
            pn = util.parse(number, countryIso);
            LogUtil.v(
                    "PhoneNumberGeoUtilImpl.getGeoDescription",
                    "- parsed number: " + LogUtil.sanitizePii(pn));
        } catch (NumberParseException e) {
            LogUtil.e(
                    "PhoneNumberGeoUtilImpl.getGeoDescription",
                    "getGeoDescription: NumberParseException for incoming number '"
                            + LogUtil.sanitizePii(number)
                            + "'");
        }

        if (pn != null) {
            String description = geocoder.getDescriptionForNumber(pn, locale);
            LogUtil.v(
                    "PhoneNumberGeoUtilImpl.getGeoDescription", "- got description: '" + description + "'");
            return description;
        }

        return null;
    }
}
