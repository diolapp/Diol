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
import android.support.annotation.AnyThread;
import android.support.annotation.VisibleForTesting;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.storage.Unencrypted;

/**
 * Provides information about the state of the annotated call log.
 */
@ThreadSafe
public final class CallLogState {

    private static final String ANNOTATED_CALL_LOG_BUILT_PREF = "annotated_call_log_built";

    private final SharedPreferences sharedPreferences;
    private final ListeningExecutorService backgroundExecutor;

    @VisibleForTesting
    @Inject
    public CallLogState(
            @Unencrypted SharedPreferences sharedPreferences,
            @BackgroundExecutor ListeningExecutorService backgroundExecutor) {
        this.sharedPreferences = sharedPreferences;
        this.backgroundExecutor = backgroundExecutor;
    }

    /**
     * Mark the call log as having been built. This is written to disk the first time the annotated
     * call log has been built and shouldn't ever be reset unless the user clears data.
     */
    @AnyThread
    public void markBuilt() {
        sharedPreferences.edit().putBoolean(ANNOTATED_CALL_LOG_BUILT_PREF, true).apply();
    }

    /**
     * Clear the call log state. This is useful for example if the annotated call log needs to be
     * disabled because there was a problem.
     */
    @AnyThread
    public void clearData() {
        sharedPreferences.edit().remove(ANNOTATED_CALL_LOG_BUILT_PREF).apply();
    }

    /**
     * Returns true if the annotated call log has been built at least once.
     *
     * <p>It may not yet have been built if the user was just upgraded to the new call log, or they
     * just cleared data.
     */
    @AnyThread
    public ListenableFuture<Boolean> isBuilt() {
        return backgroundExecutor.submit(
                () -> sharedPreferences.getBoolean(ANNOTATED_CALL_LOG_BUILT_PREF, false));
    }
}
