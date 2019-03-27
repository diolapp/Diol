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

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.os.UserManagerCompat;
import android.telecom.VideoProfile;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.common.concurrent.ThreadUtil;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.incallui.answer.protocol.AnswerScreen;
import app.diol.incallui.answer.protocol.AnswerScreenDelegate;
import app.diol.incallui.answerproximitysensor.AnswerProximitySensor;
import app.diol.incallui.answerproximitysensor.PseudoScreenState;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.DialerCallListener;
import app.diol.incallui.incalluilock.InCallUiLock;

/**
 * Manages changes for an incoming call screen.
 */
public class AnswerScreenPresenter
        implements AnswerScreenDelegate, DialerCall.CannedTextResponsesLoadedListener {
    private static final int ACCEPT_REJECT_CALL_TIME_OUT_IN_MILLIS = 5000;

    @NonNull
    private final Context context;
    @NonNull
    private final AnswerScreen answerScreen;
    @NonNull
    private final DialerCall call;
    private long actionPerformedTimeMillis;

    AnswerScreenPresenter(
            @NonNull Context context, @NonNull AnswerScreen answerScreen, @NonNull DialerCall call) {
        LogUtil.i("AnswerScreenPresenter.constructor", null);
        this.context = Assert.isNotNull(context);
        this.answerScreen = Assert.isNotNull(answerScreen);
        this.call = Assert.isNotNull(call);
        if (isSmsResponseAllowed(call)) {
            answerScreen.setTextResponses(call.getCannedSmsResponses());
        }
        call.addCannedTextResponsesLoadedListener(this);

        PseudoScreenState pseudoScreenState = InCallPresenter.getInstance().getPseudoScreenState();
        if (AnswerProximitySensor.shouldUse(context, call)) {
            new AnswerProximitySensor(context, call, pseudoScreenState);
        } else {
            pseudoScreenState.setOn(true);
        }
    }

    @Override
    public boolean isActionTimeout() {
        return actionPerformedTimeMillis != 0
                && SystemClock.elapsedRealtime() - actionPerformedTimeMillis
                >= ACCEPT_REJECT_CALL_TIME_OUT_IN_MILLIS;
    }

    @Override
    public InCallUiLock acquireInCallUiLock(String tag) {
        return InCallPresenter.getInstance().acquireInCallUiLock(tag);
    }

    @Override
    public void onAnswerScreenUnready() {
        call.removeCannedTextResponsesLoadedListener(this);
    }

    @Override
    public void onRejectCallWithMessage(String message) {
        call.reject(true /* rejectWithMessage */, message);
        addTimeoutCheck();
    }

    @Override
    public void onAnswer(boolean answerVideoAsAudio) {

        DialerCall incomingCall = CallList.getInstance().getIncomingCall();
        InCallActivity inCallActivity =
                (InCallActivity) answerScreen.getAnswerScreenFragment().getActivity();
        ListenableFuture<Void> answerPrecondition;

        if (incomingCall != null && inCallActivity != null) {
            answerPrecondition = inCallActivity.getSpeakEasyCallManager().onNewIncomingCall(incomingCall);
        } else {
            answerPrecondition = Futures.immediateFuture(null);
        }

        Futures.addCallback(
                answerPrecondition,
                new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        onAnswerCallback(answerVideoAsAudio);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        onAnswerCallback(answerVideoAsAudio);
                        // TODO(erfanian): Enumerate all error states and specify recovery strategies.
                        throw new RuntimeException("Failed to successfully complete pre call tasks.", t);
                    }
                },
                DialerExecutorComponent.get(context).uiExecutor());
        addTimeoutCheck();
    }

    private void onAnswerCallback(boolean answerVideoAsAudio) {

        if (answerScreen.isVideoUpgradeRequest()) {
            if (answerVideoAsAudio) {
                Logger.get(context)
                        .logCallImpression(
                                DialerImpression.Type.VIDEO_CALL_REQUEST_ACCEPTED_AS_AUDIO,
                                call.getUniqueCallId(),
                                call.getTimeAddedMs());
                call.getVideoTech().acceptVideoRequestAsAudio();
            } else {
                Logger.get(context)
                        .logCallImpression(
                                DialerImpression.Type.VIDEO_CALL_REQUEST_ACCEPTED,
                                call.getUniqueCallId(),
                                call.getTimeAddedMs());
                call.getVideoTech().acceptVideoRequest(context);
            }
        } else {
            if (answerVideoAsAudio) {
                call.answer(VideoProfile.STATE_AUDIO_ONLY);
            } else {
                call.answer();
            }
        }
    }

    @Override
    public void onReject() {
        if (answerScreen.isVideoUpgradeRequest()) {
            Logger.get(context)
                    .logCallImpression(
                            DialerImpression.Type.VIDEO_CALL_REQUEST_DECLINED,
                            call.getUniqueCallId(),
                            call.getTimeAddedMs());
            call.getVideoTech().declineVideoRequest();
        } else {
            call.reject(false /* rejectWithMessage */, null);
        }
        addTimeoutCheck();
    }

    @Override
    public void onSpeakEasyCall() {
        LogUtil.enterBlock("AnswerScreenPresenter.onSpeakEasyCall");
        DialerCall incomingCall = CallList.getInstance().getIncomingCall();
        if (incomingCall == null) {
            LogUtil.i("AnswerScreenPresenter.onSpeakEasyCall", "incomingCall == null");
            return;
        }
        incomingCall.setIsSpeakEasyCall(true);
    }

    @Override
    public void onAnswerAndReleaseCall() {
        LogUtil.enterBlock("AnswerScreenPresenter.onAnswerAndReleaseCall");
        DialerCall activeCall = CallList.getInstance().getActiveCall();
        if (activeCall == null) {
            LogUtil.i("AnswerScreenPresenter.onAnswerAndReleaseCall", "activeCall == null");
            onAnswer(false);
        } else {
            activeCall.setReleasedByAnsweringSecondCall(true);
            activeCall.addListener(new AnswerOnDisconnected(activeCall));
            activeCall.disconnect();
        }
        addTimeoutCheck();
    }

    @Override
    public void onAnswerAndReleaseButtonDisabled() {
        DialerCall activeCall = CallList.getInstance().getActiveCall();
        if (activeCall != null) {
            activeCall.increaseSecondCallWithoutAnswerAndReleasedButtonTimes();
        }
    }

    @Override
    public void onAnswerAndReleaseButtonEnabled() {
        DialerCall activeCall = CallList.getInstance().getActiveCall();
        if (activeCall != null) {
            activeCall.increaseAnswerAndReleaseButtonDisplayedTimes();
        }
    }

    @Override
    public void onCannedTextResponsesLoaded(DialerCall call) {
        if (isSmsResponseAllowed(call)) {
            answerScreen.setTextResponses(call.getCannedSmsResponses());
        }
    }

    @Override
    public void updateWindowBackgroundColor(@FloatRange(from = -1f, to = 1.0f) float progress) {
        InCallActivity activity = (InCallActivity) answerScreen.getAnswerScreenFragment().getActivity();
        if (activity != null) {
            activity.updateWindowBackgroundColor(progress);
        }
    }

    private boolean isSmsResponseAllowed(DialerCall call) {
        return UserManagerCompat.isUserUnlocked(context)
                && call.can(android.telecom.Call.Details.CAPABILITY_RESPOND_VIA_TEXT);
    }

    private void addTimeoutCheck() {
        actionPerformedTimeMillis = SystemClock.elapsedRealtime();
        if (answerScreen.getAnswerScreenFragment().isVisible()) {
            ThreadUtil.postDelayedOnUiThread(
                    () -> {
                        if (!answerScreen.getAnswerScreenFragment().isVisible()) {
                            LogUtil.d(
                                    "AnswerScreenPresenter.addTimeoutCheck",
                                    "accept/reject call timed out, do nothing");
                            return;
                        }
                        LogUtil.i("AnswerScreenPresenter.addTimeoutCheck", "accept/reject call timed out");
                        // Force re-evaluate which fragment to show.
                        InCallPresenter.getInstance().refreshUi();
                    },
                    ACCEPT_REJECT_CALL_TIME_OUT_IN_MILLIS);
        }
    }

    private class AnswerOnDisconnected implements DialerCallListener {

        private final DialerCall disconnectingCall;

        AnswerOnDisconnected(DialerCall disconnectingCall) {
            this.disconnectingCall = disconnectingCall;
        }

        @Override
        public void onDialerCallDisconnect() {
            LogUtil.i(
                    "AnswerScreenPresenter.AnswerOnDisconnected", "call disconnected, answering new call");
            call.answer();
            disconnectingCall.removeListener(this);
        }

        @Override
        public void onDialerCallUpdate() {
        }

        @Override
        public void onDialerCallChildNumberChange() {
        }

        @Override
        public void onDialerCallLastForwardedNumberChange() {
        }

        @Override
        public void onDialerCallUpgradeToVideo() {
        }

        @Override
        public void onDialerCallSessionModificationStateChange() {
        }

        @Override
        public void onWiFiToLteHandover() {
        }

        @Override
        public void onHandoverToWifiFailure() {
        }

        @Override
        public void onInternationalCallOnWifi() {
        }

        @Override
        public void onEnrichedCallSessionUpdate() {
        }
    }
}
