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

import android.telephony.PhoneNumberUtils;

import app.diol.incallui.DialpadPresenter.DialpadUi;
import app.diol.incallui.baseui.Presenter;
import app.diol.incallui.baseui.Ui;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.TelecomAdapter;

/**
 * Logic for call buttons.
 */
public class DialpadPresenter extends Presenter<DialpadUi>
        implements InCallPresenter.InCallStateListener {

    private DialerCall call;

    @Override
    public void onUiReady(DialpadUi ui) {
        super.onUiReady(ui);
        InCallPresenter.getInstance().addListener(this);
        call = CallList.getInstance().getOutgoingOrActive();
    }

    @Override
    public void onUiUnready(DialpadUi ui) {
        super.onUiUnready(ui);
        InCallPresenter.getInstance().removeListener(this);
    }

    @Override
    public void onStateChange(
            InCallPresenter.InCallState oldState,
            InCallPresenter.InCallState newState,
            CallList callList) {
        call = callList.getOutgoingOrActive();
        Log.d(this, "DialpadPresenter mCall = " + call);
    }

    /**
     * Processes the specified digit as a DTMF key, by playing the appropriate DTMF tone, and
     * appending the digit to the EditText field that displays the DTMF digits sent so far.
     */
    public final void processDtmf(char c) {
        Log.d(this, "Processing dtmf key " + c);
        // if it is a valid key, then update the display and send the dtmf tone.
        if (PhoneNumberUtils.is12Key(c) && call != null) {
            Log.d(this, "updating display and sending dtmf tone for '" + c + "'");

            // Append this key to the "digits" widget.
            DialpadUi dialpadUi = getUi();
            if (dialpadUi != null) {
                dialpadUi.appendDigitsToField(c);
            }
            // Plays the tone through Telecom.
            TelecomAdapter.getInstance().playDtmfTone(call.getId(), c);
        } else {
            Log.d(this, "ignoring dtmf request for '" + c + "'");
        }
    }

    /**
     * Stops the local tone based on the phone type.
     */
    public void stopDtmf() {
        if (call != null) {
            Log.d(this, "stopping remote tone");
            TelecomAdapter.getInstance().stopDtmfTone(call.getId());
        }
    }

    public interface DialpadUi extends Ui {

        void appendDigitsToField(char digit);
    }
}
