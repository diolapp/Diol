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
import android.content.Intent;
import android.support.annotation.VisibleForTesting;

import app.diol.dialer.common.LogUtil;
import app.diol.incallui.InCallPresenter.InCallState;
import app.diol.incallui.InCallPresenter.InCallStateListener;
import app.diol.incallui.InCallPresenter.InCallUiListener;
import app.diol.incallui.call.CallList;

/**
 * Responsible for broadcasting the Intent INCOMING_CALL_VISIBILITY_CHANGED so other processes could
 * know when the incoming call activity is started or finished.
 */
public class MotorolaInCallUiNotifier implements InCallUiListener, InCallStateListener {

    @VisibleForTesting
    static final String EXTRA_VISIBLE_KEY = "visible";

    @VisibleForTesting
    static final String ACTION_INCOMING_CALL_VISIBILITY_CHANGED =
            "com.motorola.incallui.action.INCOMING_CALL_VISIBILITY_CHANGED";

    @VisibleForTesting
    static final String PERMISSION_INCOMING_CALL_VISIBILITY_CHANGED =
            "com.motorola.incallui.permission.INCOMING_CALL_VISIBILITY_CHANGED";

    private final Context context;

    MotorolaInCallUiNotifier(Context context) {
        this.context = context;
    }

    @Override
    public void onUiShowing(boolean showing) {
        if (showing && CallList.getInstance().getIncomingCall() != null) {
            sendInCallUiBroadcast(true);
        }
    }

    @Override
    public void onStateChange(InCallState oldState, InCallState newState, CallList callList) {
        if (oldState != null
                && oldState.isConnectingOrConnected()
                && newState == InCallState.NO_CALLS) {
            sendInCallUiBroadcast(false);
        }
    }

    private void sendInCallUiBroadcast(boolean visible) {
        LogUtil.d(
                "MotorolaInCallUiNotifier.sendInCallUiBroadcast",
                "Send InCallUi Broadcast, visible: " + visible);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_VISIBLE_KEY, visible);
        intent.setAction(ACTION_INCOMING_CALL_VISIBILITY_CHANGED);
        context.sendBroadcast(intent, PERMISSION_INCOMING_CALL_VISIBILITY_CHANGED);
    }
}
