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

package app.diol.dialer.voicemail.listui;

import android.content.Context;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import app.diol.R;
import app.diol.dialer.calllogutils.CallLogDates;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.time.Clock;
import app.diol.dialer.voicemail.model.VoicemailEntry;

/**
 * Computes the primary text for voicemail entries.
 *
 * <p>These text values are shown in the voicemail tab.
 */
public class VoicemailEntryText {

    public static String buildPrimaryVoicemailText(Context context, VoicemailEntry data) {
        StringBuilder primaryText = new StringBuilder();
        if (!TextUtils.isEmpty(data.getNumberAttributes().getName())) {
            primaryText.append(data.getNumberAttributes().getName());
        } else if (!TextUtils.isEmpty(data.getFormattedNumber())) {
            primaryText.append(data.getFormattedNumber());
        } else {
            // TODO(uabdullah): Handle CallLog.Calls.PRESENTATION_*, including Verizon restricted numbers.
            primaryText.append(context.getText(R.string.voicemail_entry_unknown));
        }
        return primaryText.toString();
    }

    /**
     * Uses the new date and location formatting rules to format the location and date in the new
     * voicemail tab.
     *
     * <p>Rules: $Location • Date
     *
     * <p>Examples:
     *
     * <p>Jun 20 San Francisco • Now
     *
     * <p>Markham, ON • Jul 27
     *
     * <p>Toledo, OH • 12:15 PM
     *
     * <p>Date rules: if < 1 minute ago: "Now"; else if today: HH:MM(am|pm); else if < 3 days: day;
     * else: MON D *
     *
     * @return $Location • Date
     */
    public static String buildSecondaryVoicemailText(
            Context context, Clock clock, VoicemailEntry voicemailEntry) {
        return secondaryTextPrefix(context, clock, voicemailEntry);
    }

    private static String secondaryTextPrefix(
            Context context, Clock clock, VoicemailEntry voicemailEntry) {
        StringBuilder secondaryText = new StringBuilder();
        String location = voicemailEntry.getGeocodedLocation();
        if (!TextUtils.isEmpty(location)) {
            secondaryText.append(location);
        }
        if (secondaryText.length() > 0) {
            secondaryText.append(" • ");
        }
        secondaryText.append(
                CallLogDates.newCallLogTimestampLabel(
                        context,
                        clock.currentTimeMillis(),
                        voicemailEntry.getTimestamp(),
                        /* abbreviateDateTime = */ true));

        long duration = voicemailEntry.getDuration();
        if (duration >= 0) {
            secondaryText.append(" • ");
            String formattedDuration = getVoicemailDuration(context, voicemailEntry);
            secondaryText.append(formattedDuration);
        }
        return secondaryText.toString();
    }

    static String getVoicemailDuration(Context context, VoicemailEntry voicemailEntry) {
        long minutes = TimeUnit.SECONDS.toMinutes(voicemailEntry.getDuration());
        long seconds = voicemailEntry.getDuration() - TimeUnit.MINUTES.toSeconds(minutes);

        // The format for duration is "MM:SS" and we never expect the duration to be > 5 minutes
        // However an incorrect duration could be set by the framework/someone to be >99, and in that
        // case cap it at 99, for the UI to still be able to display it in "MM:SS" format.
        if (minutes > 99) {
            LogUtil.w(
                    "VoicemailEntryText.getVoicemailDuration",
                    "Duration was %d",
                    voicemailEntry.getDuration());
            minutes = 99;
        }
        return context.getString(R.string.voicemailDurationFormat, minutes, seconds);
    }
}
