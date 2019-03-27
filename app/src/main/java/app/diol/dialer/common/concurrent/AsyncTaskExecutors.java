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

import app.diol.dialer.common.Assert;

/**
 * Factory methods for creating AsyncTaskExecutors.
 *
 * <p>All of the factory methods on this class check first to see if you have set a static {@link
 * AsyncTaskExecutorFactory} set through the {@link #setFactoryForTest(AsyncTaskExecutorFactory)}
 * method, and if so delegate to that instead, which is one way of injecting dependencies for
 * testing classes whose construction cannot be controlled such as {@link android.app.Activity}.
 */
public final class AsyncTaskExecutors {

    /**
     * A single instance of the {@link AsyncTaskExecutorFactory}, to which we delegate if it is
     * non-null, for injecting when testing.
     */
    private static AsyncTaskExecutorFactory injectedAsyncTaskExecutorFactory = null;

    /**
     * Creates an AsyncTaskExecutor that submits tasks to run with {@link AsyncTask#SERIAL_EXECUTOR}.
     */
    public static AsyncTaskExecutor createAsyncTaskExecutor() {
        synchronized (AsyncTaskExecutors.class) {
            if (injectedAsyncTaskExecutorFactory != null) {
                return injectedAsyncTaskExecutorFactory.createAsyncTaskExeuctor();
            }
            return new SimpleAsyncTaskExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    /**
     * Creates an AsyncTaskExecutor that submits tasks to run with {@link
     * AsyncTask#THREAD_POOL_EXECUTOR}.
     */
    public static AsyncTaskExecutor createThreadPoolExecutor() {
        synchronized (AsyncTaskExecutors.class) {
            if (injectedAsyncTaskExecutorFactory != null) {
                return injectedAsyncTaskExecutorFactory.createAsyncTaskExeuctor();
            }
            return new SimpleAsyncTaskExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static void setFactoryForTest(AsyncTaskExecutorFactory factory) {
        synchronized (AsyncTaskExecutors.class) {
            injectedAsyncTaskExecutorFactory = factory;
        }
    }

    /**
     * Interface for creating AsyncTaskExecutor objects.
     */
    public interface AsyncTaskExecutorFactory {

        AsyncTaskExecutor createAsyncTaskExeuctor();
    }

    static class SimpleAsyncTaskExecutor implements AsyncTaskExecutor {

        private final Executor executor;

        public SimpleAsyncTaskExecutor(Executor executor) {
            this.executor = executor;
        }

        @Override
        @MainThread
        public <T> AsyncTask<T, ?, ?> submit(Object identifer, AsyncTask<T, ?, ?> task, T... params) {
            Assert.isMainThread();
            return task.executeOnExecutor(executor, params);
        }
    }
}
