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

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.inject.Singleton;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.common.concurrent.Annotations.LightweightExecutor;
import app.diol.dialer.common.concurrent.Annotations.NonUiParallel;
import app.diol.dialer.common.concurrent.Annotations.NonUiSerial;
import app.diol.dialer.common.concurrent.Annotations.Ui;
import app.diol.dialer.common.concurrent.Annotations.UiParallel;
import app.diol.dialer.common.concurrent.Annotations.UiSerial;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Module which provides concurrency bindings.
 */
@Module
public abstract class DialerExecutorModule {

    @Provides
    @Singleton
    @Ui
    static ListeningExecutorService provideUiThreadExecutorService() {
        return new UiThreadExecutor();
    }

    @Provides
    @Singleton
    @NonUiParallel
    static ExecutorService provideNonUiThreadPool() {
        return Executors.newFixedThreadPool(
                5,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        LogUtil.i("DialerExecutorModule.newThread", "creating low priority thread");
                        Thread thread = new Thread(runnable, "DialerExecutors-LowPriority");
                        // Java thread priority 4 corresponds to Process.THREAD_PRIORITY_BACKGROUND (10)
                        thread.setPriority(4);
                        return thread;
                    }
                });
    }

    @Provides
    @Singleton
    @NonUiSerial
    static ScheduledExecutorService provideNonUiSerialExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        LogUtil.i("NonUiTaskBuilder.newThread", "creating serial thread");
                        Thread thread = new Thread(runnable, "DialerExecutors-LowPriority-Serial");
                        // Java thread priority 4 corresponds to Process.THREAD_PRIORITY_BACKGROUND (10)
                        thread.setPriority(4);
                        return thread;
                    }
                });
    }

    @Provides
    @UiParallel
    static ExecutorService provideUiThreadPool() {
        return (ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR;
    }

    @Provides
    @Singleton
    @UiSerial
    static ScheduledExecutorService provideUiSerialExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        LogUtil.i("DialerExecutorModule.newThread", "creating serial thread");
                        Thread thread = new Thread(runnable, "DialerExecutors-HighPriority-Serial");
                        // Java thread priority 5 corresponds to Process.THREAD_PRIORITY_DEFAULT (0)
                        thread.setPriority(5);
                        return thread;
                    }
                });
    }

    @Provides
    @Singleton
    @LightweightExecutor
    static ListeningExecutorService provideLightweightExecutor(@UiParallel ExecutorService delegate) {
        return MoreExecutors.listeningDecorator(delegate);
    }

    @Provides
    @Singleton
    @BackgroundExecutor
    static ListeningExecutorService provideBackgroundExecutor(
            @NonUiParallel ExecutorService delegate) {
        return MoreExecutors.listeningDecorator(delegate);
    }

    @Binds
    abstract DialerExecutorFactory bindDialerExecutorFactory(
            DefaultDialerExecutorFactory defaultDialerExecutorFactory);
}
