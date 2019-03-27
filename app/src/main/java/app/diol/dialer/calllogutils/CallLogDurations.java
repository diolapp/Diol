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

package app.diol.dialer.calllogutils;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.Formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.diol.R;
import app.diol.dialer.util.DialerUtils;

/**
 * Utility class for formatting duration and data usage in call log entries.
 */
public class CallLogDurations {

    private static CharSequence formatDuration(Context context, long elapsedSeconds) {
        // Getting this method into a good state took a bunch of work between eng, i18n team and
        // translators. If at all possible, the strings should not be changed or updated.
        long minutes = TimeUnit.SECONDS.toMinutes(elapsedSeconds);
        long seconds = elapsedSeconds - TimeUnit.MINUTES.toSeconds(minutes);
        Resources res = context.getResources();
        String formatPattern;
        if (elapsedSeconds >= 60) {
            String minutesString = res.getString(R.string.call_details_minutes_abbreviation);
            String secondsString = res.getString(R.string.call_details_seconds_abbreviation);
            // example output: "1m 1s"
            formatPattern =
                    context.getString(
                            R.string.call_duration_format_pattern,
                            Long.toString(minutes),
                            minutesString,
                            Long.toString(seconds),
                            secondsString);
        } else {
            String secondsString = res.getString(R.string.call_details_seconds_abbreviation);
            // example output: "1s"
            formatPattern =
                    context.getString(
                            R.string.call_duration_short_format_pattern, Long.toString(seconds), secondsString);
        }

        // Since we don't want to update the strings.xml, we need to remove the quotations from the
        // previous implementation.
        return formatPattern.replace("\'", "");
    }

    private static CharSequence formatDurationA11y(Context context, long elapsedSeconds) {
        Resources res = context.getResources();
        if (elapsedSeconds >= 60) {
            int minutes = (int) (elapsedSeconds / 60);
            int seconds = (int) elapsedSeconds - minutes * 60;
            String minutesString = res.getQuantityString(R.plurals.a11y_minutes, minutes);
            String secondsString = res.getQuantityString(R.plurals.a11y_seconds, seconds);
            // example output: "1 minute 1 second", "2 minutes 2 seconds", ect.
            return context.getString(
                    R.string.a11y_call_duration_format, minutes, minutesString, seconds, secondsString);
        } else {
            String secondsString = res.getQuantityString(R.plurals.a11y_seconds, (int) elapsedSeconds);
            // example output: "1 second", "2 seconds"
            return context.getString(
                    R.string.a11y_call_duration_short_format, elapsedSeconds, secondsString);
        }
    }

    /**
     * Formats a string containing the call duration and the data usage (if specified).
     *
     * @param elapsedSeconds Total elapsed seconds.
     * @param dataUsage      Data usage in bytes, or null if not specified.
     * @return String containing call duration and data usage.
     */
    public static CharSequence formatDurationAndDataUsage(
            Context context, long elapsedSeconds, long dataUsage) {
        return formatDurationAndDataUsageInternal(
                context, formatDuration(context, elapsedSeconds), dataUsage);
    }

    /**
     * Formats a string containing the call duration and the data usage (if specified) for TalkBack.
     *
     * @param elapsedSeconds Total elapsed seconds.
     * @param dataUsage      Data usage in bytes, or null if not specified.
     * @return String containing call duration and data usage.
     */
    public static CharSequence formatDurationAndDataUsageA11y(
            Context context, long elapsedSeconds, long dataUsage) {
        return formatDurationAndDataUsageInternal(
                context, formatDurationA11y(context, elapsedSeconds), dataUsage);
    }

    private static CharSequence formatDurationAndDataUsageInternal(
            Context context, CharSequence duration, long dataUsage) {
        List<CharSequence> durationItems = new ArrayList<>();
        if (dataUsage > 0) {
            durationItems.add(duration);
            durationItems.add(Formatter.formatShortFileSize(context, dataUsage));
            return DialerUtils.join(durationItems);
        } else {
            return duration;
        }
    }
}
