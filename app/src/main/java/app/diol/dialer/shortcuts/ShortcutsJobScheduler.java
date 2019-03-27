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

package app.diol.dialer.shortcuts;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Schedules dialer shortcut jobs.
 *
 * <p>A {@link ConfigProvider} value controls whether the jobs which creates shortcuts should be
 * scheduled or cancelled.
 */
public class ShortcutsJobScheduler {

    @MainThread
    public static void scheduleAllJobs(@NonNull Context context) {
        LogUtil.enterBlock("ShortcutsJobScheduler.scheduleAllJobs");
        Assert.isMainThread();

        if (Shortcuts.areDynamicShortcutsEnabled(context)) {
            LogUtil.i("ShortcutsJobScheduler.scheduleAllJobs", "enabling shortcuts");

            PeriodicJobService.schedulePeriodicJob(context);
        } else {
            LogUtil.i("ShortcutsJobScheduler.scheduleAllJobs", "disabling shortcuts");

            PeriodicJobService.cancelJob(context);
        }
    }
}
