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

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.FailureListener;
import app.diol.dialer.common.concurrent.DialerExecutor.SuccessListener;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;

/**
 * Do not use this class directly. Instead use {@link DialerExecutors}.
 *
 * @param <InputT>  the type of the object sent to the task upon execution
 * @param <OutputT> the type of the result of the background computation
 */
public final class DialerUiTaskFragment<InputT, OutputT> extends Fragment {

    private Worker<InputT, OutputT> worker;
    private SuccessListener<OutputT> successListener;
    private FailureListener failureListener;

    private ScheduledExecutorService serialExecutor;
    private Executor parallelExecutor;
    private ScheduledFuture<?> scheduledFuture;

    /**
     * Creates a new {@link DialerUiTaskFragment} or gets an existing one in the event that a
     * configuration change occurred while the previous activity's task was still running. Must be
     * called from onCreate of your activity or fragment.
     *
     * @param taskId          used for the headless fragment ID and task ID
     * @param worker          a function executed on a worker thread which accepts an {@link InputT} and
     *                        returns an {@link OutputT}. It should ideally not be an inner class of your
     *                        activity/fragment (meaning it should not be a lambda, anonymous, or non-static) but it can
     *                        be a static nested class. The static nested class should not contain any reference to UI,
     *                        including any activity or fragment or activity context, though it may reference some
     *                        threadsafe system objects such as the application context.
     * @param successListener a function executed on the main thread upon task success. There are no
     *                        restraints on this as it is executed on the main thread, so lambdas, anonymous, or inner
     *                        classes of your activity or fragment are all fine.
     * @param failureListener a function executed on the main thread upon task failure. The exception
     *                        is already logged so this can often be a no-op. There are no restraints on this as it is
     *                        executed on the main thread, so lambdas, anonymous, or inner classes of your activity or
     *                        fragment are all fine.
     * @param <InputT>        the type of the object sent to the task upon execution
     * @param <OutputT>       the type of the result of the background computation
     * @return a {@link DialerUiTaskFragment} which may be used to call the "execute*" methods
     */
    @MainThread
    static <InputT, OutputT> DialerUiTaskFragment<InputT, OutputT> create(
            FragmentManager fragmentManager,
            String taskId,
            Worker<InputT, OutputT> worker,
            SuccessListener<OutputT> successListener,
            FailureListener failureListener,
            @NonNull ScheduledExecutorService serialExecutorService,
            @NonNull Executor parallelExecutor) {
        Assert.isMainThread();

        DialerUiTaskFragment<InputT, OutputT> fragment =
                (DialerUiTaskFragment<InputT, OutputT>) fragmentManager.findFragmentByTag(taskId);

        if (fragment == null) {
            LogUtil.i("DialerUiTaskFragment.create", "creating new DialerUiTaskFragment for " + taskId);
            fragment = new DialerUiTaskFragment<>();
            fragmentManager.beginTransaction().add(fragment, taskId).commit();
        }
        fragment.worker = worker;
        fragment.successListener = successListener;
        fragment.failureListener = failureListener;
        fragment.serialExecutor = Assert.isNotNull(serialExecutorService);
        fragment.parallelExecutor = Assert.isNotNull(parallelExecutor);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LogUtil.enterBlock("DialerUiTaskFragment.onDetach");
        successListener = null;
        failureListener = null;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false /* mayInterrupt */);
            scheduledFuture = null;
        }
    }

    void executeSerial(InputT input) {
        serialExecutor.execute(() -> runTask(input));
    }

    void executeSerialWithWait(InputT input, long waitMillis) {
        if (scheduledFuture != null) {
            LogUtil.i("DialerUiTaskFragment.executeSerialWithWait", "cancelling waiting task");
            scheduledFuture.cancel(false /* mayInterrupt */);
        }
        scheduledFuture =
                serialExecutor.schedule(() -> runTask(input), waitMillis, TimeUnit.MILLISECONDS);
    }

    void executeParallel(InputT input) {
        parallelExecutor.execute(() -> runTask(input));
    }

    void executeOnCustomExecutor(ExecutorService executor, InputT input) {
        executor.execute(() -> runTask(input));
    }

    @WorkerThread
    private void runTask(@Nullable InputT input) {
        try {
            OutputT output = worker.doInBackground(input);
            if (successListener == null) {
                LogUtil.i("DialerUiTaskFragment.runTask", "task succeeded but UI is dead");
            } else {
                ThreadUtil.postOnUiThread(
                        () -> {
                            // Even though there is a null check above, it is possible for the activity/fragment
                            // to be finished between the time the runnable is posted and the time it executes. Do
                            // an additional check here.
                            if (successListener == null) {
                                LogUtil.i(
                                        "DialerUiTaskFragment.runTask",
                                        "task succeeded but UI died after success runnable posted");
                            } else {
                                successListener.onSuccess(output);
                            }
                        });
            }
        } catch (Throwable throwable) {
            LogUtil.e("DialerUiTaskFragment.runTask", "task failed", throwable);
            if (failureListener == null) {
                LogUtil.i("DialerUiTaskFragment.runTask", "task failed but UI is dead");
            } else {
                ThreadUtil.postOnUiThread(
                        () -> {
                            // Even though there is a null check above, it is possible for the activity/fragment
                            // to be finished between the time the runnable is posted and the time it executes. Do
                            // an additional check here.
                            if (failureListener == null) {
                                LogUtil.i(
                                        "DialerUiTaskFragment.runTask",
                                        "task failed but UI died after failure runnable posted");
                            } else {
                                failureListener.onFailure(throwable);
                            }
                        });
            }
        }
    }
}
