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

import app.diol.dialer.proguard.UsedByReflection;

/**
 * Gets the build type. The functionality depends on a an implementation being present in the app
 * that has the same package and the class name ending in "Impl". For example,
 * app.diol.dialer.buildtype.BuildTypeAccessorImpl. This class is found by the module using
 * reflection.
 */
@UsedByReflection(value = "BuildType.java")
        /* package */ interface BuildTypeAccessor {
    @BuildType.Type
    int getBuildType();
}
