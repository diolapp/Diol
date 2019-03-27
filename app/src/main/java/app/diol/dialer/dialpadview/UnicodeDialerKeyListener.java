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

package app.diol.dialer.dialpadview;

import android.telephony.PhoneNumberUtils;
import android.text.Spanned;
import android.text.method.DialerKeyListener;

/**
 * {@link DialerKeyListener} with Unicode support. Converts any Unicode(e.g. Arabic) characters that
 * represent digits into digits before filtering the results so that we can support pasted digits
 * from Unicode languages.
 */
public class UnicodeDialerKeyListener extends DialerKeyListener {

    public static final UnicodeDialerKeyListener INSTANCE = new UnicodeDialerKeyListener();

    @Override
    public CharSequence filter(
            CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        final String converted =
                PhoneNumberUtils.convertKeypadLettersToDigits(
                        PhoneNumberUtils.replaceUnicodeDigits(source.toString()));
        // PhoneNumberUtils.replaceUnicodeDigits performs a character for character replacement,
        // so we can assume that start and end positions should remain unchanged.
        CharSequence result = super.filter(converted, start, end, dest, dstart, dend);
        if (result == null) {
            if (source.equals(converted)) {
                // There was no conversion or filtering performed. Just return null according to
                // the behavior of DialerKeyListener.
                return null;
            } else {
                // filter returns null if the charsequence is to be returned unchanged/unfiltered.
                // But in this case we do want to return a modified character string (even if
                // none of the characters in the modified string are filtered). So if
                // result == null we return the unfiltered but converted numeric string instead.
                return converted.subSequence(start, end);
            }
        }
        return result;
    }
}
