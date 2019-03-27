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

package app.diol.dialer.calllog;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.diol.dialer.calllog.datasources.CallLogDataSource;
import app.diol.dialer.calllog.datasources.DataSources;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.Ui;
import app.diol.dialer.inject.ApplicationContext;

/**
 * Coordinates work across {@link DataSources}.
 *
 * <p>All methods should be called on the main thread.
 */
@Singleton
public final class CallLogFramework {

    private final Context appContext;
    private final DataSources dataSources;
    private final AnnotatedCallLogMigrator annotatedCallLogMigrator;
    private final ListeningExecutorService uiExecutor;
    private final CallLogState callLogState;

    @Inject
    CallLogFramework(
            @ApplicationContext Context appContext,
            DataSources dataSources,
            AnnotatedCallLogMigrator annotatedCallLogMigrator,
            @Ui ListeningExecutorService uiExecutor,
            CallLogState callLogState) {
        this.appContext = appContext;
        this.dataSources = dataSources;
        this.annotatedCallLogMigrator = annotatedCallLogMigrator;
        this.uiExecutor = uiExecutor;
        this.callLogState = callLogState;
    }

    /**
     * Registers the content observers for all data sources.
     */
    public void registerContentObservers() {
        LogUtil.enterBlock("CallLogFramework.registerContentObservers");
        for (CallLogDataSource dataSource : dataSources.getDataSourcesIncludingSystemCallLog()) {
            dataSource.registerContentObservers();
        }
    }

    /**
     * Enables the framework.
     */
    public ListenableFuture<Void> enable() {
        registerContentObservers();
        return annotatedCallLogMigrator.migrate();
    }

    /**
     * Disables the framework.
     */
    public ListenableFuture<Void> disable() {
        return Futures.transform(
                Futures.allAsList(disableDataSources(), annotatedCallLogMigrator.clearData()),
                unused -> null,
                MoreExecutors.directExecutor());
    }

    private ListenableFuture<Void> disableDataSources() {
        LogUtil.enterBlock("CallLogFramework.disableDataSources");

        for (CallLogDataSource dataSource : dataSources.getDataSourcesIncludingSystemCallLog()) {
            dataSource.unregisterContentObservers();
        }

        callLogState.clearData();

        // Clear data only after all content observers have been disabled.
        List<ListenableFuture<Void>> allFutures = new ArrayList<>();
        for (CallLogDataSource dataSource : dataSources.getDataSourcesIncludingSystemCallLog()) {
            allFutures.add(dataSource.clearData());
        }

        return Futures.transform(
                Futures.allAsList(allFutures),
                unused -> {
                    // Send a broadcast to the OldMainActivityPeer to remove the NewCallLogFragment and
                    // NewVoicemailFragment if it is currently attached. If this is not done, user interaction
                    // with the fragment could cause call log framework state to be unexpectedly written. For
                    // example scrolling could cause the AnnotatedCallLog to be read (which would trigger
                    // database creation).
                    LocalBroadcastManager.getInstance(appContext)
                            .sendBroadcastSync(new Intent("disableCallLogFramework"));
                    return null;
                },
                uiExecutor);
    }
}
