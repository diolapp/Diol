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

import com.google.common.util.concurrent.AsyncCallable;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.util.concurrent.Futures.immediateCancelledFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

/**
 * Serializes execution of a set of operations. This class guarantees that a submitted callable will
 * not be called before previously submitted callables have completed.
 */
public final class DialerFutureSerializer {
    /**
     * This reference acts as a pointer tracking the head of a linked list of ListenableFutures.
     */
    private final AtomicReference<ListenableFuture<?>> ref =
            new AtomicReference<>(immediateFuture(null));

    /**
     * Enqueues a task to run when the previous task (if any) completes.
     */
    public <T> ListenableFuture<T> submit(final Callable<T> callable, Executor executor) {
        return submitAsync(() -> immediateFuture(callable.call()), executor);
    }

    /**
     * Enqueues a task to run when the previous task (if any) completes.
     *
     * <p>Cancellation does not propagate from the output future to the future returned from {@code
     * callable}, but if the output future is cancelled before {@link AsyncCallable#call()} is
     * invoked, {@link AsyncCallable#call()} will not be invoked.
     */
    public <T> ListenableFuture<T> submitAsync(final AsyncCallable<T> callable, Executor executor) {
        AtomicBoolean wasCancelled = new AtomicBoolean(false);
        final AsyncCallable<T> task =
                () -> {
                    if (wasCancelled.get()) {
                        return immediateCancelledFuture();
                    }
                    return callable.call();
                };
        /*
         * Three futures are at play here:
         * taskFuture is the future that comes from the callable.
         * newFuture is the future we use to track the serialization of our task.
         * oldFuture is the previous task's newFuture.
         *
         * newFuture is guaranteed to only complete once all tasks previously submitted to this instance
         * once the futures returned from those submissions have completed.
         */
        final SettableFuture<Object> newFuture = SettableFuture.create();

        final ListenableFuture<?> oldFuture = ref.getAndSet(newFuture);

        // Invoke our task once the previous future completes.
        final ListenableFuture<T> taskFuture =
                Futures.nonCancellationPropagating(
                        Futures.submitAsync(task, runnable -> oldFuture.addListener(runnable, executor)));
        // newFuture's lifetime is determined by taskFuture, unless taskFuture is cancelled, in which
        // case it falls back to oldFuture's. This is to ensure that if the future we return is
        // cancelled, we don't begin execution of the next task until after oldFuture completes.
        taskFuture.addListener(
                () -> {
                    if (taskFuture.isCancelled()) {
                        // Since the value of oldFuture can only ever be immediateFuture(null) or setFuture of a
                        // future that eventually came from immediateFuture(null), this doesn't leak throwables
                        // or completion values.
                        wasCancelled.set(true);
                        newFuture.setFuture(oldFuture);
                    } else {
                        newFuture.set(null);
                    }
                },
                directExecutor());

        return taskFuture;
    }
}
