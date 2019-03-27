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

package app.diol.dialer.binary;

import android.app.Application;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.v4.os.BuildCompat;

import app.diol.dialer.blocking.BlockedNumbersAutoMigrator;
import app.diol.dialer.blocking.FilteredNumberAsyncQueryHandler;
import app.diol.dialer.calllog.CallLogComponent;
import app.diol.dialer.calllog.CallLogFramework;
import app.diol.dialer.calllog.config.CallLogConfig;
import app.diol.dialer.calllog.config.CallLogConfigComponent;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.inject.ContextModule;
import app.diol.dialer.inject.HasRootComponent;
import app.diol.dialer.notification.NotificationChannelManager;
import app.diol.dialer.persistentlog.PersistentLogger;
import app.diol.dialer.strictmode.StrictModeComponent;

/**
 * A common application subclass for all Dialer build variants.
 */
public class DialerApplication extends Application implements HasRootComponent {

    private volatile Object rootComponent;

    @Override
    public void onCreate() {
        Trace.beginSection("DialerApplication.onCreate");
        StrictModeComponent.get(this).getDialerStrictMode().onApplicationCreate(this);
        super.onCreate();
        new BlockedNumbersAutoMigrator(
                this.getApplicationContext(),
                new FilteredNumberAsyncQueryHandler(this),
                DialerExecutorComponent.get(this).dialerExecutorFactory())
                .asyncAutoMigrate();
        initializeAnnotatedCallLog();
        PersistentLogger.initialize(this);

        if (BuildCompat.isAtLeastO()) {
            NotificationChannelManager.initChannels(this);
        }
        Trace.endSection();
    }

    private void initializeAnnotatedCallLog() {
        CallLogConfig callLogConfig = CallLogConfigComponent.get(this).callLogConfig();
        callLogConfig.schedulePollingJob();

        if (callLogConfig.isCallLogFrameworkEnabled()) {
            CallLogFramework callLogFramework = CallLogComponent.get(this).callLogFramework();
            callLogFramework.registerContentObservers();
        } else {
            LogUtil.i("DialerApplication.initializeAnnotatedCallLog", "framework not enabled");
        }
    }

    /**
     * Returns a new instance of the root component for the application. Sub classes should define a
     * root component that extends all the sub components "HasComponent" intefaces. The component
     * should specify all modules that the application supports and provide stubs for the remainder.
     */
    @NonNull
    protected Object buildRootComponent() {
        return DaggerDialerRootComponent.builder().contextModule(new ContextModule(this)).build();
    }

    /**
     * Returns a cached instance of application's root component.
     */
    @Override
    @NonNull
    public final Object component() {
        Object result = rootComponent;
        if (result == null) {
            synchronized (this) {
                result = rootComponent;
                if (result == null) {
                    rootComponent = result = buildRootComponent();
                }
            }
        }
        return result;
    }
}
