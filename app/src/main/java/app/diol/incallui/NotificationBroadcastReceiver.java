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
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.telecom.CallAudioState;
import android.telecom.VideoProfile;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.TelecomAdapter;
import app.diol.incallui.speakeasy.SpeakEasyCallManager;

/**
 * Accepts broadcast Intents which will be prepared by {@link StatusBarNotifier} and thus sent from
 * the notification manager. This should be visible from outside, but shouldn't be exported.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    /**
     * Intent Action used for hanging up the current call from Notification bar. This will choose
     * first ringing call, first active call, or first background call (typically in STATE_HOLDING
     * state).
     */
    public static final String ACTION_DECLINE_INCOMING_CALL =
            "app.diol.incallui.ACTION_DECLINE_INCOMING_CALL";

    public static final String ACTION_HANG_UP_ONGOING_CALL =
            "app.diol.incallui.ACTION_HANG_UP_ONGOING_CALL";
    public static final String ACTION_ANSWER_VIDEO_INCOMING_CALL =
            "app.diol.incallui.ACTION_ANSWER_VIDEO_INCOMING_CALL";
    public static final String ACTION_ANSWER_VOICE_INCOMING_CALL =
            "app.diol.incallui.ACTION_ANSWER_VOICE_INCOMING_CALL";
    public static final String ACTION_ACCEPT_VIDEO_UPGRADE_REQUEST =
            "app.diol.incallui.ACTION_ACCEPT_VIDEO_UPGRADE_REQUEST";
    public static final String ACTION_DECLINE_VIDEO_UPGRADE_REQUEST =
            "app.diol.incallui.ACTION_DECLINE_VIDEO_UPGRADE_REQUEST";
    public static final String ACTION_TURN_ON_SPEAKER = "app.diol.incallui.ACTION_TURN_ON_SPEAKER";
    public static final String ACTION_TURN_OFF_SPEAKER =
            "app.diol.incallui.ACTION_TURN_OFF_SPEAKER";
    public static final String ACTION_ANSWER_SPEAKEASY_CALL =
            "app.diol.incallui.ACTION_ANSWER_SPEAKEASY_CALL";

    @RequiresApi(VERSION_CODES.N_MR1)
    public static final String ACTION_PULL_EXTERNAL_CALL =
            "app.diol.incallui.ACTION_PULL_EXTERNAL_CALL";

    public static final String EXTRA_NOTIFICATION_ID =
            "app.diol.incallui.extra.EXTRA_NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogUtil.i("NotificationBroadcastReceiver.onReceive", "Broadcast from Notification: " + action);

        // TODO: Commands of this nature should exist in the CallList.
        if (action.equals(ACTION_ANSWER_VIDEO_INCOMING_CALL)) {
            answerIncomingCall(VideoProfile.STATE_BIDIRECTIONAL, context);
        } else if (action.equals(ACTION_ANSWER_VOICE_INCOMING_CALL)) {
            answerIncomingCall(VideoProfile.STATE_AUDIO_ONLY, context);
        } else if (action.equals(ACTION_ANSWER_SPEAKEASY_CALL)) {
            markIncomingCallAsSpeakeasyCall();
            answerIncomingCall(VideoProfile.STATE_AUDIO_ONLY, context);
        } else if (action.equals(ACTION_DECLINE_INCOMING_CALL)) {
            Logger.get(context)
                    .logImpression(DialerImpression.Type.REJECT_INCOMING_CALL_FROM_NOTIFICATION);
            declineIncomingCall();
        } else if (action.equals(ACTION_HANG_UP_ONGOING_CALL)) {
            hangUpOngoingCall();
        } else if (action.equals(ACTION_ACCEPT_VIDEO_UPGRADE_REQUEST)) {
            acceptUpgradeRequest(context);
        } else if (action.equals(ACTION_DECLINE_VIDEO_UPGRADE_REQUEST)) {
            declineUpgradeRequest();
        } else if (action.equals(ACTION_PULL_EXTERNAL_CALL)) {
            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
            InCallPresenter.getInstance().getExternalCallNotifier().pullExternalCall(notificationId);
        } else if (action.equals(ACTION_TURN_ON_SPEAKER)) {
            TelecomAdapter.getInstance().setAudioRoute(CallAudioState.ROUTE_SPEAKER);
        } else if (action.equals(ACTION_TURN_OFF_SPEAKER)) {
            TelecomAdapter.getInstance().setAudioRoute(CallAudioState.ROUTE_WIRED_OR_EARPIECE);
        }
    }

    private void acceptUpgradeRequest(Context context) {
        CallList callList = InCallPresenter.getInstance().getCallList();
        if (callList == null) {
            StatusBarNotifier.clearAllCallNotifications();
            LogUtil.e("NotificationBroadcastReceiver.acceptUpgradeRequest", "call list is empty");
        } else {
            DialerCall call = callList.getVideoUpgradeRequestCall();
            if (call != null) {
                call.getVideoTech().acceptVideoRequest(context);
            }
        }
    }

    private void declineUpgradeRequest() {
        CallList callList = InCallPresenter.getInstance().getCallList();
        if (callList == null) {
            StatusBarNotifier.clearAllCallNotifications();
            LogUtil.e("NotificationBroadcastReceiver.declineUpgradeRequest", "call list is empty");
        } else {
            DialerCall call = callList.getVideoUpgradeRequestCall();
            if (call != null) {
                call.getVideoTech().declineVideoRequest();
            }
        }
    }

    private void hangUpOngoingCall() {
        CallList callList = InCallPresenter.getInstance().getCallList();
        if (callList == null) {
            StatusBarNotifier.clearAllCallNotifications();
            LogUtil.e("NotificationBroadcastReceiver.hangUpOngoingCall", "call list is empty");
        } else {
            DialerCall call = callList.getOutgoingCall();
            if (call == null) {
                call = callList.getActiveOrBackgroundCall();
            }
            LogUtil.i(
                    "NotificationBroadcastReceiver.hangUpOngoingCall", "disconnecting call, call: " + call);
            if (call != null) {
                call.disconnect();
            }
        }
    }

    private void markIncomingCallAsSpeakeasyCall() {
        CallList callList = InCallPresenter.getInstance().getCallList();
        if (callList == null) {
            LogUtil.e(
                    "NotificationBroadcastReceiver.markIncomingCallAsSpeakeasyCall", "call list is empty");
        } else {
            DialerCall call = callList.getIncomingCall();
            if (call != null) {
                call.setIsSpeakEasyCall(true);
            }
        }
    }

    private void answerIncomingCall(int videoState, @NonNull Context context) {
        CallList callList = InCallPresenter.getInstance().getCallList();
        if (callList == null) {
            StatusBarNotifier.clearAllCallNotifications();
            LogUtil.e("NotificationBroadcastReceiver.answerIncomingCall", "call list is empty");
        } else {
            DialerCall call = callList.getIncomingCall();
            if (call != null) {

                SpeakEasyCallManager speakEasyCallManager =
                        InCallPresenter.getInstance().getSpeakEasyCallManager();
                ListenableFuture<Void> answerPrecondition;

                if (speakEasyCallManager != null) {
                    answerPrecondition = speakEasyCallManager.onNewIncomingCall(call);
                } else {
                    answerPrecondition = Futures.immediateFuture(null);
                }

                Futures.addCallback(
                        answerPrecondition,
                        new FutureCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                answerIncomingCallCallback(call, videoState);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                answerIncomingCallCallback(call, videoState);
                                // TODO(erfanian): Enumerate all error states and specify recovery strategies.
                                throw new RuntimeException("Failed to successfully complete pre call tasks.", t);
                            }
                        },
                        DialerExecutorComponent.get(context).uiExecutor());
            }
        }
    }

    private void answerIncomingCallCallback(@NonNull DialerCall call, int videoState) {
        call.answer(videoState);
        InCallPresenter.getInstance().showInCall(false /* showDialpad */, false /* newOutgoingCall */);
    }

    private void declineIncomingCall() {
        CallList callList = InCallPresenter.getInstance().getCallList();
        if (callList == null) {
            StatusBarNotifier.clearAllCallNotifications();
            LogUtil.e("NotificationBroadcastReceiver.declineIncomingCall", "call list is empty");
        } else {
            DialerCall call = callList.getIncomingCall();
            if (call != null) {
                call.reject(false /* rejectWithMessage */, null);
            }
        }
    }
}
