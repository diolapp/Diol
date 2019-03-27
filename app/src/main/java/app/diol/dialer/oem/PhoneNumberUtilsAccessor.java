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
import android.telephony.PhoneNumberUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides access to hidden APIs in {@link android.telephony.PhoneNumberUtils}.
 */
public final class PhoneNumberUtilsAccessor {

    /**
     * Checks if a given number is an emergency number for the country that the user is in.
     *
     * @param subId   the subscription ID of the SIM
     * @param number  the number to check
     * @param context the specific context which the number should be checked against
     * @return true if the specified number is an emergency number for the country the user is
     * currently in.
     */
    public static boolean isLocalEmergencyNumber(Context context, int subId, String number) {
        try {
            Method method =
                    PhoneNumberUtils.class.getMethod(
                            "isLocalEmergencyNumber", Context.class, int.class, String.class);
            return (boolean) method.invoke(null, context, subId, number);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
