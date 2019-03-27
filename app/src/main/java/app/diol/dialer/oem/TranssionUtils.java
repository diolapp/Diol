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

package app.diol.dialer.oem;

import android.content.Context;
import android.os.Build;
import android.support.annotation.VisibleForTesting;

import com.google.common.collect.ImmutableSet;

import app.diol.dialer.common.Assert;
import app.diol.dialer.compat.telephony.TelephonyManagerCompat;

/**
 * Utilities for Transsion devices.
 */
public final class TranssionUtils {

    @VisibleForTesting
    public static final ImmutableSet<String> TRANSSION_DEVICE_MANUFACTURERS =
            ImmutableSet.of("INFINIX MOBILITY LIMITED", "itel", "TECNO");

    @VisibleForTesting
    public static final ImmutableSet<String> TRANSSION_SECRET_CODES =
            ImmutableSet.of("*#07#", "*#87#", "*#43#", "*#2727#", "*#88#");

    private TranssionUtils() {
    }

    /**
     * Returns true if
     *
     * <ul>
     * <li>the device is a Transsion device, AND
     * <li>the input is a secret code for Transsion devices.
     * </ul>
     */
    public static boolean isTranssionSecretCode(String input) {
        return TRANSSION_DEVICE_MANUFACTURERS.contains(Build.MANUFACTURER)
                && TRANSSION_SECRET_CODES.contains(input);
    }

    /**
     * Handle a Transsion secret code by passing it to {@link
     * TelephonyManagerCompat#handleSecretCode(Context, String)}.
     *
     * <p>Before calling this method, we must use {@link #isTranssionSecretCode(String)} to ensure the
     * device is a Transsion device and the input is a valid Transsion secret code.
     *
     * <p>An exception will be thrown if either of the conditions above is not met.
     */
    public static void handleTranssionSecretCode(Context context, String input) {
        Assert.checkState(isTranssionSecretCode(input));

        TelephonyManagerCompat.handleSecretCode(context, getDigitsFromSecretCode(input));
    }

    private static String getDigitsFromSecretCode(String input) {
        // We assume a valid secret code is of format "*#{[0-9]+}#".
        Assert.checkArgument(input.length() > 3 && input.startsWith("*#") && input.endsWith("#"));

        return input.substring(2, input.length() - 1);
    }
}
