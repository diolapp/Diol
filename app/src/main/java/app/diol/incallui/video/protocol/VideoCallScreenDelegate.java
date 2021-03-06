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

import android.content.Context;
import android.view.SurfaceView;

import app.diol.incallui.videosurface.protocol.VideoSurfaceTexture;

/**
 * Callbacks from the module out to the container.
 */
public interface VideoCallScreenDelegate {

    void initVideoCallScreenDelegate(Context context, VideoCallScreen videoCallScreen);

    void onVideoCallScreenUiReady();

    void onVideoCallScreenUiUnready();

    void cancelAutoFullScreen();

    void resetAutoFullscreenTimer();

    void onSystemUiVisibilityChange(boolean visible);

    void onCameraPermissionGranted();

    boolean shouldShowCameraPermissionToast();

    void onCameraPermissionDialogShown();

    VideoSurfaceTexture getLocalVideoSurfaceTexture();

    VideoSurfaceTexture getRemoteVideoSurfaceTexture();

    void setSurfaceViews(SurfaceView preview, SurfaceView remote);

    int getDeviceOrientation();
}
