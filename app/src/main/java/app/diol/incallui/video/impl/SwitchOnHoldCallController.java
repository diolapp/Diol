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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;

import app.diol.dialer.common.Assert;
import app.diol.incallui.incall.protocol.InCallScreenDelegate;
import app.diol.incallui.incall.protocol.SecondaryInfo;
import app.diol.incallui.video.protocol.VideoCallScreenDelegate;

/**
 * Manages the swap button and on hold banner.
 */
public class SwitchOnHoldCallController implements OnClickListener {

    @NonNull
    private InCallScreenDelegate inCallScreenDelegate;
    @NonNull
    private VideoCallScreenDelegate videoCallScreenDelegate;

    @NonNull
    private View switchOnHoldButton;

    @NonNull
    private View onHoldBanner;

    private boolean isVisible;

    private boolean isEnabled;

    @Nullable
    private SecondaryInfo secondaryInfo;

    public SwitchOnHoldCallController(
            @NonNull View switchOnHoldButton,
            @NonNull View onHoldBanner,
            @NonNull InCallScreenDelegate inCallScreenDelegate,
            @NonNull VideoCallScreenDelegate videoCallScreenDelegate) {
        this.switchOnHoldButton = Assert.isNotNull(switchOnHoldButton);
        switchOnHoldButton.setOnClickListener(this);
        this.onHoldBanner = Assert.isNotNull(onHoldBanner);
        this.inCallScreenDelegate = Assert.isNotNull(inCallScreenDelegate);
        this.videoCallScreenDelegate = Assert.isNotNull(videoCallScreenDelegate);
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        updateButtonState();
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        updateButtonState();
    }

    public void setOnScreen() {
        isVisible = hasSecondaryInfo();
        updateButtonState();
    }

    public void setSecondaryInfo(@Nullable SecondaryInfo secondaryInfo) {
        this.secondaryInfo = secondaryInfo;
        isVisible = hasSecondaryInfo();
    }

    private boolean hasSecondaryInfo() {
        return secondaryInfo != null && secondaryInfo.shouldShow();
    }

    public void updateButtonState() {
        switchOnHoldButton.setEnabled(isEnabled);
        switchOnHoldButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        onHoldBanner.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        inCallScreenDelegate.onSecondaryInfoClicked();
        videoCallScreenDelegate.resetAutoFullscreenTimer();
    }
}
