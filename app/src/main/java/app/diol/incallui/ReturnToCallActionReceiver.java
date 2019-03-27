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

package app.diol.incallui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telecom.CallAudioState;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.incallui.audiomode.AudioModeProvider;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.TelecomAdapter;

/**
 * Handles clicks on the return-to-call bubble
 */
public class ReturnToCallActionReceiver extends BroadcastReceiver {

    public static final String ACTION_RETURN_TO_CALL = "returnToCallV2";
    public static final String ACTION_TOGGLE_SPEAKER = "toggleSpeakerV2";
    public static final String ACTION_SHOW_AUDIO_ROUTE_SELECTOR = "showAudioRouteSelectorV2";
    public static final String ACTION_TOGGLE_MUTE = "toggleMuteV2";
    public static final String ACTION_END_CALL = "endCallV2";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_RETURN_TO_CALL:
                returnToCall(context);
                break;
            case ACTION_TOGGLE_SPEAKER:
                toggleSpeaker(context);
                break;
            case ACTION_SHOW_AUDIO_ROUTE_SELECTOR:
                showAudioRouteSelector(context);
                break;
            case ACTION_TOGGLE_MUTE:
                toggleMute(context);
                break;
            case ACTION_END_CALL:
                endCall(context);
                break;
            default:
                throw Assert.createIllegalStateFailException(
                        "Invalid intent action: " + intent.getAction());
        }
    }

    private void returnToCall(Context context) {
        DialerCall call = getCall();
        Logger.get(context)
                .logCallImpression(
                        DialerImpression.Type.BUBBLE_V2_RETURN_TO_CALL,
                        call != null ? call.getUniqueCallId() : "",
                        call != null ? call.getTimeAddedMs() : 0);

        Intent activityIntent = InCallActivity.getIntent(context, false, false, false);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }

    private void toggleSpeaker(Context context) {
        CallAudioState audioState = AudioModeProvider.getInstance().getAudioState();

        if ((audioState.getSupportedRouteMask() & CallAudioState.ROUTE_BLUETOOTH)
                == CallAudioState.ROUTE_BLUETOOTH) {
            LogUtil.w(
                    "ReturnToCallActionReceiver.toggleSpeaker",
                    "toggleSpeaker() called when bluetooth available."
                            + " Probably should have shown audio route selector");
        }

        DialerCall call = getCall();

        int newRoute;
        if (audioState.getRoute() == CallAudioState.ROUTE_SPEAKER) {
            newRoute = CallAudioState.ROUTE_WIRED_OR_EARPIECE;
            Logger.get(context)
                    .logCallImpression(
                            DialerImpression.Type.BUBBLE_V2_WIRED_OR_EARPIECE,
                            call != null ? call.getUniqueCallId() : "",
                            call != null ? call.getTimeAddedMs() : 0);
        } else {
            newRoute = CallAudioState.ROUTE_SPEAKER;
            Logger.get(context)
                    .logCallImpression(
                            DialerImpression.Type.BUBBLE_V2_SPEAKERPHONE,
                            call != null ? call.getUniqueCallId() : "",
                            call != null ? call.getTimeAddedMs() : 0);
        }
        TelecomAdapter.getInstance().setAudioRoute(newRoute);
    }

    public void showAudioRouteSelector(Context context) {
        Intent intent = new Intent(context, AudioRouteSelectorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(intent);
    }

    private void toggleMute(Context context) {
        DialerCall call = getCall();
        boolean shouldMute = !AudioModeProvider.getInstance().getAudioState().isMuted();
        Logger.get(context)
                .logCallImpression(
                        shouldMute
                                ? DialerImpression.Type.BUBBLE_V2_MUTE_CALL
                                : DialerImpression.Type.BUBBLE_V2_UNMUTE_CALL,
                        call != null ? call.getUniqueCallId() : "",
                        call != null ? call.getTimeAddedMs() : 0);
        TelecomAdapter.getInstance().mute(shouldMute);
    }

    private void endCall(Context context) {
        DialerCall call = getCall();

        Logger.get(context)
                .logCallImpression(
                        DialerImpression.Type.BUBBLE_V2_END_CALL,
                        call != null ? call.getUniqueCallId() : "",
                        call != null ? call.getTimeAddedMs() : 0);
        if (call != null) {
            call.disconnect();
        }
    }

    private DialerCall getCall() {
        CallList callList = InCallPresenter.getInstance().getCallList();
        if (callList != null) {
            DialerCall call = callList.getOutgoingCall();
            if (call == null) {
                call = callList.getActiveOrBackgroundCall();
            }
            if (call != null) {
                return call;
            }
        }
        return null;
    }
}
