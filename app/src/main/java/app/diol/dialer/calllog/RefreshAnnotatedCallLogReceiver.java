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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import app.diol.dialer.calllog.RefreshAnnotatedCallLogWorker.RefreshResult;
import app.diol.dialer.calllog.constants.IntentNames;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.ThreadUtil;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.logging.LoggingBindings;
import app.diol.dialer.metrics.FutureTimer;
import app.diol.dialer.metrics.Metrics;
import app.diol.dialer.metrics.MetricsComponent;

/**
 * A {@link BroadcastReceiver} that starts/cancels refreshing the annotated call log when notified.
 */
public final class RefreshAnnotatedCallLogReceiver extends BroadcastReceiver {

    /**
     * This is a reasonable time that it might take between related call log writes, that also
     * shouldn't slow down single-writes too much. For example, when populating the database using the
     * simulator, using this value results in ~6 refresh cycles (on a release build) to write 120 call
     * log entries.
     */
    private static final long REFRESH_ANNOTATED_CALL_LOG_WAIT_MILLIS = 100L;

    private final RefreshAnnotatedCallLogWorker refreshAnnotatedCallLogWorker;
    private final FutureTimer futureTimer;
    private final LoggingBindings logger;

    @Nullable
    private Runnable refreshAnnotatedCallLogRunnable;

    public RefreshAnnotatedCallLogReceiver(Context context) {
        refreshAnnotatedCallLogWorker =
                CallLogComponent.get(context).getRefreshAnnotatedCallLogWorker();
        futureTimer = MetricsComponent.get(context).futureTimer();
        logger = Logger.get(context);
    }

    /**
     * Returns an {@link IntentFilter} containing all actions accepted by this broadcast receiver.
     */
    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IntentNames.ACTION_REFRESH_ANNOTATED_CALL_LOG);
        intentFilter.addAction(IntentNames.ACTION_CANCEL_REFRESHING_ANNOTATED_CALL_LOG);
        return intentFilter;
    }

    private static DialerImpression.Type getImpressionType(
            boolean checkDirty, RefreshResult refreshResult) {
        switch (refreshResult) {
            case NOT_DIRTY:
                return DialerImpression.Type.ANNOTATED_CALL_LOG_NOT_DIRTY;
            case REBUILT_BUT_NO_CHANGES_NEEDED:
                return checkDirty
                        ? DialerImpression.Type.ANNOTATED_CALL_LOG_NO_CHANGES_NEEDED
                        : DialerImpression.Type.ANNOTATED_CALL_LOG_FORCE_REFRESH_NO_CHANGES_NEEDED;
            case REBUILT_AND_CHANGES_NEEDED:
                return checkDirty
                        ? DialerImpression.Type.ANNOTATED_CALL_LOG_CHANGES_NEEDED
                        : DialerImpression.Type.ANNOTATED_CALL_LOG_FORCE_REFRESH_CHANGES_NEEDED;
            default:
                throw new IllegalStateException("Unsupported result: " + refreshResult);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.enterBlock("RefreshAnnotatedCallLogReceiver.onReceive");

        String action = intent.getAction();

        if (IntentNames.ACTION_REFRESH_ANNOTATED_CALL_LOG.equals(action)) {
            boolean checkDirty = intent.getBooleanExtra(IntentNames.EXTRA_CHECK_DIRTY, false);
            refreshAnnotatedCallLog(checkDirty);
        } else if (IntentNames.ACTION_CANCEL_REFRESHING_ANNOTATED_CALL_LOG.equals(action)) {
            cancelRefreshingAnnotatedCallLog();
        }
    }

    /**
     * Request a refresh of the annotated call log.
     *
     * <p>Note that the execution will be delayed by {@link #REFRESH_ANNOTATED_CALL_LOG_WAIT_MILLIS}.
     * Once the work begins, it can't be cancelled.
     *
     * @see #cancelRefreshingAnnotatedCallLog()
     */
    private void refreshAnnotatedCallLog(boolean checkDirty) {
        LogUtil.enterBlock("RefreshAnnotatedCallLogReceiver.refreshAnnotatedCallLog");

        // If we already scheduled a refresh, cancel it and schedule a new one so that repeated requests
        // in quick succession don't result in too much work. For example, if we get 10 requests in
        // 10ms, and a complete refresh takes a constant 200ms, the refresh will take 300ms (100ms wait
        // and 1 iteration @200ms) instead of 2 seconds (10 iterations @ 200ms) since the work requests
        // are serialized in RefreshAnnotatedCallLogWorker.
        //
        // We might get many requests in quick succession, for example, when the simulator inserts
        // hundreds of rows into the system call log, or when the data for a new call is incrementally
        // written to different columns as it becomes available.
        ThreadUtil.getUiThreadHandler().removeCallbacks(refreshAnnotatedCallLogRunnable);

        refreshAnnotatedCallLogRunnable =
                () -> {
                    ListenableFuture<RefreshResult> future =
                            checkDirty
                                    ? refreshAnnotatedCallLogWorker.refreshWithDirtyCheck()
                                    : refreshAnnotatedCallLogWorker.refreshWithoutDirtyCheck();
                    Futures.addCallback(
                            future,
                            new FutureCallback<RefreshResult>() {
                                @Override
                                public void onSuccess(RefreshResult refreshResult) {
                                    logger.logImpression(getImpressionType(checkDirty, refreshResult));
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    ThreadUtil.getUiThreadHandler()
                                            .post(
                                                    () -> {
                                                        throw new RuntimeException(throwable);
                                                    });
                                }
                            },
                            MoreExecutors.directExecutor());
                    futureTimer.applyTiming(future, new EventNameFromResultFunction(checkDirty));
                };

        ThreadUtil.getUiThreadHandler()
                .postDelayed(refreshAnnotatedCallLogRunnable, REFRESH_ANNOTATED_CALL_LOG_WAIT_MILLIS);
    }

    /**
     * When a refresh is requested, its execution is delayed (see {@link
     * #refreshAnnotatedCallLog(boolean)}). This method only cancels the refresh if it hasn't started.
     */
    private void cancelRefreshingAnnotatedCallLog() {
        LogUtil.enterBlock("RefreshAnnotatedCallLogReceiver.cancelRefreshingAnnotatedCallLog");

        ThreadUtil.getUiThreadHandler().removeCallbacks(refreshAnnotatedCallLogRunnable);
    }

    private static class EventNameFromResultFunction implements Function<RefreshResult, String> {

        private final boolean checkDirty;

        private EventNameFromResultFunction(boolean checkDirty) {
            this.checkDirty = checkDirty;
        }

        @Override
        public String apply(RefreshResult refreshResult) {
            switch (refreshResult) {
                case NOT_DIRTY:
                    return Metrics.ANNOTATED_CALL_LOG_NOT_DIRTY; // NOT_DIRTY implies forceRefresh is false
                case REBUILT_BUT_NO_CHANGES_NEEDED:
                    return checkDirty
                            ? Metrics.ANNOTATED_LOG_NO_CHANGES_NEEDED
                            : Metrics.NEW_CALL_LOG_FORCE_REFRESH_NO_CHANGES_NEEDED;
                case REBUILT_AND_CHANGES_NEEDED:
                    return checkDirty
                            ? Metrics.ANNOTATED_CALL_LOG_CHANGES_NEEDED
                            : Metrics.ANNOTATED_CALL_LOG_FORCE_REFRESH_CHANGES_NEEDED;
                default:
                    throw new IllegalStateException("Unsupported result: " + refreshResult);
            }
        }
    }
}
