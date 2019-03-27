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

import com.google.common.util.concurrent.AbstractListeningExecutorService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * An ExecutorService that delegates to the UI thread. Rejects attempts to shut down, and all
 * shutdown related APIs are unimplemented.
 */
public class UiThreadExecutor extends AbstractListeningExecutorService {

    @Inject
    public UiThreadExecutor() {
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> ListenableFuture<V> submit(final Callable<V> task) {
        final SettableFuture<V> resultFuture = SettableFuture.create();
        ThreadUtil.postOnUiThread(
                () -> {
                    try {
                        resultFuture.set(task.call());
                    } catch (Exception e) {
                        // uncaught exceptions on the UI thread should crash the app
                        resultFuture.setException(e);
                        throw new RuntimeException(e);
                    }
                });
        return resultFuture;
    }

    @Override
    public void execute(final Runnable runnable) {
        ThreadUtil.postOnUiThread(runnable);
    }
}
