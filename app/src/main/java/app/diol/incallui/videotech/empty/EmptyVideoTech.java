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

package app.diol.incallui.videotech.empty;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.common.Assert;
import app.diol.incallui.video.protocol.VideoCallScreen;
import app.diol.incallui.video.protocol.VideoCallScreenDelegate;
import app.diol.incallui.videotech.VideoTech;
import app.diol.incallui.videotech.utils.SessionModificationState;

/**
 * Default video tech that is always available but doesn't do anything.
 */
public class EmptyVideoTech implements VideoTech {

    @Override
    public boolean isAvailable(Context context, PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @Override
    public boolean isTransmittingOrReceiving() {
        return false;
    }

    @Override
    public boolean isSelfManagedCamera() {
        return false;
    }

    @Override
    public boolean shouldUseSurfaceView() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public VideoCallScreenDelegate createVideoCallScreenDelegate(
            Context context, VideoCallScreen videoCallScreen) {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public void onCallStateChanged(
            Context context, int newState, PhoneAccountHandle phoneAccountHandle) {
    }

    @Override
    public void onRemovedFromCallList() {
    }

    @Override
    public int getSessionModificationState() {
        return SessionModificationState.NO_REQUEST;
    }

    @Override
    public void upgradeToVideo(@NonNull Context context) {
    }

    @Override
    public void acceptVideoRequest(@NonNull Context context) {
    }

    @Override
    public void acceptVideoRequestAsAudio() {
    }

    @Override
    public void declineVideoRequest() {
    }

    @Override
    public boolean isTransmitting() {
        return false;
    }

    @Override
    public void stopTransmission() {
    }

    @Override
    public void resumeTransmission(@NonNull Context context) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
    }

    @Override
    public void setCamera(@Nullable String cameraId) {
    }

    @Override
    public void setDeviceOrientation(int rotation) {
    }

    @Override
    public void becomePrimary() {
    }

    @Override
    public app.diol.dialer.logging.VideoTech.Type getVideoTechType() {
        return app.diol.dialer.logging.VideoTech.Type.NONE;
    }
}
