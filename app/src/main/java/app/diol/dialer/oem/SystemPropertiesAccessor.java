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

import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import app.diol.dialer.common.LogUtil;

/**
 * Hacky way to call the hidden SystemProperties class API. Needed to get the real value of
 * ro.carrier and some other values.
 */
class SystemPropertiesAccessor {
    private Method systemPropertiesGetMethod;

    @SuppressWarnings("PrivateApi")
    public String get(String name) {
        Method systemPropertiesGetMethod = getSystemPropertiesGetMethod();
        if (systemPropertiesGetMethod == null) {
            return null;
        }

        try {
            return (String) systemPropertiesGetMethod.invoke(null, name);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            LogUtil.e("SystemPropertiesAccessor.get", "unable to invoke system method", e);
            return null;
        }
    }

    @SuppressWarnings("PrivateApi")
    private @Nullable
    Method getSystemPropertiesGetMethod() {
        if (systemPropertiesGetMethod != null) {
            return systemPropertiesGetMethod;
        }

        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            if (systemPropertiesClass == null) {
                return null;
            }
            systemPropertiesGetMethod = systemPropertiesClass.getMethod("get", String.class);
            return systemPropertiesGetMethod;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LogUtil.e("SystemPropertiesAccessor.get", "unable to access system class", e);
            return null;
        }
    }
}
