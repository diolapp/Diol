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

import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.util.Optional;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.strictmode.StrictModeUtils;

/**
 * Responsible for transforming numbers to make them dialable and valid when roaming.
 */
final class NumberTransformer {

    private final PhoneNumberUtil phoneNumberUtil;
    private final Constraints constraints;

    NumberTransformer(Constraints constraints) {
        this.constraints = constraints;
        this.phoneNumberUtil = StrictModeUtils.bypass(PhoneNumberUtil::getInstance);
    }

    /**
     * A method to do assisted dialing transformations.
     *
     * <p>The library will do its best to attempt a transformation, but, if for any reason the
     * transformation fails, we return an empty optional. The operation can be considered a success
     * when the Optional we return has a value set.
     */
    Optional<TransformationInfo> doAssistedDialingTransformation(
            String numbertoTransform, String userHomeCountryCode, String userRoamingCountryCode) {

        if (!constraints.meetsPreconditions(
                numbertoTransform, userHomeCountryCode, userRoamingCountryCode)) {
            LogUtil.i(
                    "NumberTransformer.doAssistedDialingTransformation",
                    "assisted dialing failed preconditions");
            return Optional.empty();
        }

        PhoneNumber phoneNumber =
                StrictModeUtils.bypass(
                        () -> {
                            try {
                                return phoneNumberUtil.parse(numbertoTransform, userHomeCountryCode);
                            } catch (NumberParseException e) {
                                LogUtil.i(
                                        "NumberTransformer.doAssistedDialingTransformation", "number failed to parse");
                                return null;
                            }
                        });

        if (phoneNumber == null) {
            return Optional.empty();
        }

        String transformedNumber =
                StrictModeUtils.bypass(
                        () ->
                                phoneNumberUtil.formatNumberForMobileDialing(
                                        phoneNumber, userRoamingCountryCode, true));

        // formatNumberForMobileDialing may return an empty String.
        if (TextUtils.isEmpty(transformedNumber)) {
            LogUtil.i(
                    "NumberTransformer.doAssistedDialingTransformation",
                    "formatNumberForMobileDialing returned an empty string");
            return Optional.empty();
        }

        // TODO Verify the transformed number is still valid?
        return Optional.of(
                TransformationInfo.builder()
                        .setOriginalNumber(numbertoTransform)
                        .setTransformedNumber(transformedNumber)
                        .setUserHomeCountryCode(userHomeCountryCode)
                        .setUserRoamingCountryCode(userRoamingCountryCode)
                        .setTransformedNumberCountryCallingCode(phoneNumber.getCountryCode())
                        .build());
    }
}
