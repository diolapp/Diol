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
import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import app.diol.dialer.activecalls.ActiveCallInfo;
import app.diol.dialer.activecalls.ActiveCallsComponent;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.state.DialerCallState;

/**
 * Updates {@link app.diol.dialer.activecalls.ActiveCalls}
 */
@SuppressWarnings("Guava")
public class ActiveCallsCallListListener implements CallList.Listener {

    private final Context appContext;

    ActiveCallsCallListListener(Context appContext) {
        this.appContext = appContext;
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
        ImmutableList.Builder<ActiveCallInfo> activeCalls = ImmutableList.builder();
        for (DialerCall call : callList.getAllCalls()) {
            if (call.getState() != DialerCallState.DISCONNECTED && call.getAccountHandle() != null) {
                activeCalls.add(
                        ActiveCallInfo.builder()
                                .setPhoneAccountHandle(Optional.of(call.getAccountHandle()))
                                .build());
            }
        }
        ActiveCallsComponent.get(appContext).activeCalls().setActiveCalls(activeCalls.build());
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
