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

package app.diol.dialer.constants;

import android.content.Context;
import android.support.annotation.NonNull;

import app.diol.dialer.common.Assert;
import app.diol.dialer.proguard.UsedByReflection;

/**
 * Utility to access constants that are different across build variants (Google Dialer, AOSP,
 * etc...). This functionality depends on a an implementation being present in the app that has the
 * same package and the class name ending in "Impl". For example,
 * com.android.dialer.constants.ConstantsImpl. This class is found by the module using reflection.
 */
@UsedByReflection(value = "Constants.java")
public abstract class Constants {
    private static Constants instance;
    private static boolean didInitializeInstance;

    protected Constants() {
    }

    @NonNull
    public static synchronized Constants get() {
        if (!didInitializeInstance) {
            didInitializeInstance = true;
            try {
                Class<?> clazz = Class.forName(Constants.class.getName() + "Impl");
                instance = (Constants) clazz.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                Assert.fail(
                        "Unable to create an instance of ConstantsImpl. To fix this error include one of the "
                                + "constants modules (googledialer, aosp etc...) in your target.");
            }
        }
        return instance;
    }

    @NonNull
    public abstract String getFilteredNumberProviderAuthority();

    @NonNull
    public abstract String getFileProviderAuthority();

    @NonNull
    public abstract String getAnnotatedCallLogProviderAuthority();

    @NonNull
    public abstract String getPhoneLookupHistoryProviderAuthority();

    @NonNull
    public abstract String getPreferredSimFallbackProviderAuthority();

    public abstract String getUserAgent(Context context);

    @NonNull
    public abstract String getSettingsActivity();
}
