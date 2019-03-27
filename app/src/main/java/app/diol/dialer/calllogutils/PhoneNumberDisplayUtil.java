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

package app.diol.dialer.calllogutils;

import android.content.Context;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;

import com.google.common.base.Optional;

import app.diol.R;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;

/**
 * Helper for formatting and managing the display of phone numbers.
 */
public class PhoneNumberDisplayUtil {

    /**
     * Returns the string to display for the given phone number if there is no matching contact.
     */
    public static CharSequence getDisplayName(
            Context context, CharSequence number, int presentation, boolean isVoicemail) {
        Optional<String> presentationString = getNameForPresentation(context, presentation);
        if (presentationString.isPresent()) {
            return presentationString.get();
        }
        if (isVoicemail) {
            return context.getResources().getString(R.string.voicemail_string);
        }
        if (PhoneNumberHelper.isLegacyUnknownNumbers(number)) {
            return context.getResources().getString(R.string.unknown);
        }
        return "";
    }

    /**
     * Returns the string associated with the given presentation.
     */
    public static Optional<String> getNameForPresentation(Context appContext, int presentation) {
        if (presentation == Calls.PRESENTATION_UNKNOWN) {
            return Optional.of(appContext.getResources().getString(R.string.unknown));
        }
        if (presentation == Calls.PRESENTATION_RESTRICTED) {
            return Optional.of(PhoneNumberHelper.getDisplayNameForRestrictedNumber(appContext));
        }
        if (presentation == Calls.PRESENTATION_PAYPHONE) {
            return Optional.of(appContext.getResources().getString(R.string.payphone));
        }
        return Optional.absent();
    }

    /**
     * Returns the string to display for the given phone number.
     *
     * @param number          the number to display
     * @param formattedNumber the formatted number if available, may be null
     */
    static CharSequence getDisplayNumber(
            Context context,
            CharSequence number,
            int presentation,
            CharSequence formattedNumber,
            CharSequence postDialDigits,
            boolean isVoicemail) {
        final CharSequence displayName = getDisplayName(context, number, presentation, isVoicemail);
        if (!TextUtils.isEmpty(displayName)) {
            return getTtsSpannableLtrNumber(displayName);
        }

        if (!TextUtils.isEmpty(formattedNumber)) {
            return getTtsSpannableLtrNumber(formattedNumber);
        } else if (!TextUtils.isEmpty(number)) {
            return getTtsSpannableLtrNumber(number.toString() + postDialDigits);
        } else {
            return context.getResources().getString(R.string.unknown);
        }
    }

    /**
     * Returns number annotated as phone number in LTR direction.
     */
    private static CharSequence getTtsSpannableLtrNumber(CharSequence number) {
        return PhoneNumberUtils.createTtsSpannable(
                BidiFormatter.getInstance().unicodeWrap(number.toString(), TextDirectionHeuristics.LTR));
    }
}
