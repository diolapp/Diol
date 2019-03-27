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
import app.diol.incallui.incall.protocol.InCallScreenDelegate;
import app.diol.incallui.rtt.impl.RttCheckableButton.OnCheckedChangeListener;
import app.diol.incallui.speakerbuttonlogic.SpeakerButtonInfo;

/**
 * Overflow menu for RTT call.
 */
public class RttOverflowMenu extends PopupWindow implements OnCheckedChangeListener {

    private final RttCheckableButton muteButton;
    private final RttCheckableButton speakerButton;
    private final RttCheckableButton dialpadButton;
    private final RttCheckableButton addCallButton;
    private final RttCheckableButton swapCallButton;
    private final InCallButtonUiDelegate inCallButtonUiDelegate;
    private final InCallScreenDelegate inCallScreenDelegate;
    private boolean isSwitchToSecondaryButtonEnabled;
    private boolean isSwapCallButtonEnabled;

    RttOverflowMenu(
            Context context,
            InCallButtonUiDelegate inCallButtonUiDelegate,
            InCallScreenDelegate inCallScreenDelegate) {
        super(context, null, 0, R.style.OverflowMenu);
        this.inCallButtonUiDelegate = inCallButtonUiDelegate;
        this.inCallScreenDelegate = inCallScreenDelegate;
        View view = View.inflate(context, R.layout.overflow_menu, null);
        setContentView(view);
        setOnDismissListener(this::dismiss);
        setFocusable(true);
        setWidth(context.getResources().getDimensionPixelSize(R.dimen.rtt_overflow_menu_width));
        muteButton = view.findViewById(R.id.menu_mute);
        muteButton.setOnCheckedChangeListener(this);
        speakerButton = view.findViewById(R.id.menu_speaker);
        speakerButton.setOnCheckedChangeListener(this);
        dialpadButton = view.findViewById(R.id.menu_keypad);
        dialpadButton.setOnCheckedChangeListener(this);
        addCallButton = view.findViewById(R.id.menu_add_call);
        addCallButton.setOnClickListener(v -> this.inCallButtonUiDelegate.addCallClicked());
        swapCallButton = view.findViewById(R.id.menu_swap_call);
        swapCallButton.setOnClickListener(
                v -> {
                    if (isSwapCallButtonEnabled) {
                        this.inCallButtonUiDelegate.swapClicked();
                    }
                    if (isSwitchToSecondaryButtonEnabled) {
                        this.inCallScreenDelegate.onSecondaryInfoClicked();
                    }
                });
    }

    @Override
    public void onCheckedChanged(RttCheckableButton button, boolean isChecked) {
        if (button == muteButton) {
            inCallButtonUiDelegate.muteClicked(isChecked, true);
        } else if (button == speakerButton) {
            inCallButtonUiDelegate.toggleSpeakerphone();
        } else if (button == dialpadButton) {
            inCallButtonUiDelegate.showDialpadClicked(isChecked);
        }
    }

    void setMuteButtonChecked(boolean isChecked) {
        muteButton.setChecked(isChecked);
    }

    void setAudioState(CallAudioState audioState) {
        SpeakerButtonInfo info = new SpeakerButtonInfo(audioState);
        if (info.nonBluetoothMode) {
            speakerButton.setChecked(info.isChecked);
            speakerButton.setOnClickListener(null);
            speakerButton.setOnCheckedChangeListener(this);
        } else {
            speakerButton.setText(info.label);
            speakerButton.setCompoundDrawablesWithIntrinsicBounds(info.icon, 0, 0, 0);
            speakerButton.setOnClickListener(
                    v -> {
                        inCallButtonUiDelegate.showAudioRouteSelector();
                        dismiss();
                    });
            speakerButton.setOnCheckedChangeListener(null);
        }
    }

    void setDialpadButtonChecked(boolean isChecked) {
        dialpadButton.setChecked(isChecked);
    }

    void enableSwapCallButton(boolean enabled) {
        isSwapCallButtonEnabled = enabled;
        swapCallButton.setVisibility(
                isSwapCallButtonEnabled || isSwitchToSecondaryButtonEnabled ? View.VISIBLE : View.GONE);
    }

    void enableSwitchToSecondaryButton(boolean enabled) {
        isSwitchToSecondaryButtonEnabled = enabled;
        swapCallButton.setVisibility(
                isSwapCallButtonEnabled || isSwitchToSecondaryButtonEnabled ? View.VISIBLE : View.GONE);
    }
}
