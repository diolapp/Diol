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

package app.diol.dialer.buildtype;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Utility to find out which build type the app is running as.
 */
public class BuildType {

    private static int cachedBuildType;
    private static boolean didInitializeBuildType;
    private BuildType() {
    }

    @Type
    public static synchronized int get() {
        if (!didInitializeBuildType) {
            didInitializeBuildType = true;
            try {
                Class<?> clazz = Class.forName(BuildTypeAccessor.class.getName() + "Impl");
                BuildTypeAccessor accessorImpl = (BuildTypeAccessor) clazz.getConstructor().newInstance();
                cachedBuildType = accessorImpl.getBuildType();
            } catch (ReflectiveOperationException e) {
                LogUtil.e("BuildType.get", "error creating BuildTypeAccessorImpl", e);
                Assert.fail(
                        "Unable to get build type. To fix this error include one of the build type "
                                + "modules (bugfood, etc...) in your target.");
            }
        }
        return cachedBuildType;
    }

    /**
     * The type of build.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            Type.BUGFOOD,
            Type.FISHFOOD,
            Type.DOGFOOD,
            Type.RELEASE,
            Type.TEST,
    })
    public @interface Type {
        int BUGFOOD = 1;
        int FISHFOOD = 2;
        int DOGFOOD = 3;
        int RELEASE = 4;
        int TEST = 5;
    }
}
