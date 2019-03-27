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

package app.diol.contacts.common.compat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccount;

/**
 * Compatiblity class for {@link android.telecom.PhoneAccount}
 */
public class PhoneAccountCompat {

    /**
     * Builds and returns an icon {@code Drawable} to represent this {@code PhoneAccount} in a user
     * interface.
     *
     * @param phoneAccount the PhoneAccount from which to build the icon.
     * @param context      A {@code Context} to use for loading Drawables.
     * @return An icon for this PhoneAccount, or null
     */
    @Nullable
    public static Drawable createIconDrawable(
            @Nullable PhoneAccount phoneAccount, @Nullable Context context) {
        if (phoneAccount == null || context == null) {
            return null;
        }
        return createIconDrawableMarshmallow(phoneAccount, context);
    }

    @Nullable
    private static Drawable createIconDrawableMarshmallow(
            PhoneAccount phoneAccount, Context context) {
        Icon accountIcon = phoneAccount.getIcon();
        if (accountIcon == null) {
            return null;
        }
        return accountIcon.loadDrawable(context);
    }
}
