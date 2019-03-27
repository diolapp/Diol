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

package app.diol.incallui.rtt.impl;

import android.content.Context;
import android.telecom.CallAudioState;
import android.view.View;
import android.widget.PopupWindow;

import app.diol.R;
import app.diol.incallui.incall.protocol.InCallButtonUiDelegate;

/**
 * Audio select menu for RTT call.
 */
public class AudioSelectMenu extends PopupWindow {

    private final InCallButtonUiDelegate inCallButtonUiDelegate;
    private final OnButtonClickListener onButtonClickListener;
    private final RttCheckableButton bluetoothButton;
    private final RttCheckableButton speakerButton;
    private final RttCheckableButton headsetButton;
    private final RttCheckableButton earpieceButton;

    AudioSelectMenu(
            Context context,
            InCallButtonUiDelegate inCallButtonUiDelegate,
            OnButtonClickListener onButtonClickListener) {
        super(context, null, 0, R.style.OverflowMenu);
        this.inCallButtonUiDelegate = inCallButtonUiDelegate;
        this.onButtonClickListener = onButtonClickListener;
        View view = View.inflate(context, R.layout.audio_route, null);
        setContentView(view);
        setOnDismissListener(this::dismiss);
        setFocusable(true);
        setWidth(context.getResources().getDimensionPixelSize(R.dimen.rtt_overflow_menu_width));
        view.findViewById(R.id.audioroute_back)
                .setOnClickListener(
                        v -> {
                            dismiss();
                            this.onButtonClickListener.onBackPressed();
                        });
        CallAudioState audioState = inCallButtonUiDelegate.getCurrentAudioState();
        bluetoothButton = view.findViewById(R.id.audioroute_bluetooth);
        speakerButton = view.findViewById(R.id.audioroute_speaker);
        headsetButton = view.findViewById(R.id.audioroute_headset);
        earpieceButton = view.findViewById(R.id.audioroute_earpiece);
        initItem(bluetoothButton, CallAudioState.ROUTE_BLUETOOTH, audioState);
        initItem(speakerButton, CallAudioState.ROUTE_SPEAKER, audioState);
        initItem(headsetButton, CallAudioState.ROUTE_WIRED_HEADSET, audioState);
        initItem(earpieceButton, CallAudioState.ROUTE_EARPIECE, audioState);
    }

    private void initItem(RttCheckableButton item, final int itemRoute, CallAudioState audioState) {
        if ((audioState.getSupportedRouteMask() & itemRoute) == 0) {
            item.setVisibility(View.GONE);
        } else if (audioState.getRoute() == itemRoute) {
            item.setChecked(true);
        }
        item.setOnClickListener(
                (v) -> {
                    inCallButtonUiDelegate.setAudioRoute(itemRoute);
                });
    }

    void setAudioState(CallAudioState audioState) {
        bluetoothButton.setChecked(audioState.getRoute() == CallAudioState.ROUTE_BLUETOOTH);
        speakerButton.setChecked(audioState.getRoute() == CallAudioState.ROUTE_SPEAKER);
        headsetButton.setChecked(audioState.getRoute() == CallAudioState.ROUTE_WIRED_HEADSET);
        earpieceButton.setChecked(audioState.getRoute() == CallAudioState.ROUTE_EARPIECE);
    }

    interface OnButtonClickListener {
        void onBackPressed();
    }
}
