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

package app.diol.dialer.phonenumberproto;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.MatchType;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.ShortNumberInfo;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Wrapper for selected methods in {@link PhoneNumberUtil} which uses the {@link DialerPhoneNumber}
 * lite proto instead of the {@link com.google.i18n.phonenumbers.Phonenumber.PhoneNumber} POJO.
 *
 * <p>All methods should be called on a worker thread.
 */
public class DialerPhoneNumberUtil {
    private final PhoneNumberUtil phoneNumberUtil;
    private final ShortNumberInfo shortNumberInfo;

    @WorkerThread
    public DialerPhoneNumberUtil() {
        Assert.isWorkerThread();
        this.phoneNumberUtil = PhoneNumberUtil.getInstance();
        this.shortNumberInfo = ShortNumberInfo.getInstance();
    }

    /**
     * Parses the provided raw phone number into a {@link DialerPhoneNumber}.
     *
     * @see PhoneNumberUtil#parse(CharSequence, String)
     */
    @WorkerThread
    public DialerPhoneNumber parse(@Nullable String numberToParse, @Nullable String defaultRegion) {
        Assert.isWorkerThread();

        DialerPhoneNumber.Builder dialerPhoneNumber = DialerPhoneNumber.newBuilder();

        if (defaultRegion != null) {
            dialerPhoneNumber.setCountryIso(defaultRegion);
        }

        // Numbers can be null or empty for incoming "unknown" calls.
        if (numberToParse == null) {
            return dialerPhoneNumber.build();
        }

        // If the number is a service number, just store the raw number and don't bother trying to parse
        // it. PhoneNumberUtil#parse ignores these characters which can lead to confusing behavior, such
        // as the numbers "#123" and "123" being considered the same. The "#" can appear in the middle
        // of a service number and the "*" can appear at the beginning (see a bug).
        if (isServiceNumber(numberToParse)) {
            return dialerPhoneNumber.setNormalizedNumber(numberToParse).build();
        }

        String postDialPortion = PhoneNumberUtils.extractPostDialPortion(numberToParse);
        if (!postDialPortion.isEmpty()) {
            dialerPhoneNumber.setPostDialPortion(postDialPortion);
        }

        String networkPortion = PhoneNumberUtils.extractNetworkPortion(numberToParse);

        try {
            PhoneNumber phoneNumber = phoneNumberUtil.parse(networkPortion, defaultRegion);
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                String validNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.E164);
                if (TextUtils.isEmpty(validNumber)) {
                    throw new IllegalStateException(
                            "e164 number should not be empty: " + LogUtil.sanitizePii(numberToParse));
                }
                // The E164 representation doesn't contain post-dial digits, but we need to preserve them.
                if (!postDialPortion.isEmpty()) {
                    validNumber += postDialPortion;
                }
                return dialerPhoneNumber.setNormalizedNumber(validNumber).setIsValid(true).build();
            }
        } catch (NumberParseException e) {
            // fall through
        }
        return dialerPhoneNumber.setNormalizedNumber(networkPortion + postDialPortion).build();
    }

    /**
     * Returns true if the two numbers:
     *
     * <ul>
     * <li>were parseable by libphonenumber (see {@link #parse(String, String)}),
     * <li>are a {@link MatchType#SHORT_NSN_MATCH}, {@link MatchType#NSN_MATCH}, or {@link
     * MatchType#EXACT_MATCH}, and
     * <li>have the same post-dial digits.
     * </ul>
     *
     * <p>If either number is not parseable, returns true if their raw inputs have the same network
     * and post-dial portions.
     *
     * <p>An empty number is never considered to match another number.
     *
     * @see PhoneNumberUtil#isNumberMatch(PhoneNumber, PhoneNumber)
     */
    @WorkerThread
    public boolean isMatch(
            @NonNull DialerPhoneNumber firstNumberIn, @NonNull DialerPhoneNumber secondNumberIn) {
        Assert.isWorkerThread();

        // An empty number should not be combined with any other number.
        if (firstNumberIn.getNormalizedNumber().isEmpty()
                || secondNumberIn.getNormalizedNumber().isEmpty()) {
            return false;
        }

        // Two numbers with different countries should not match.
        if (!firstNumberIn.getCountryIso().equals(secondNumberIn.getCountryIso())) {
            return false;
        }

        PhoneNumber phoneNumber1 = null;
        try {
            phoneNumber1 =
                    phoneNumberUtil.parse(firstNumberIn.getNormalizedNumber(), firstNumberIn.getCountryIso());
        } catch (NumberParseException e) {
            // fall through
        }

        PhoneNumber phoneNumber2 = null;
        try {
            phoneNumber2 =
                    phoneNumberUtil.parse(
                            secondNumberIn.getNormalizedNumber(), secondNumberIn.getCountryIso());
        } catch (NumberParseException e) {
            // fall through
        }

        // If either number is a service number or either number can't be parsed by libphonenumber, just
        // fallback to basic textual matching.
        if (isServiceNumber(firstNumberIn.getNormalizedNumber())
                || isServiceNumber(secondNumberIn.getNormalizedNumber())
                || phoneNumber1 == null
                || phoneNumber2 == null) {
            return firstNumberIn.getNormalizedNumber().equals(secondNumberIn.getNormalizedNumber());
        }

        // Both numbers are parseable, first check for short codes to so that a number like "5555"
        // doesn't match "55555" (due to those being a SHORT_NSN_MATCH below).
        if (shortNumberInfo.isPossibleShortNumber(phoneNumber1)
                || shortNumberInfo.isPossibleShortNumber(phoneNumber2)) {
            return firstNumberIn.getNormalizedNumber().equals(secondNumberIn.getNormalizedNumber());
        }

        // Both numbers are parseable, use more sophisticated libphonenumber matching.
        MatchType matchType = phoneNumberUtil.isNumberMatch(phoneNumber1, phoneNumber2);

        return (matchType == MatchType.SHORT_NSN_MATCH
                || matchType == MatchType.NSN_MATCH
                || matchType == MatchType.EXACT_MATCH)
                && firstNumberIn.getPostDialPortion().equals(secondNumberIn.getPostDialPortion());
    }

    private boolean isServiceNumber(@NonNull String rawNumber) {
        return rawNumber.contains("#") || rawNumber.startsWith("*");
    }
}
