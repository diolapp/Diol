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

/**
 * Utility class for {@link InCallButtonIds}.
 */
public class InCallButtonIdsExtension {

    /**
     * Converts the given {@link InCallButtonIds} to a human readable string.
     *
     * @param id the id to convert.
     * @return the human readable string.
     */
    public static String toString(@InCallButtonIds int id) {
        if (id == InCallButtonIds.BUTTON_AUDIO) {
            return "AUDIO";
        } else if (id == InCallButtonIds.BUTTON_MUTE) {
            return "MUTE";
        } else if (id == InCallButtonIds.BUTTON_DIALPAD) {
            return "DIALPAD";
        } else if (id == InCallButtonIds.BUTTON_HOLD) {
            return "HOLD";
        } else if (id == InCallButtonIds.BUTTON_SWAP) {
            return "SWAP";
        } else if (id == InCallButtonIds.BUTTON_UPGRADE_TO_VIDEO) {
            return "UPGRADE_TO_VIDEO";
        } else if (id == InCallButtonIds.BUTTON_DOWNGRADE_TO_AUDIO) {
            return "DOWNGRADE_TO_AUDIO";
        } else if (id == InCallButtonIds.BUTTON_SWITCH_CAMERA) {
            return "SWITCH_CAMERA";
        } else if (id == InCallButtonIds.BUTTON_ADD_CALL) {
            return "ADD_CALL";
        } else if (id == InCallButtonIds.BUTTON_MERGE) {
            return "MERGE";
        } else if (id == InCallButtonIds.BUTTON_PAUSE_VIDEO) {
            return "PAUSE_VIDEO";
        } else if (id == InCallButtonIds.BUTTON_MANAGE_VIDEO_CONFERENCE) {
            return "MANAGE_VIDEO_CONFERENCE";
        } else if (id == InCallButtonIds.BUTTON_MANAGE_VOICE_CONFERENCE) {
            return "MANAGE_VOICE_CONFERENCE";
        } else if (id == InCallButtonIds.BUTTON_SWITCH_TO_SECONDARY) {
            return "SWITCH_TO_SECONDARY";
        } else if (id == InCallButtonIds.BUTTON_SWAP_SIM) {
            return "SWAP_SIM";
        } else if (id == InCallButtonIds.BUTTON_UPGRADE_TO_RTT) {
            return "UPGRADE_TO_RTT";
        } else {
            return "INVALID_BUTTON: " + id;
        }
    }
}
