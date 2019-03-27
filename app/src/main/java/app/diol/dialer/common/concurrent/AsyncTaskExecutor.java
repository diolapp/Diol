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

import android.os.AsyncTask;
import android.support.annotation.MainThread;

import java.util.concurrent.Executor;

/**
 * Interface used to submit {@link AsyncTask} objects to run in the background.
 *
 * <p>This interface has a direct parallel with the {@link Executor} interface. It exists to
 * decouple the mechanics of AsyncTask submission from the description of how that AsyncTask will
 * execute.
 *
 * <p>One immediate benefit of this approach is that testing becomes much easier, since it is easy
 * to introduce a mock or fake AsyncTaskExecutor in unit/integration tests, and thus inspect which
 * tasks have been submitted and control their execution in an orderly manner.
 *
 * <p>Another benefit in due course will be the management of the submitted tasks. An extension to
 * this interface is planned to allow Activities to easily cancel all the submitted tasks that are
 * still pending in the onDestroy() method of the Activity.
 */
public interface AsyncTaskExecutor {

    /**
     * Executes the given AsyncTask with the default Executor.
     *
     * <p>This method <b>must only be called from the ui thread</b>.
     *
     * <p>The identifier supplied is any Object that can be used to identify the task later. Most
     * commonly this will be an enum which the tests can also refer to. {@code null} is also accepted,
     * though of course this won't help in identifying the task later.
     */
    @MainThread
    <T> AsyncTask<T, ?, ?> submit(Object identifier, AsyncTask<T, ?, ?> task, T... params);
}
