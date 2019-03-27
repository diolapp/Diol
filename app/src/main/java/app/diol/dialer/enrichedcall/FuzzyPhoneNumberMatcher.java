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
package app.diol.dialer.enrichedcall;

import android.support.annotation.NonNull;

/**
 * Utility for comparing phone numbers.
 */
public class FuzzyPhoneNumberMatcher {

    private static final int REQUIRED_MATCHED_DIGITS = 7;

    /**
     * Returns {@code true} if the given numbers can be interpreted to be the same.
     *
     * <p>This method is called numerous times when rendering the call log. Using string methods is
     * too slow, so character by character matching is used instead.
     */
    public static boolean matches(@NonNull String lhs, @NonNull String rhs) {
        return lastSevenDigitsCharacterByCharacterMatches(lhs, rhs);
    }

    /**
     * This strategy examines the numbers character by character starting from the end. If the last
     * {@link #REQUIRED_MATCHED_DIGITS} match, it returns {@code true}.
     */
    private static boolean lastSevenDigitsCharacterByCharacterMatches(
            @NonNull String lhs, @NonNull String rhs) {
        int lhsIndex = lhs.length() - 1;
        int rhsIndex = rhs.length() - 1;

        int matchedDigits = 0;

        while (lhsIndex >= 0 && rhsIndex >= 0) {
            if (!Character.isDigit(lhs.charAt(lhsIndex))) {
                --lhsIndex;
                continue;
            }
            if (!Character.isDigit(rhs.charAt(rhsIndex))) {
                --rhsIndex;
                continue;
            }
            if (lhs.charAt(lhsIndex) != rhs.charAt(rhsIndex)) {
                break;
            }
            --lhsIndex;
            --rhsIndex;
            ++matchedDigits;
        }

        return matchedDigits >= REQUIRED_MATCHED_DIGITS;
    }
}
