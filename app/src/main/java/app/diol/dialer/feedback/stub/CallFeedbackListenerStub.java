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

package app.diol.dialer.feedback.stub;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import app.diol.dialer.common.Assert;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;

/**
 * Stub implementation of {@link com.google.android.apps.dialer.feedback.CallFeedbackListenerImpl}
 */
public class CallFeedbackListenerStub implements CallList.Listener {

    @NonNull
    private final Context context;

    @Inject
    public CallFeedbackListenerStub(@ApplicationContext @NonNull Context context) {
        this.context = Assert.isNotNull(context);
    }

    @Override
    public void onIncomingCall(DialerCall call) {
    }

    @Override
    public void onUpgradeToVideo(DialerCall call) {
    }

    @Override
    public void onSessionModificationStateChange(DialerCall call) {
    }

    @Override
    public void onCallListChange(CallList callList) {
    }

    @Override
    public void onDisconnect(DialerCall call) {
    }

    @Override
    public void onWiFiToLteHandover(DialerCall call) {
    }

    @Override
    public void onHandoverToWifiFailed(DialerCall call) {
    }

    @Override
    public void onInternationalCallOnWifi(@NonNull DialerCall call) {
    }
}
