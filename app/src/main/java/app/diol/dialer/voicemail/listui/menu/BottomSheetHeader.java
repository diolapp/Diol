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

package app.diol.dialer.voicemail.listui.menu;

import android.text.TextUtils;

import app.diol.dialer.calllogutils.PhotoInfoBuilder;
import app.diol.dialer.historyitemactions.HistoryItemBottomSheetHeaderInfo;
import app.diol.dialer.voicemail.model.VoicemailEntry;

/**
 * Configures the top row in the bottom sheet for the Voicemail Tab
 */
final class BottomSheetHeader {

    static HistoryItemBottomSheetHeaderInfo fromVoicemailEntry(VoicemailEntry voicemailEntry) {
        return HistoryItemBottomSheetHeaderInfo.newBuilder()
                .setNumber(voicemailEntry.getNumber())
                .setPhotoInfo(PhotoInfoBuilder.fromVoicemailEntry(voicemailEntry))
                .setPrimaryText(buildPrimaryVoicemailText(voicemailEntry))
                .setSecondaryText(buildSecondaryVoicemailText(voicemailEntry))
                .build();
    }

    private static String buildSecondaryVoicemailText(VoicemailEntry voicemailEntry) {
        return voicemailEntry.getGeocodedLocation();
    }

    private static String buildPrimaryVoicemailText(VoicemailEntry data) {
        StringBuilder primaryText = new StringBuilder();
        if (!TextUtils.isEmpty(data.getNumberAttributes().getName())) {
            primaryText.append(data.getNumberAttributes().getName());
        } else if (!TextUtils.isEmpty(data.getFormattedNumber())) {
            primaryText.append(data.getFormattedNumber());
        } else {
            // TODO(uabdullah): Handle CallLog.Calls.PRESENTATION_*, including Verizon restricted numbers.
            // primaryText.append(context.getText(R.string.voicemail_unknown));
            // TODO(uabdullah): Figure out why http://gpaste/5980163120562176 error when using string
            primaryText.append("Unknown");
        }
        return primaryText.toString();
    }
}
