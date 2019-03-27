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

package app.diol.contacts.common.util;

import java.util.ArrayList;

import app.diol.dialer.common.LogUtil;

/**
 * A {@link StopWatch} records start, laps and stop, and print them to logcat.
 */
public class StopWatch {

    private final String mLabel;

    private final ArrayList<Long> mTimes = new ArrayList<>();
    private final ArrayList<String> mLapLabels = new ArrayList<>();

    private StopWatch(String label) {
        mLabel = label;
        lap("");
    }

    /**
     * Create a new instance and start it.
     */
    public static StopWatch start(String label) {
        return new StopWatch(label);
    }

    /**
     * Record a lap.
     */
    public void lap(String lapLabel) {
        mTimes.add(System.currentTimeMillis());
        mLapLabels.add(lapLabel);
    }

    /**
     * Stop it and log the result, if the total time >= {@code timeThresholdToLog}.
     */
    public void stopAndLog(String TAG, int timeThresholdToLog) {

        lap("");

        final long start = mTimes.get(0);
        final long stop = mTimes.get(mTimes.size() - 1);

        final long total = stop - start;
        if (total < timeThresholdToLog) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(mLabel);
        sb.append(",");
        sb.append(total);
        sb.append(": ");

        long last = start;
        for (int i = 1; i < mTimes.size(); i++) {
            final long current = mTimes.get(i);
            sb.append(mLapLabels.get(i));
            sb.append(",");
            sb.append((current - last));
            sb.append(" ");
            last = current;
        }
        LogUtil.v(TAG, sb.toString());
    }
}
