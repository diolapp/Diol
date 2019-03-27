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
import android.support.annotation.Nullable;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.telecom.TelecomUtil;

/**
 * Methods to help extract {@code PhoneAccount} information from database and Telecomm sources.
 */
public class PhoneAccountUtils {

    /**
     * Extract account label from PhoneAccount object.
     */
    @Nullable
    public static String getAccountLabel(
            Context context, @Nullable PhoneAccountHandle accountHandle) {
        PhoneAccount account = getAccountOrNull(context, accountHandle);
        if (account != null && account.getLabel() != null) {
            return account.getLabel().toString();
        }
        return null;
    }

    /**
     * Extract account color from PhoneAccount object.
     */
    public static int getAccountColor(Context context, @Nullable PhoneAccountHandle accountHandle) {
        final PhoneAccount account = TelecomUtil.getPhoneAccount(context, accountHandle);

        // For single-sim devices the PhoneAccount will be NO_HIGHLIGHT_COLOR by default, so it is
        // safe to always use the account highlight color.
        return account == null ? PhoneAccount.NO_HIGHLIGHT_COLOR : account.getHighlightColor();
    }

    /**
     * Determine whether a phone account supports call subjects.
     *
     * @return {@code true} if call subjects are supported, {@code false} otherwise.
     */
    public static boolean getAccountSupportsCallSubject(
            Context context, @Nullable PhoneAccountHandle accountHandle) {
        final PhoneAccount account = TelecomUtil.getPhoneAccount(context, accountHandle);

        return account != null && account.hasCapabilities(PhoneAccount.CAPABILITY_CALL_SUBJECT);
    }

    /**
     * Retrieve the account metadata, but if the account does not exist or the device has only a
     * single registered and enabled account, return null.
     */
    @Nullable
    private static PhoneAccount getAccountOrNull(
            Context context, @Nullable PhoneAccountHandle accountHandle) {
        if (TelecomUtil.getCallCapablePhoneAccounts(context).size() <= 1) {
            return null;
        }
        return TelecomUtil.getPhoneAccount(context, accountHandle);
    }
}
