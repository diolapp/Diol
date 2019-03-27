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

package app.diol.dialer.performancereport;

import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.logging.UiAction;

/**
 * Tracks UI performance for a call.
 */
public final class PerformanceReport {

    private static final long INVALID_TIME = -1;
    private static final long ACTIVE_DURATION = TimeUnit.MINUTES.toMillis(5);

    private static final List<UiAction.Type> actions = new ArrayList<>();
    private static final List<Long> actionTimestamps = new ArrayList<>();
    private static boolean recording = false;
    private static long appLaunchTimeMillis = INVALID_TIME;
    private static long firstClickTimeMillis = INVALID_TIME;
    private static long lastActionTimeMillis = INVALID_TIME;
    @Nullable
    private static UiAction.Type ignoreActionOnce = null;
    private static final RecyclerView.OnScrollListener recordOnScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                        PerformanceReport.recordClick(UiAction.Type.SCROLL);
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }
            };
    private static int startingTabIndex = -1; // UNKNOWN

    private PerformanceReport() {
    }

    public static void startRecording() {
        LogUtil.enterBlock("PerformanceReport.startRecording");

        appLaunchTimeMillis = SystemClock.elapsedRealtime();
        lastActionTimeMillis = appLaunchTimeMillis;
        if (!actions.isEmpty()) {
            actions.clear();
            actionTimestamps.clear();
        }
        recording = true;
    }

    public static void stopRecording() {
        LogUtil.enterBlock("PerformanceReport.stopRecording");
        recording = false;
    }

    public static void recordClick(UiAction.Type action) {
        if (!recording) {
            return;
        }

        if (action == ignoreActionOnce) {
            LogUtil.i("PerformanceReport.recordClick", "%s is ignored", action.toString());
            ignoreActionOnce = null;
            return;
        }
        ignoreActionOnce = null;

        LogUtil.v("PerformanceReport.recordClick", action.toString());

        // Timeout
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastActionTimeMillis > ACTIVE_DURATION) {
            startRecording();
            recordClick(action);
            return;
        }

        lastActionTimeMillis = currentTime;
        if (actions.isEmpty()) {
            firstClickTimeMillis = currentTime;
        }
        actions.add(action);
        actionTimestamps.add(currentTime - appLaunchTimeMillis);
    }

    public static void recordScrollStateChange(int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            recordClick(UiAction.Type.SCROLL);
        }
    }

    public static void logOnScrollStateChange(RecyclerView recyclerView) {
        // Remove the listener in case it was added before
        recyclerView.removeOnScrollListener(recordOnScrollListener);
        recyclerView.addOnScrollListener(recordOnScrollListener);
    }

    public static boolean isRecording() {
        return recording;
    }

    public static long getTimeSinceAppLaunch() {
        if (appLaunchTimeMillis == INVALID_TIME) {
            return INVALID_TIME;
        }
        return SystemClock.elapsedRealtime() - appLaunchTimeMillis;
    }

    public static long getTimeSinceFirstClick() {
        if (firstClickTimeMillis == INVALID_TIME) {
            return INVALID_TIME;
        }
        return SystemClock.elapsedRealtime() - firstClickTimeMillis;
    }

    public static List<UiAction.Type> getActions() {
        return actions;
    }

    public static List<Long> getActionTimestamps() {
        return actionTimestamps;
    }

    public static int getStartingTabIndex() {
        return startingTabIndex;
    }

    public static void setStartingTabIndex(int startingTabIndex) {
        PerformanceReport.startingTabIndex = startingTabIndex;
    }

    public static void setIgnoreActionOnce(@Nullable UiAction.Type ignoreActionOnce) {
        PerformanceReport.ignoreActionOnce = ignoreActionOnce;
        LogUtil.i(
                "PerformanceReport.setIgnoreActionOnce",
                "next action will be ignored once if it is %s",
                ignoreActionOnce.toString());
    }
}
