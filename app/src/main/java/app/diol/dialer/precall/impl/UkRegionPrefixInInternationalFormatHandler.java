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

package app.diol.dialer.precall.impl;

import android.content.Context;
import android.telephony.PhoneNumberUtils;

import com.google.common.base.Optional;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.precall.impl.MalformedNumberRectifier.MalformedNumberHandler;

/**
 * It is customary in UK to present numbers as "+44 (0) xx xxxx xxxx". This is actually a amalgam of
 * international (+44 xx xxxx xxxx) and regional (0xx xxxx xxxx) format, and is in fact invalid. It
 * might be rejected depending on the carrier.
 *
 * <p>This class removes the "0" region code prefix if the first dialable digits are "+440". UK
 * short codes and region codes in international format will never start with a 0.
 */
class UkRegionPrefixInInternationalFormatHandler implements MalformedNumberHandler {

    private static final String MALFORMED_PREFIX = "+440";

    @Override
    public Optional<String> handle(Context context, String number) {
        if (!ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getBoolean("uk_region_prefix_in_international_format_fix_enabled", true)) {
            return Optional.absent();
        }
        if (!PhoneNumberUtils.normalizeNumber(number).startsWith(MALFORMED_PREFIX)) {
            return Optional.absent();
        }
        LogUtil.i("UkRegionPrefixInInternationalFormatHandler.handle", "removing (0) in UK numbers");

        // libPhoneNumber is not used because we want to keep post dial digits, and this is on the main
        // thread.
        String convertedNumber = PhoneNumberUtils.convertKeypadLettersToDigits(number);
        StringBuilder result = new StringBuilder();
        int prefixPosition = 0;
        for (int i = 0; i < convertedNumber.length(); i++) {
            char c = convertedNumber.charAt(i);
            if (c != MALFORMED_PREFIX.charAt(prefixPosition)) {
                result.append(c);
                continue;
            }
            prefixPosition++;
            if (prefixPosition == MALFORMED_PREFIX.length()) {
                result.append(convertedNumber.substring(i + 1));
                break;
            }
            result.append(c);
        }
        return Optional.of(result.toString());
    }
}
