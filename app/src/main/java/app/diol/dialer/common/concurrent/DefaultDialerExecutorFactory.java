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
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.NonUiParallel;
import app.diol.dialer.common.concurrent.Annotations.NonUiSerial;
import app.diol.dialer.common.concurrent.Annotations.UiParallel;
import app.diol.dialer.common.concurrent.Annotations.UiSerial;
import app.diol.dialer.common.concurrent.DialerExecutor.Builder;
import app.diol.dialer.common.concurrent.DialerExecutor.FailureListener;
import app.diol.dialer.common.concurrent.DialerExecutor.SuccessListener;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;

/**
 * The production {@link DialerExecutorFactory}.
 */
public class DefaultDialerExecutorFactory implements DialerExecutorFactory {
    private final ExecutorService nonUiThreadPool;
    private final ScheduledExecutorService nonUiSerialExecutor;
    private final ExecutorService uiThreadPool;
    private final ScheduledExecutorService uiSerialExecutor;

    @Inject
    DefaultDialerExecutorFactory(
            @NonUiParallel ExecutorService nonUiThreadPool,
            @NonUiSerial ScheduledExecutorService nonUiSerialExecutor,
            @UiParallel ExecutorService uiThreadPool,
            @UiSerial ScheduledExecutorService uiSerialExecutor) {
        this.nonUiThreadPool = nonUiThreadPool;
        this.nonUiSerialExecutor = nonUiSerialExecutor;
        this.uiThreadPool = uiThreadPool;
        this.uiSerialExecutor = uiSerialExecutor;
    }

    @Override
    @NonNull
    public <InputT, OutputT> DialerExecutor.Builder<InputT, OutputT> createUiTaskBuilder(
            @NonNull FragmentManager fragmentManager,
            @NonNull String taskId,
            @NonNull Worker<InputT, OutputT> worker) {
        return new UiTaskBuilder<>(
                Assert.isNotNull(fragmentManager),
                Assert.isNotNull(taskId),
                Assert.isNotNull(worker),
                uiSerialExecutor,
                uiThreadPool);
    }

    @Override
    @NonNull
    public <InputT, OutputT> DialerExecutor.Builder<InputT, OutputT> createNonUiTaskBuilder(
            @NonNull Worker<InputT, OutputT> worker) {
        return new NonUiTaskBuilder<>(Assert.isNotNull(worker), nonUiSerialExecutor, nonUiThreadPool);
    }

    private abstract static class BaseTaskBuilder<InputT, OutputT>
            implements DialerExecutor.Builder<InputT, OutputT> {

        @Nullable
        final ScheduledExecutorService serialExecutorService;
        @Nullable
        final Executor parallelExecutor;
        private final Worker<InputT, OutputT> worker;
        private SuccessListener<OutputT> successListener = output -> {
        };
        private FailureListener failureListener =
                throwable -> {
                    throw new RuntimeException(throwable);
                };

        BaseTaskBuilder(
                Worker<InputT, OutputT> worker,
                @Nullable ScheduledExecutorService serialExecutorService,
                @Nullable Executor parallelExecutor) {
            this.worker = worker;
            this.serialExecutorService = serialExecutorService;
            this.parallelExecutor = parallelExecutor;
        }

        @NonNull
        @Override
        public Builder<InputT, OutputT> onSuccess(@NonNull SuccessListener<OutputT> successListener) {
            this.successListener = Assert.isNotNull(successListener);
            return this;
        }

        @NonNull
        @Override
        public Builder<InputT, OutputT> onFailure(@NonNull FailureListener failureListener) {
            this.failureListener = Assert.isNotNull(failureListener);
            return this;
        }
    }

    /**
     * Convenience class for use by {@link DialerExecutorFactory} implementations.
     */
    public static class UiTaskBuilder<InputT, OutputT> extends BaseTaskBuilder<InputT, OutputT> {
        private final FragmentManager fragmentManager;
        private final String id;

        private DialerUiTaskFragment<InputT, OutputT> dialerUiTaskFragment;

        public UiTaskBuilder(
                FragmentManager fragmentManager,
                String id,
                Worker<InputT, OutputT> worker,
                ScheduledExecutorService serialExecutor,
                Executor parallelExecutor) {
            super(worker, serialExecutor, parallelExecutor);
            this.fragmentManager = fragmentManager;
            this.id = id;
        }

        @NonNull
        @Override
        public DialerExecutor<InputT> build() {
            dialerUiTaskFragment =
                    DialerUiTaskFragment.create(
                            fragmentManager,
                            id,
                            super.worker,
                            super.successListener,
                            super.failureListener,
                            serialExecutorService,
                            parallelExecutor);
            return new UiDialerExecutor<>(dialerUiTaskFragment);
        }
    }

    /**
     * Convenience class for use by {@link DialerExecutorFactory} implementations.
     */
    public static class NonUiTaskBuilder<InputT, OutputT> extends BaseTaskBuilder<InputT, OutputT> {

        public NonUiTaskBuilder(
                Worker<InputT, OutputT> worker,
                @NonNull ScheduledExecutorService serialExecutor,
                @NonNull Executor parallelExecutor) {
            super(worker, Assert.isNotNull(serialExecutor), Assert.isNotNull(parallelExecutor));
        }

        @NonNull
        @Override
        public DialerExecutor<InputT> build() {
            return new NonUiDialerExecutor<>(
                    super.worker,
                    super.successListener,
                    super.failureListener,
                    serialExecutorService,
                    parallelExecutor);
        }
    }

    private static class UiDialerExecutor<InputT, OutputT> implements DialerExecutor<InputT> {

        private final DialerUiTaskFragment<InputT, OutputT> dialerUiTaskFragment;

        UiDialerExecutor(DialerUiTaskFragment<InputT, OutputT> dialerUiTaskFragment) {
            this.dialerUiTaskFragment = dialerUiTaskFragment;
        }

        @Override
        public void executeSerial(@Nullable InputT input) {
            dialerUiTaskFragment.executeSerial(input);
        }

        @Override
        public void executeSerialWithWait(@Nullable InputT input, long waitMillis) {
            dialerUiTaskFragment.executeSerialWithWait(input, waitMillis);
        }

        @Override
        public void executeParallel(@Nullable InputT input) {
            dialerUiTaskFragment.executeParallel(input);
        }

        @Override
        public void executeOnCustomExecutorService(
                @NonNull ExecutorService executorService, @Nullable InputT input) {
            dialerUiTaskFragment.executeOnCustomExecutor(Assert.isNotNull(executorService), input);
        }
    }

    private static class NonUiDialerExecutor<InputT, OutputT> implements DialerExecutor<InputT> {

        private final Worker<InputT, OutputT> worker;
        private final SuccessListener<OutputT> successListener;
        private final FailureListener failureListener;

        private final ScheduledExecutorService serialExecutorService;
        private final Executor parallelExecutor;

        private ScheduledFuture<?> scheduledFuture;

        NonUiDialerExecutor(
                Worker<InputT, OutputT> worker,
                SuccessListener<OutputT> successListener,
                FailureListener failureListener,
                ScheduledExecutorService serialExecutorService,
                Executor parallelExecutor) {
            this.worker = worker;
            this.successListener = successListener;
            this.failureListener = failureListener;
            this.serialExecutorService = serialExecutorService;
            this.parallelExecutor = parallelExecutor;
        }

        @Override
        public void executeSerial(@Nullable InputT input) {
            serialExecutorService.execute(() -> run(input));
        }

        @Override
        public void executeSerialWithWait(@Nullable InputT input, long waitMillis) {
            if (scheduledFuture != null) {
                LogUtil.i("NonUiDialerExecutor.executeSerialWithWait", "cancelling waiting task");
                scheduledFuture.cancel(false /* mayInterrupt */);
            }
            scheduledFuture =
                    serialExecutorService.schedule(() -> run(input), waitMillis, TimeUnit.MILLISECONDS);
        }

        @Override
        public void executeParallel(@Nullable InputT input) {
            parallelExecutor.execute(() -> run(input));
        }

        @Override
        public void executeOnCustomExecutorService(
                @NonNull ExecutorService executorService, @Nullable InputT input) {
            Assert.isNotNull(executorService).execute(() -> run(input));
        }

        private void run(@Nullable InputT input) {
            OutputT output;
            try {
                output = worker.doInBackground(input);
            } catch (Throwable throwable) {
                ThreadUtil.postOnUiThread(() -> failureListener.onFailure(throwable));
                return;
            }
            ThreadUtil.postOnUiThread(() -> successListener.onSuccess(output));
        }
    }
}
