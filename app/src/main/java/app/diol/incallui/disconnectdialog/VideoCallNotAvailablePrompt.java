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

package app.diol.incallui.disconnectdialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.util.Pair;

import app.diol.R;
import app.diol.contacts.common.compat.telecom.TelecomManagerCompat;
import app.diol.dialer.callintent.CallInitiationType;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.precall.PreCall;
import app.diol.incallui.call.DialerCall;

/**
 * Prompt user to make voice call if video call is not currently available.
 */
public class VideoCallNotAvailablePrompt implements DisconnectDialog {

    @Override
    public boolean shouldShow(DisconnectCause disconnectCause) {
        if (disconnectCause.getCode() == DisconnectCause.ERROR
                && TelecomManagerCompat.REASON_IMS_ACCESS_BLOCKED.equals(disconnectCause.getReason())) {
            LogUtil.i(
                    "VideoCallNotAvailablePrompt.shouldShowPrompt",
                    "showing prompt for disconnect cause: %s",
                    disconnectCause.getReason());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Pair<Dialog, CharSequence> createDialog(@NonNull Context context, DialerCall call) {
        CharSequence title = context.getString(R.string.video_call_not_available_title);

        Dialog dialog =
                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setMessage(context.getString(R.string.video_call_not_available_message))
                        .setPositiveButton(
                                R.string.voice_call,
                                (dialog1, which) ->
                                        makeVoiceCall(context, call.getNumber(), call.getAccountHandle()))
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
        return new Pair<>(dialog, title);
    }

    private void makeVoiceCall(Context context, String number, PhoneAccountHandle accountHandle) {
        LogUtil.enterBlock("VideoCallNotAvailablePrompt.makeVoiceCall");
        PreCall.start(
                context,
                new CallIntentBuilder(number, CallInitiationType.Type.IMS_VIDEO_BLOCKED_FALLBACK_TO_VOICE)
                        .setPhoneAccountHandle(accountHandle));
    }
}
