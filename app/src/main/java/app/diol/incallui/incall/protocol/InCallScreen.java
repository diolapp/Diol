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

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.accessibility.AccessibilityEvent;

/**
 * Interface for the call card module.
 */
public interface InCallScreen {

    void setPrimary(@NonNull PrimaryInfo primaryInfo);

    void setSecondary(@NonNull SecondaryInfo secondaryInfo);

    void setCallState(@NonNull PrimaryCallState primaryCallState);

    void setEndCallButtonEnabled(boolean enabled, boolean animate);

    void showManageConferenceCallButton(boolean visible);

    boolean isManageConferenceVisible();

    void dispatchPopulateAccessibilityEvent(AccessibilityEvent event);

    void showNoteSentToast();

    void updateInCallScreenColors();

    void onInCallScreenDialpadVisibilityChange(boolean isShowing);

    int getAnswerAndDialpadContainerResourceId();

    void showLocationUi(Fragment locationUi);

    boolean isShowingLocationUi();

    Fragment getInCallScreenFragment();
}
