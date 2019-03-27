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

import android.annotation.TargetApi;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.telecom.Call.RttCall;

import java.io.IOException;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.ThreadUtil;
import app.diol.dialer.rtt.RttTranscript;
import app.diol.incallui.InCallPresenter.InCallState;
import app.diol.incallui.InCallPresenter.InCallStateListener;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.rtt.protocol.RttCallScreen;
import app.diol.incallui.rtt.protocol.RttCallScreenDelegate;

/**
 * Logic related to the {@link RttCallScreen} and for managing changes to the RTT calling surfaces
 * based on other user interface events and incoming events.
 */
@TargetApi(28)
public class RttCallPresenter implements RttCallScreenDelegate, InCallStateListener {

    private RttCallScreen rttCallScreen;
    private RttCall rttCall;
    private HandlerThread handlerThread;
    private RemoteMessageHandler remoteMessageHandler;

    @Override
    public void initRttCallScreenDelegate(RttCallScreen rttCallScreen) {
        this.rttCallScreen = rttCallScreen;
    }

    @Override
    public void onLocalMessage(String message) {
        if (rttCall == null) {
            LogUtil.w("RttCallPresenter.onLocalMessage", "Rtt Call is not started yet");
            return;
        }
        remoteMessageHandler.writeMessage(message);
    }

    @Override
    public void onRttCallScreenUiReady() {
        LogUtil.enterBlock("RttCallPresenter.onRttCallScreenUiReady");
        InCallPresenter.getInstance().addListener(this);
        startListenOnRemoteMessage();
        DialerCall call = CallList.getInstance().getCallById(rttCallScreen.getCallId());
        if (call != null) {
            rttCallScreen.onRestoreRttChat(call.getRttTranscript());
        }
    }

    @Override
    public void onSaveRttTranscript() {
        LogUtil.enterBlock("RttCallPresenter.onSaveRttTranscript");
        DialerCall call = CallList.getInstance().getCallById(rttCallScreen.getCallId());
        if (call != null) {
            saveTranscript(call);
        }
    }

    @Override
    public void onRttCallScreenUiUnready() {
        LogUtil.enterBlock("RttCallPresenter.onRttCallScreenUiUnready");
        InCallPresenter.getInstance().removeListener(this);
        stopListenOnRemoteMessage();
        onSaveRttTranscript();
    }

    private void saveTranscript(DialerCall dialerCall) {
        LogUtil.enterBlock("RttCallPresenter.saveTranscript");
        RttTranscript.Builder builder = RttTranscript.newBuilder();
        builder

                .setId(String.valueOf(dialerCall.getCreationTimeMillis()))

                .setTimestamp(dialerCall.getCreationTimeMillis())
                .setNumber(dialerCall.getNumber())
                .addAllMessages(rttCallScreen.getRttTranscriptMessageList());
        dialerCall.setRttTranscript(builder.build());
    }

    @Override
    public void onStateChange(InCallState oldState, InCallState newState, CallList callList) {
        LogUtil.enterBlock("RttCallPresenter.onStateChange");
        if (newState == InCallState.INCALL) {
            startListenOnRemoteMessage();
        }
    }

    private void startListenOnRemoteMessage() {
        DialerCall call = CallList.getInstance().getCallById(rttCallScreen.getCallId());
        if (call == null) {
            LogUtil.i("RttCallPresenter.startListenOnRemoteMessage", "call does not exist");
            return;
        }
        rttCall = call.getRttCall();
        if (rttCall == null) {
            LogUtil.i("RttCallPresenter.startListenOnRemoteMessage", "RTT Call is not started yet");
            return;
        }
        if (handlerThread != null && handlerThread.isAlive()) {
            LogUtil.i("RttCallPresenter.startListenOnRemoteMessage", "already running");
            return;
        }
        handlerThread = new HandlerThread("RttCallRemoteMessageHandler");
        handlerThread.start();
        remoteMessageHandler =
                new RemoteMessageHandler(handlerThread.getLooper(), rttCall, rttCallScreen);
        remoteMessageHandler.start();
    }

    private void stopListenOnRemoteMessage() {
        if (handlerThread != null && handlerThread.isAlive()) {
            handlerThread.quit();
        }
    }

    private static class RemoteMessageHandler extends Handler {
        private static final int START = 1;
        private static final int READ_MESSAGE = 2;
        private static final int WRITE_MESSAGE = 3;

        private final RttCall rttCall;
        private final RttCallScreen rttCallScreen;

        RemoteMessageHandler(Looper looper, RttCall rttCall, RttCallScreen rttCallScreen) {
            super(looper);
            this.rttCall = rttCall;
            this.rttCallScreen = rttCallScreen;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case START:
                    sendEmptyMessage(READ_MESSAGE);
                    break;
                case READ_MESSAGE:
                    try {
                        final String message = rttCall.readImmediately();
                        if (message != null) {
                            ThreadUtil.postOnUiThread(() -> rttCallScreen.onRemoteMessage(message));
                        }
                    } catch (IOException e) {
                        LogUtil.e("RttCallPresenter.RemoteMessageHandler.handleMessage", "read message", e);
                    }
                    sendEmptyMessageDelayed(READ_MESSAGE, 200);
                    break;
                case WRITE_MESSAGE:
                    try {
                        rttCall.write((String) msg.obj);
                    } catch (IOException e) {
                        LogUtil.e("RttCallPresenter.RemoteMessageHandler.handleMessage", "write message", e);
                    }
                    break;
                default: // fall out
            }
        }

        void start() {
            sendEmptyMessage(START);
        }

        void writeMessage(String message) {
            sendMessage(obtainMessage(WRITE_MESSAGE, message));
        }
    }
}
