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

import android.app.Activity;
import android.provider.CallLog.Calls;

import app.diol.dialer.callintent.CallInitiationType;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.calllog.model.CoalescedRow;
import app.diol.dialer.duo.DuoComponent;
import app.diol.dialer.precall.PreCall;

/**
 * Actions which can be performed on a call log row.
 */
public final class CallLogRowActions {

    /**
     * Places a call to the number in the provided {@link CoalescedRow}.
     *
     * <p>If the call was a video call, a video call will be placed, and if the call was an audio
     * call, an audio call will be placed. The phone account corresponding to the row is used.
     */
    public static void startCallForRow(Activity activity, CoalescedRow row) {
        // TODO(zachh): More granular logging?
        PreCall.start(
                activity,
                new CallIntentBuilder(
                        row.getNumber().getNormalizedNumber(), CallInitiationType.Type.CALL_LOG)
                        .setIsVideoCall((row.getFeatures() & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO)
                        .setIsDuoCall(
                                DuoComponent.get(activity)
                                        .getDuo()
                                        .isDuoAccount(row.getPhoneAccountComponentName())));
    }
}
