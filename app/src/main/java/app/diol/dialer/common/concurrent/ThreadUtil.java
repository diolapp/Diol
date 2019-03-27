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

package app.diol.dialer.common.concurrent;

import android.os.Handler;
import android.os.Looper;

/**
 * Application-wide utility methods for working with threads.
 */
public class ThreadUtil {
    private static volatile Handler mainThreadHandler;

    /**
     * Posts a runnable to the UI thread.
     */
    public static void postOnUiThread(Runnable runnable) {
        getUiThreadHandler().post(runnable);
    }

    /**
     * Posts a runnable to the UI thread, to be run after the specified amount of time elapses.
     */
    public static void postDelayedOnUiThread(Runnable runnable, long delayMillis) {
        getUiThreadHandler().postDelayed(runnable, delayMillis);
    }

    /**
     * Gets a handler which uses the main looper.
     */
    public static Handler getUiThreadHandler() {
        if (mainThreadHandler == null) {
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return mainThreadHandler;
    }
}
