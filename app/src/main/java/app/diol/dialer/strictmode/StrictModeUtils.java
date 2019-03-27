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

package app.diol.dialer.strictmode;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.preference.PreferenceManager;
import android.support.annotation.AnyThread;
import android.support.v4.os.UserManagerCompat;

import app.diol.dialer.buildtype.BuildType;
import app.diol.dialer.buildtype.BuildType.Type;
import app.diol.dialer.function.Supplier;
import app.diol.dialer.storage.StorageComponent;

/**
 * Utilities for enforcing strict-mode in an app.
 */
public final class StrictModeUtils {

    private static final ThreadPolicy THREAD_NO_PENALTY =
            new StrictMode.ThreadPolicy.Builder().permitAll().build();

    private StrictModeUtils() {
    }

    /**
     * Convenience method for disabling and enabling the thread policy death penalty using lambdas.
     *
     * <p>For example:
     *
     * <p><code>
     * Value foo = StrictModeUtils.bypass(() -> doDiskAccessOnMainThreadReturningValue());
     * </code>
     *
     * <p>The thread policy is only mutated if this is called from the main thread.
     */
    @AnyThread
    public static <T> T bypass(Supplier<T> supplier) {
        if (isStrictModeAllowed() && onMainThread()) {
            ThreadPolicy originalPolicy = StrictMode.getThreadPolicy();
            StrictMode.setThreadPolicy(THREAD_NO_PENALTY);
            try {
                return supplier.get();
            } finally {
                StrictMode.setThreadPolicy(originalPolicy);
            }
        }
        return supplier.get();
    }

    /**
     * Convenience method for disabling and enabling the thread policy death penalty using lambdas.
     *
     * <p>For example:
     *
     * <p><code>
     * StrictModeUtils.bypass(() -> doDiskAccessOnMainThread());
     * </code>
     *
     * <p>The thread policy is only mutated if this is called from the main thread.
     */
    @AnyThread
    public static void bypass(Runnable runnable) {
        if (isStrictModeAllowed() && onMainThread()) {
            ThreadPolicy originalPolicy = StrictMode.getThreadPolicy();
            StrictMode.setThreadPolicy(THREAD_NO_PENALTY);
            try {
                runnable.run();
            } finally {
                StrictMode.setThreadPolicy(originalPolicy);
            }
        } else {
            runnable.run();
        }
    }

    public static boolean isStrictModeAllowed() {
        return BuildType.get() == Type.BUGFOOD;
    }

    private static boolean onMainThread() {
        return Looper.getMainLooper().equals(Looper.myLooper());
    }

    /**
     * We frequently access shared preferences on the main thread, which causes strict mode
     * violations. When strict mode is allowed, warm up the shared preferences so that later uses of
     * shared preferences access the in-memory versions and we don't have to bypass strict mode at
     * every point in the application where shared preferences are accessed.
     */
    public static void warmupSharedPrefs(Application application) {
        // From credential-encrypted (CE) storage, i.e.:
        //    /data/data/com.android.dialer/shared_prefs

        if (UserManagerCompat.isUserUnlocked(application)) {
            // <package_name>_preferences.xml
            PreferenceManager.getDefaultSharedPreferences(application);

            // <package_name>.xml
            application.getSharedPreferences(application.getPackageName(), Context.MODE_PRIVATE);
        }

        // From device-encrypted (DE) storage, i.e.:
        //   /data/user_de/0/com.android.dialer/shared_prefs/

        // <package_name>_preferences.xml
        StorageComponent.get(application).unencryptedSharedPrefs();
    }
}
