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

import android.content.SharedPreferences;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javax.inject.Inject;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.storage.Unencrypted;

/**
 * Builds the annotated call log on application create once after the feature is enabled to reduce
 * the latency the first time call log is shown.
 */
public final class AnnotatedCallLogMigrator {

    private static final String PREF_MIGRATED = "annotatedCallLogMigratorMigrated";

    private final SharedPreferences sharedPreferences;
    private final RefreshAnnotatedCallLogWorker refreshAnnotatedCallLogWorker;
    private final ListeningExecutorService backgroundExecutor;

    @Inject
    AnnotatedCallLogMigrator(
            @Unencrypted SharedPreferences sharedPreferences,
            @BackgroundExecutor ListeningExecutorService backgroundExecutor,
            RefreshAnnotatedCallLogWorker refreshAnnotatedCallLogWorker) {
        this.sharedPreferences = sharedPreferences;
        this.backgroundExecutor = backgroundExecutor;
        this.refreshAnnotatedCallLogWorker = refreshAnnotatedCallLogWorker;
    }

    /**
     * Builds the annotated call log on application create once after the feature is enabled to reduce
     * the latency the first time call log is shown.
     */
    public ListenableFuture<Void> migrate() {
        return Futures.transformAsync(
                shouldMigrate(),
                (shouldMigrate) -> {
                    if (!shouldMigrate) {
                        return Futures.immediateFuture(null);
                    }
                    LogUtil.i("AnnotatedCallLogMigrator.migrate", "migrating annotated call log");
                    return Futures.transform(
                            refreshAnnotatedCallLogWorker.refreshWithoutDirtyCheck(),
                            (unused) -> {
                                sharedPreferences.edit().putBoolean(PREF_MIGRATED, true).apply();
                                return null;
                            },
                            MoreExecutors.directExecutor());
                },
                MoreExecutors.directExecutor());
    }

    private ListenableFuture<Boolean> shouldMigrate() {
        return backgroundExecutor.submit(() -> !sharedPreferences.getBoolean(PREF_MIGRATED, false));
    }

    /**
     * Clears data that indicates if migration happened or not. This is necessary if migration needs
     * to happen again, for example because the call log framework was disabled via flags due to a
     * problem.
     */
    ListenableFuture<Void> clearData() {
        return backgroundExecutor.submit(
                () -> {
                    sharedPreferences.edit().remove(PREF_MIGRATED).apply();
                    return null;
                });
    }
}
