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

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;

import app.diol.dialer.common.Assert;

/**
 * Shared application executors.
 */
public final class DialerExecutors {

    /**
     * An application-wide thread pool used for low priority (non-UI) tasks.
     *
     * <p>This exists to prevent each individual dialer component from having to create its own
     * threads/pools, which would result in the application having more threads than really necessary.
     *
     * @param context any valid context object from which the application context can be retrieved
     */
    public static ExecutorService getLowPriorityThreadPool(@NonNull Context context) {
        return DialerExecutorComponent.get(Assert.isNotNull(context)).lowPriorityThreadPool();
    }
}
