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

import android.content.Context;
import android.os.Bundle;
import android.telecom.CallAudioState;

/**
 * Callbacks from the module out to the container.
 */
public interface InCallButtonUiDelegate {

    void onInCallButtonUiReady(InCallButtonUi inCallButtonUi);

    void onInCallButtonUiUnready();

    void onSaveInstanceState(Bundle outState);

    void onRestoreInstanceState(Bundle savedInstanceState);

    void refreshMuteState();

    void addCallClicked();

    void muteClicked(boolean checked, boolean clickedByUser);

    void mergeClicked();

    void holdClicked(boolean checked);

    void swapClicked();

    void showDialpadClicked(boolean checked);

    void changeToVideoClicked();

    void changeToRttClicked();

    void switchCameraClicked(boolean useFrontFacingCamera);

    void toggleCameraClicked();

    void pauseVideoClicked(boolean pause);

    void toggleSpeakerphone();

    CallAudioState getCurrentAudioState();

    void setAudioRoute(int route);

    void onEndCallClicked();

    void showAudioRouteSelector();

    void swapSimClicked();

    Context getContext();
}
