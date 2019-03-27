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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.telecom.CallAudioState;

import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.incallui.audiomode.AudioModeProvider;
import app.diol.incallui.audioroute.AudioRouteSelectorDialogFragment;
import app.diol.incallui.audioroute.AudioRouteSelectorDialogFragment.AudioRouteSelectorPresenter;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.CallList.Listener;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.TelecomAdapter;

/**
 * Simple activity that just shows the audio route selector fragment
 */
public class AudioRouteSelectorActivity extends FragmentActivity
        implements AudioRouteSelectorPresenter, Listener {

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        AudioRouteSelectorDialogFragment.newInstance(AudioModeProvider.getInstance().getAudioState())
                .show(getSupportFragmentManager(), AudioRouteSelectorDialogFragment.TAG);

        CallList.getInstance().addListener(this);
    }

    @Override
    public void onAudioRouteSelected(int audioRoute) {
        TelecomAdapter.getInstance().setAudioRoute(audioRoute);
        finish();

        // Log the select action with audio route and call
        DialerImpression.Type impressionType = null;
        if ((audioRoute & CallAudioState.ROUTE_WIRED_OR_EARPIECE) != 0) {
            impressionType = DialerImpression.Type.BUBBLE_V2_WIRED_OR_EARPIECE;
        } else if (audioRoute == CallAudioState.ROUTE_SPEAKER) {
            impressionType = DialerImpression.Type.BUBBLE_V2_SPEAKERPHONE;
        } else if (audioRoute == CallAudioState.ROUTE_BLUETOOTH) {
            impressionType = DialerImpression.Type.BUBBLE_V2_BLUETOOTH;
        }
        if (impressionType == null) {
            return;
        }

        DialerCall call = getCall();
        if (call != null) {
            Logger.get(this)
                    .logCallImpression(impressionType, call.getUniqueCallId(), call.getTimeAddedMs());
        } else {
            Logger.get(this).logImpression(impressionType);
        }
    }

    @Override
    public void onAudioRouteSelectorDismiss() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioRouteSelectorDialogFragment audioRouteSelectorDialogFragment =
                (AudioRouteSelectorDialogFragment)
                        getSupportFragmentManager().findFragmentByTag(AudioRouteSelectorDialogFragment.TAG);
        // If Android back button is pressed, the fragment is dismissed and removed. If home button is
        // pressed, we have to manually dismiss the fragment here. The fragment is also removed when
        // dismissed.
        if (audioRouteSelectorDialogFragment != null) {
            audioRouteSelectorDialogFragment.dismiss();
        }
        // We don't expect the activity to resume, except for orientation change.
        if (!isChangingConfigurations()) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        CallList.getInstance().removeListener(this);
        super.onDestroy();
    }

    private DialerCall getCall() {
        DialerCall dialerCall = CallList.getInstance().getOutgoingCall();
        if (dialerCall == null) {
            dialerCall = CallList.getInstance().getActiveOrBackgroundCall();
        }
        return dialerCall;
    }

    @Override
    public void onDisconnect(DialerCall call) {
        if (getCall() == null) {
            finish();
        }
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
    public void onWiFiToLteHandover(DialerCall call) {
    }

    @Override
    public void onHandoverToWifiFailed(DialerCall call) {
    }

    @Override
    public void onInternationalCallOnWifi(@NonNull DialerCall call) {
    }
}
