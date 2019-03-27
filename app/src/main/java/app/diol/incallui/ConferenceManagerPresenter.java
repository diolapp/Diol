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

import java.util.ArrayList;
import java.util.List;

import app.diol.incallui.ConferenceManagerPresenter.ConferenceManagerUi;
import app.diol.incallui.InCallPresenter.InCallDetailsListener;
import app.diol.incallui.InCallPresenter.InCallState;
import app.diol.incallui.InCallPresenter.InCallStateListener;
import app.diol.incallui.InCallPresenter.IncomingCallListener;
import app.diol.incallui.baseui.Presenter;
import app.diol.incallui.baseui.Ui;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;

/**
 * Logic for call buttons.
 */
public class ConferenceManagerPresenter extends Presenter<ConferenceManagerUi>
        implements InCallStateListener, InCallDetailsListener, IncomingCallListener {

    @Override
    public void onUiReady(ConferenceManagerUi ui) {
        super.onUiReady(ui);

        // register for call state changes last
        InCallPresenter.getInstance().addListener(this);
        InCallPresenter.getInstance().addIncomingCallListener(this);
    }

    @Override
    public void onUiUnready(ConferenceManagerUi ui) {
        super.onUiUnready(ui);

        InCallPresenter.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);
    }

    @Override
    public void onStateChange(InCallState oldState, InCallState newState, CallList callList) {
        if (getUi().isFragmentVisible()) {
            Log.v(this, "onStateChange" + newState);
            if (newState == InCallState.INCALL) {
                final DialerCall call = callList.getActiveOrBackgroundCall();
                if (call != null && call.isConferenceCall()) {
                    Log.v(
                            this, "Number of existing calls is " + String.valueOf(call.getChildCallIds().size()));
                    update(callList);
                } else {
                    InCallPresenter.getInstance().showConferenceCallManager(false);
                }
            } else {
                InCallPresenter.getInstance().showConferenceCallManager(false);
            }
        }
    }

    @Override
    public void onDetailsChanged(DialerCall call, android.telecom.Call.Details details) {
        boolean canDisconnect =
                details.can(android.telecom.Call.Details.CAPABILITY_DISCONNECT_FROM_CONFERENCE);
        boolean canSeparate =
                details.can(android.telecom.Call.Details.CAPABILITY_SEPARATE_FROM_CONFERENCE);

        if (call.can(android.telecom.Call.Details.CAPABILITY_DISCONNECT_FROM_CONFERENCE)
                != canDisconnect
                || call.can(android.telecom.Call.Details.CAPABILITY_SEPARATE_FROM_CONFERENCE)
                != canSeparate) {
            getUi().refreshCall(call);
        }

        if (!details.can(android.telecom.Call.Details.CAPABILITY_MANAGE_CONFERENCE)) {
            InCallPresenter.getInstance().showConferenceCallManager(false);
        }
    }

    @Override
    public void onIncomingCall(InCallState oldState, InCallState newState, DialerCall call) {
        // When incoming call exists, set conference ui invisible.
        if (getUi().isFragmentVisible()) {
            Log.d(this, "onIncomingCall()... Conference ui is showing, hide it.");
            InCallPresenter.getInstance().showConferenceCallManager(false);
        }
    }

    public void init(CallList callList) {
        update(callList);
    }

    /**
     * Updates the conference participant adapter.
     *
     * @param callList The callList.
     */
    private void update(CallList callList) {
        // callList is non null, but getActiveOrBackgroundCall() may return null
        final DialerCall currentCall = callList.getActiveOrBackgroundCall();
        if (currentCall == null) {
            return;
        }

        ArrayList<DialerCall> calls = new ArrayList<>(currentCall.getChildCallIds().size());
        for (String callerId : currentCall.getChildCallIds()) {
            calls.add(callList.getCallById(callerId));
        }

        Log.d(this, "Number of calls is " + String.valueOf(calls.size()));

        // Users can split out a call from the conference call if either the active call or the
        // holding call is empty. If both are filled, users can not split out another call.
        final boolean hasActiveCall = (callList.getActiveCall() != null);
        final boolean hasHoldingCall = (callList.getBackgroundCall() != null);
        boolean canSeparate = !(hasActiveCall && hasHoldingCall);

        getUi().update(calls, canSeparate);
    }

    public interface ConferenceManagerUi extends Ui {

        boolean isFragmentVisible();

        void update(List<DialerCall> participants, boolean parentCanSeparate);

        void refreshCall(DialerCall call);
    }
}
