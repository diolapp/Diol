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

package app.diol.incallui.speakerbuttonlogic;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.telecom.CallAudioState;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.diol.R;

/**
 * Info about how a "Speaker" button should be displayed
 */
public class SpeakerButtonInfo {

    // Testing note: most of this is exercised in ReturnToCallTest.java

    @DrawableRes
    public final int icon;
    @StringRes
    public final int contentDescription;
    @StringRes
    public final int label;
    public final boolean nonBluetoothMode;
    public final boolean isChecked;
    public SpeakerButtonInfo(CallAudioState audioState) {
        if ((audioState.getSupportedRouteMask() & CallAudioState.ROUTE_BLUETOOTH)
                == CallAudioState.ROUTE_BLUETOOTH) {
            nonBluetoothMode = false;
            label = R.string.incall_label_audio;

            if ((audioState.getRoute() & CallAudioState.ROUTE_BLUETOOTH)
                    == CallAudioState.ROUTE_BLUETOOTH) {
                icon = R.drawable.volume_bluetooth;
                contentDescription = R.string.incall_content_description_bluetooth;
                isChecked = true;
            } else if ((audioState.getRoute() & CallAudioState.ROUTE_SPEAKER)
                    == CallAudioState.ROUTE_SPEAKER) {
                icon = R.drawable.quantum_ic_volume_up_vd_theme_24;
                contentDescription = R.string.incall_content_description_speaker;
                isChecked = true;
            } else if ((audioState.getRoute() & CallAudioState.ROUTE_WIRED_HEADSET)
                    == CallAudioState.ROUTE_WIRED_HEADSET) {
                icon = R.drawable.quantum_ic_headset_vd_theme_24;
                contentDescription = R.string.incall_content_description_headset;
                isChecked = true;
            } else {
                icon = R.drawable.quantum_ic_phone_in_talk_vd_theme_24;
                contentDescription = R.string.incall_content_description_earpiece;
                isChecked = false;
            }
        } else {
            nonBluetoothMode = true;
            isChecked = audioState.getRoute() == CallAudioState.ROUTE_SPEAKER;
            label = R.string.incall_label_speaker;
            icon = R.drawable.quantum_ic_volume_up_vd_theme_24;
            contentDescription = R.string.incall_content_description_speaker;
        }
    }

    /**
     * Preferred size for icons
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({IconSize.SIZE_24_DP, IconSize.SIZE_36_DP})
    public @interface IconSize {
        int SIZE_24_DP = 1;
        int SIZE_36_DP = 2;
    }
}
