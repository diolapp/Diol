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

import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.telecom.CallAudioState;

/**
 * Interface for the call button UI.
 */
public interface InCallButtonUi {

    void showButton(@InCallButtonIds int buttonId, boolean show);

    void enableButton(@InCallButtonIds int buttonId, boolean enable);

    void setEnabled(boolean on);

    void setHold(boolean on);

    void setCameraSwitched(boolean isBackFacingCamera);

    void setVideoPaused(boolean isPaused);

    void setAudioState(CallAudioState audioState);

    /**
     * Once showButton() has been called on each of the individual buttons in the UI, call this to
     * configure the overflow menu appropriately.
     */
    void updateButtonStates();

    void updateInCallButtonUiColors(@ColorInt int color);

    Fragment getInCallButtonUiFragment();

    void showAudioRouteSelector();
}
