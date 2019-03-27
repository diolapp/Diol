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

import android.app.FragmentManager;
import android.support.annotation.NonNull;

import app.diol.dialer.common.concurrent.DialerExecutor.Worker;

/**
 * Factory interface for creating {@link DialerExecutor} objects.
 *
 * <p>Factory instances may be used instead of the static methods in {@link DialerExecutors} in
 * order to improve testability.
 *
 * @see DialerExecutors
 */
public interface DialerExecutorFactory {

    /**
     * Must be called from onCreate of your activity or fragment.
     *
     * @param taskId used for the headless fragment ID and task ID
     * @param worker a function executed on a worker thread which accepts an {@link InputT} and
     *               returns an {@link OutputT}. It should ideally not be an inner class of your (meaning it
     *               should not be a lambda, anonymous, or non-static) but it can be a static nested class. The
     *               static nested class should not contain any reference to UI, including any activity or
     *               fragment or activity context, though it may reference some threadsafe system objects such
     *               as the application context.
     */
    @NonNull
    <InputT, OutputT> DialerExecutor.Builder<InputT, OutputT> createUiTaskBuilder(
            @NonNull FragmentManager fragmentManager,
            @NonNull String taskId,
            @NonNull Worker<InputT, OutputT> worker);

    /**
     * Create a task from a non-UI context.
     *
     * @param worker a function executed on a worker thread which accepts an {@link InputT} and
     *               returns an {@link OutputT}. It should ideally not be an inner class of your (meaning it
     *               should not be a lambda, anonymous, or non-static) but it can be a static nested class. The
     *               static nested class should not contain any reference to UI, including any activity or
     *               fragment or activity context, though it may reference some threadsafe system objects such
     *               as the application context.
     */
    @NonNull
    <InputT, OutputT> DialerExecutor.Builder<InputT, OutputT> createNonUiTaskBuilder(
            @NonNull Worker<InputT, OutputT> worker);
}
