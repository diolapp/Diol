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

package app.diol.incallui.video.impl;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.telecom.CallAudioState;
import android.view.View;
import android.view.View.OnClickListener;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.incallui.incall.protocol.InCallButtonUiDelegate;
import app.diol.incallui.video.impl.CheckableImageButton.OnCheckedChangeListener;
import app.diol.incallui.video.protocol.VideoCallScreenDelegate;

/**
 * Manages a single button.
 */
public class SpeakerButtonController implements OnCheckedChangeListener, OnClickListener {

    @NonNull
    private final InCallButtonUiDelegate inCallButtonUiDelegate;
    @NonNull
    private final VideoCallScreenDelegate videoCallScreenDelegate;

    @NonNull
    private CheckableImageButton button;

    @DrawableRes
    private int icon = R.drawable.quantum_ic_volume_up_vd_theme_24;

    private boolean isChecked;
    private boolean checkable;
    private boolean isEnabled;
    private CharSequence contentDescription;

    SpeakerButtonController(
            @NonNull CheckableImageButton button,
            @NonNull InCallButtonUiDelegate inCallButtonUiDelegate,
            @NonNull VideoCallScreenDelegate videoCallScreenDelegate) {
        this.inCallButtonUiDelegate = Assert.isNotNull(inCallButtonUiDelegate);
        this.videoCallScreenDelegate = Assert.isNotNull(videoCallScreenDelegate);
        this.button = Assert.isNotNull(button);
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        updateButtonState();
    }

    void updateButtonState() {
        button.setVisibility(View.VISIBLE);
        button.setEnabled(isEnabled);
        button.setChecked(isChecked);
        button.setOnClickListener(checkable ? null : this);
        button.setOnCheckedChangeListener(checkable ? this : null);
        button.setImageResource(icon);
        button.setContentDescription(contentDescription);
    }

    public void setAudioState(CallAudioState audioState) {
        LogUtil.i("SpeakerButtonController.setSupportedAudio", "audioState: " + audioState);

        @StringRes int contentDescriptionResId;
        if ((audioState.getSupportedRouteMask() & CallAudioState.ROUTE_BLUETOOTH)
                == CallAudioState.ROUTE_BLUETOOTH) {
            checkable = false;
            isChecked = false;

            if ((audioState.getRoute() & CallAudioState.ROUTE_BLUETOOTH)
                    == CallAudioState.ROUTE_BLUETOOTH) {
                icon = R.drawable.quantum_ic_bluetooth_audio_vd_theme_24;
                contentDescriptionResId = R.string.incall_content_description_bluetooth;
            } else if ((audioState.getRoute() & CallAudioState.ROUTE_SPEAKER)
                    == CallAudioState.ROUTE_SPEAKER) {
                icon = R.drawable.quantum_ic_volume_up_vd_theme_24;
                contentDescriptionResId = R.string.incall_content_description_speaker;
            } else if ((audioState.getRoute() & CallAudioState.ROUTE_WIRED_HEADSET)
                    == CallAudioState.ROUTE_WIRED_HEADSET) {
                icon = R.drawable.quantum_ic_headset_vd_theme_24;
                contentDescriptionResId = R.string.incall_content_description_headset;
            } else {
                icon = R.drawable.quantum_ic_phone_in_talk_vd_theme_24;
                contentDescriptionResId = R.string.incall_content_description_earpiece;
            }
        } else {
            checkable = true;
            isChecked = audioState.getRoute() == CallAudioState.ROUTE_SPEAKER;
            icon = R.drawable.quantum_ic_volume_up_vd_theme_24;
            contentDescriptionResId = R.string.incall_content_description_speaker;
        }

        contentDescription = button.getContext().getText(contentDescriptionResId);
        updateButtonState();
    }

    @Override
    public void onCheckedChanged(CheckableImageButton button, boolean isChecked) {
        LogUtil.i("SpeakerButtonController.onCheckedChanged", null);
        inCallButtonUiDelegate.toggleSpeakerphone();
        videoCallScreenDelegate.resetAutoFullscreenTimer();
    }

    @Override
    public void onClick(View view) {
        LogUtil.i("SpeakerButtonController.onClick", null);
        inCallButtonUiDelegate.showAudioRouteSelector();
        videoCallScreenDelegate.resetAutoFullscreenTimer();
    }
}
