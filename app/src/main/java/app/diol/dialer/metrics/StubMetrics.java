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

package app.diol.dialer.metrics;

import android.os.SystemClock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Stub {@link Metrics} which simply logs debug messages to logcat.
 */
@ThreadSafe
@Singleton
public final class StubMetrics implements Metrics {

    private final ConcurrentMap<String, StubTimerEvent> namedEvents = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, StubTimerEvent> unnamedEvents = new ConcurrentHashMap<>();

    @Inject
    StubMetrics() {
    }

    @Override
    public void startTimer(String timerEventName) {
        namedEvents.put(timerEventName, new StubTimerEvent());
    }

    @Override
    public Integer startUnnamedTimer() {
        StubTimerEvent stubTimerEvent = new StubTimerEvent();
        int id = stubTimerEvent.hashCode();
        LogUtil.d("StubMetrics.startUnnamedTimer", "started timer for id: %d", id);
        unnamedEvents.put(id, stubTimerEvent);
        return id;
    }

    @Override
    public void stopTimer(String timerEventName) {
        StubTimerEvent stubTimerEvent = namedEvents.remove(timerEventName);
        if (stubTimerEvent == null) {
            return;
        }

        LogUtil.d(
                "StubMetrics.stopTimer",
                "%s took %dms",
                timerEventName,
                SystemClock.elapsedRealtime() - stubTimerEvent.startTime);
    }

    @Override
    public void stopUnnamedTimer(int timerId, String timerEventName) {
        long startTime =
                Assert.isNotNull(
                        unnamedEvents.remove(timerId),
                        "no timer found for id: %d (%s)",
                        timerId,
                        timerEventName)
                        .startTime;

        LogUtil.d(
                "StubMetrics.stopUnnamedTimer",
                "%s took %dms",
                timerEventName,
                SystemClock.elapsedRealtime() - startTime);
    }

    @Override
    public void startJankRecorder(String eventName) {
        LogUtil.d("StubMetrics.startJankRecorder", "started jank recorder for %s", eventName);
    }

    @Override
    public void stopJankRecorder(String eventName) {
        LogUtil.d("StubMetrics.startJankRecorder", "stopped jank recorder for %s", eventName);
    }

    @Override
    public void recordMemory(String memoryEventName) {
        LogUtil.d("StubMetrics.startJankRecorder", "recorded memory for %s", memoryEventName);
    }

    @Override
    public void recordBattery(String batteryEventName) {
        LogUtil.d("StubMetrics.recordBattery", "recorded battery for %s", batteryEventName);
    }

    private static class StubTimerEvent {
        final long startTime;

        StubTimerEvent() {
            this.startTime = SystemClock.elapsedRealtime();
        }
    }
}
