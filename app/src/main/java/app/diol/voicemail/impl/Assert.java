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

package app.diol.voicemail.impl;

import android.os.Looper;

/**
 * Assertions which will result in program termination.
 */
public class Assert {

    private static Boolean isMainThreadForTest;

    public static void isTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    public static void isMainThread() {
        if (isMainThreadForTest != null) {
            isTrue(isMainThreadForTest);
            return;
        }
        isTrue(Looper.getMainLooper().equals(Looper.myLooper()));
    }

    public static void isNotMainThread() {
        if (isMainThreadForTest != null) {
            isTrue(!isMainThreadForTest);
            return;
        }
        isTrue(!Looper.getMainLooper().equals(Looper.myLooper()));
    }

    public static void fail() {
        throw new AssertionError("Fail");
    }

    /**
     * Override the main thread status for tests. Set to null to revert to normal
     * behavior
     */
    @NeededForTesting
    public static void setIsMainThreadForTesting(Boolean isMainThread) {
        isMainThreadForTest = isMainThread;
    }
}
