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

package app.diol.dialer.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

/**
 * Utility class for package management.
 */
public class PackageUtils {

    private static boolean isPackageInstalled(@NonNull String packageName, @NonNull Context context) {
        Assert.isNotNull(packageName);
        Assert.isNotNull(context);
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            if (info != null && info.packageName != null) {
                LogUtil.d("PackageUtils.isPackageInstalled", packageName + " is found");
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.d("PackageUtils.isPackageInstalled", packageName + " is NOT found");
        }
        return false;
    }

    /**
     * Returns true if the pkg is installed and enabled/default
     */
    public static boolean isPackageEnabled(@NonNull String packageName, @NonNull Context context) {
        Assert.isNotNull(packageName);
        Assert.isNotNull(context);
        if (isPackageInstalled(packageName, context)) {
            if (context.getPackageManager().getApplicationEnabledSetting(packageName)
                    != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                return true;
            }
        }
        return false;
    }
}
