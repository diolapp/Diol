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
import android.provider.CallLog.Calls;
import android.support.v4.os.BuildCompat;

import app.diol.dialer.NumberAttributes;
import app.diol.dialer.calllog.model.CoalescedRow;
import app.diol.dialer.glidephotomanager.PhotoInfo;
import app.diol.dialer.spam.Spam;
import app.diol.dialer.voicemail.model.VoicemailEntry;

/**
 * Builds {@link PhotoInfo} from other data types.
 */
public final class PhotoInfoBuilder {

    /**
     * Returns a {@link PhotoInfo.Builder} with info from {@link CoalescedRow}.
     */
    public static PhotoInfo.Builder fromCoalescedRow(Context context, CoalescedRow coalescedRow) {
        return fromNumberAttributes(coalescedRow.getNumberAttributes())
                .setName(CallLogEntryText.buildPrimaryText(context, coalescedRow).toString())
                .setFormattedNumber(coalescedRow.getFormattedNumber())
                .setIsVoicemail(coalescedRow.getIsVoicemailCall())
                .setIsSpam(
                        Spam.shouldShowAsSpam(
                                coalescedRow.getNumberAttributes().getIsSpam(), coalescedRow.getCallType()))
                .setIsVideo((coalescedRow.getFeatures() & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO)
                .setIsRtt(
                        BuildCompat.isAtLeastP()
                                && (coalescedRow.getFeatures() & Calls.FEATURES_RTT) == Calls.FEATURES_RTT);
    }

    /**
     * Returns a {@link PhotoInfo.Builder} with info from {@link VoicemailEntry}.
     */
    public static PhotoInfo.Builder fromVoicemailEntry(VoicemailEntry voicemailEntry) {
        return fromNumberAttributes(voicemailEntry.getNumberAttributes())
                .setFormattedNumber(voicemailEntry.getFormattedNumber())
                .setIsSpam(
                        Spam.shouldShowAsSpam(
                                voicemailEntry.getNumberAttributes().getIsSpam(), voicemailEntry.getCallType()));
    }

    /**
     * Returns a {@link PhotoInfo.Builder} with info from {@link NumberAttributes}.
     */
    private static PhotoInfo.Builder fromNumberAttributes(NumberAttributes numberAttributes) {
        return PhotoInfo.newBuilder()
                .setName(numberAttributes.getName())
                .setPhotoUri(numberAttributes.getPhotoUri())
                .setPhotoId(numberAttributes.getPhotoId())
                .setLookupUri(numberAttributes.getLookupUri())
                .setIsBusiness(numberAttributes.getIsBusiness())
                .setIsBlocked(numberAttributes.getIsBlocked());
    }
}
