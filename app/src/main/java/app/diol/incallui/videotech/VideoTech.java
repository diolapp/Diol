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

package app.diol.incallui.videotech;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.logging.DialerImpression;
import app.diol.incallui.video.protocol.VideoCallScreen;
import app.diol.incallui.video.protocol.VideoCallScreenDelegate;
import app.diol.incallui.videotech.utils.SessionModificationState;

/**
 * Video calling interface.
 */
public interface VideoTech {

    boolean isAvailable(Context context, PhoneAccountHandle phoneAccountHandle);

    boolean isTransmittingOrReceiving();

    /**
     * Determines if the answer video UI should open the camera directly instead of letting the video
     * tech manage the camera.
     */
    boolean isSelfManagedCamera();

    boolean shouldUseSurfaceView();

    /**
     * Returns true if the video is paused. This is different than if the video stream has been turned
     * off.
     *
     * <p>See {@link #isTransmitting()}
     */
    boolean isPaused();

    VideoCallScreenDelegate createVideoCallScreenDelegate(
            Context context, VideoCallScreen videoCallScreen);

    void onCallStateChanged(Context context, int newState, PhoneAccountHandle phoneAccountHandle);

    void onRemovedFromCallList();

    @SessionModificationState
    int getSessionModificationState();

    void upgradeToVideo(@NonNull Context context);

    void acceptVideoRequest(@NonNull Context context);

    void acceptVideoRequestAsAudio();

    void declineVideoRequest();

    boolean isTransmitting();

    void stopTransmission();

    void resumeTransmission(@NonNull Context context);

    void pause();

    void unpause();

    void setCamera(@Nullable String cameraId);

    void setDeviceOrientation(int rotation);

    /**
     * Called on {@code VideoTechManager.savedTech} when it's first selected and it will always be
     * used.
     */
    void becomePrimary();

    app.diol.dialer.logging.VideoTech.Type getVideoTechType();

    /**
     * Listener for video call events.
     */
    interface VideoTechListener {

        void onVideoTechStateChanged();

        void onSessionModificationStateChanged();

        void onCameraDimensionsChanged(int width, int height);

        void onPeerDimensionsChanged(int width, int height);

        void onVideoUpgradeRequestReceived();

        void onUpgradedToVideo(boolean switchToSpeaker);

        void onImpressionLoggingNeeded(DialerImpression.Type impressionType);
    }
}
