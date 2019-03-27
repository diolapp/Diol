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

package app.diol.dialer.i18n;

import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Matcher;

/**
 * A formatter that applies bidirectional formatting to phone numbers in text.
 */
public final class DialerBidiFormatter {

    /**
     * Unicode "Left-To-Right Embedding" (LRE) character.
     */
    private static final char LRE = '\u202A';

    /**
     * Unicode "Pop Directional Formatting" (PDF) character.
     */
    private static final char PDF = '\u202C';

    private DialerBidiFormatter() {
    }

    /**
     * Divides the given text into segments, applies LTR formatting and adds TTS span to segments that
     * are phone numbers, then reassembles the text.
     *
     * <p>Formatted phone numbers usually contain one or more whitespaces (e.g., "+1 650-253-0000",
     * "(650) 253-0000", etc). The system mistakes such a number for tokens separated by whitespaces.
     * Therefore, these numbers can't be correctly shown in a RTL context (e.g., "+1 650-253-0000"
     * would be shown as "650-253-0000 1+".)
     *
     * <p>This method wraps phone numbers with Unicode formatting characters LRE & PDF to ensure phone
     * numbers are always shown as LTR strings.
     *
     * <p>Note that the regex used to find phone numbers ({@link Patterns#PHONE}) will also match any
     * number. As this method also adds TTS span to segments that match {@link Patterns#PHONE}, extra
     * actions need to be taken if you don't want a number to be read as a phone number by TalkBack.
     */
    public static CharSequence format(@Nullable CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        // Find the start index and the end index of each segment matching the phone number pattern.
        Matcher matcher = Patterns.PHONE.matcher(text.toString());

        int currIndex = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Handle the case where the input text doesn't start with a phone number.
            if (currIndex < start) {
                spannableStringBuilder.append(text.subSequence(currIndex, start));
            }

            // For a phone number, wrap it with Unicode characters LRE & PDF so that it will always be
            // shown as a LTR string.
            spannableStringBuilder.append(
                    PhoneNumberUtils.createTtsSpannable(
                            TextUtils.concat(
                                    String.valueOf(LRE), text.subSequence(start, end), String.valueOf(PDF))));

            currIndex = end;
        }

        // Handle the case where the input doesn't end with a phone number.
        if (currIndex < text.length()) {
            spannableStringBuilder.append(text.subSequence(currIndex, text.length()));
        }

        return new SpannedString(spannableStringBuilder);
    }
}
