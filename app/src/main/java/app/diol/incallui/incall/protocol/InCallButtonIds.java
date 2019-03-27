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

package app.diol.incallui.incall.protocol;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Ids for buttons in the in call UI.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        InCallButtonIds.BUTTON_AUDIO,
        InCallButtonIds.BUTTON_MUTE,
        InCallButtonIds.BUTTON_DIALPAD,
        InCallButtonIds.BUTTON_HOLD,
        InCallButtonIds.BUTTON_SWAP,
        InCallButtonIds.BUTTON_UPGRADE_TO_VIDEO,
        InCallButtonIds.BUTTON_SWITCH_CAMERA,
        InCallButtonIds.BUTTON_DOWNGRADE_TO_AUDIO,
        InCallButtonIds.BUTTON_ADD_CALL,
        InCallButtonIds.BUTTON_MERGE,
        InCallButtonIds.BUTTON_PAUSE_VIDEO,
        InCallButtonIds.BUTTON_MANAGE_VIDEO_CONFERENCE,
        InCallButtonIds.BUTTON_MANAGE_VOICE_CONFERENCE,
        InCallButtonIds.BUTTON_SWITCH_TO_SECONDARY,
        InCallButtonIds.BUTTON_SWAP_SIM,
        InCallButtonIds.BUTTON_COUNT,
        InCallButtonIds.BUTTON_UPGRADE_TO_RTT
})
public @interface InCallButtonIds {

    int BUTTON_AUDIO = 0;
    int BUTTON_MUTE = 1;
    int BUTTON_DIALPAD = 2;
    int BUTTON_HOLD = 3;
    int BUTTON_SWAP = 4;
    int BUTTON_UPGRADE_TO_VIDEO = 5;
    int BUTTON_SWITCH_CAMERA = 6;
    int BUTTON_DOWNGRADE_TO_AUDIO = 7;
    int BUTTON_ADD_CALL = 8;
    int BUTTON_MERGE = 9;
    int BUTTON_PAUSE_VIDEO = 10;
    int BUTTON_MANAGE_VIDEO_CONFERENCE = 11;
    int BUTTON_MANAGE_VOICE_CONFERENCE = 12;
    int BUTTON_SWITCH_TO_SECONDARY = 13;
    int BUTTON_SWAP_SIM = 14;
    int BUTTON_COUNT = 15;
    int BUTTON_UPGRADE_TO_RTT = 16;
}
