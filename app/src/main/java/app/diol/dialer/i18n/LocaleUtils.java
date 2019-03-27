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

import android.content.Context;
import android.os.LocaleList;

import java.util.Locale;

/**
 * Utilities for locale.
 */
public final class LocaleUtils {

    /**
     * Returns the default locale of the device.
     */
    public static Locale getLocale(Context context) {
        LocaleList localList = context.getResources().getConfiguration().getLocales();
        if (!localList.isEmpty()) {
            return localList.get(0);
        }
        return Locale.getDefault();
    }
}
