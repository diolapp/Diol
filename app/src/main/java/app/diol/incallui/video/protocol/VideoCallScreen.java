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

package app.diol.incallui.video.protocol;

import android.support.v4.app.Fragment;

/**
 * Interface for call video call module.
 */
public interface VideoCallScreen {

    void onVideoScreenStart();

    void onVideoScreenStop();

    void showVideoViews(boolean shouldShowPreview, boolean shouldShowRemote, boolean isRemotelyHeld);

    void onLocalVideoDimensionsChanged();

    void onLocalVideoOrientationChanged();

    void onRemoteVideoDimensionsChanged();

    void updateFullscreenAndGreenScreenMode(
            boolean shouldShowFullscreen, boolean shouldShowGreenScreen);

    Fragment getVideoCallScreenFragment();

    String getCallId();

    void onHandoverFromWiFiToLte();
}
