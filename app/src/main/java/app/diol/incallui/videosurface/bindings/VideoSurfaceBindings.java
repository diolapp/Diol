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

package app.diol.incallui.videosurface.bindings;

import android.view.TextureView;

import app.diol.incallui.videosurface.impl.VideoScale;
import app.diol.incallui.videosurface.impl.VideoSurfaceTextureImpl;
import app.diol.incallui.videosurface.protocol.VideoSurfaceTexture;

/**
 * Bindings for video surface module.
 */
public class VideoSurfaceBindings {

    public static VideoSurfaceTexture createLocalVideoSurfaceTexture(boolean isPixel2017) {
        return new VideoSurfaceTextureImpl(isPixel2017, VideoSurfaceTexture.SURFACE_TYPE_LOCAL);
    }

    public static VideoSurfaceTexture createRemoteVideoSurfaceTexture(boolean isPixel2017) {
        return new VideoSurfaceTextureImpl(isPixel2017, VideoSurfaceTexture.SURFACE_TYPE_REMOTE);
    }

    public static void scaleVideoAndFillView(
            TextureView textureView, float videoWidth, float videoHeight, float rotationDegrees) {
        VideoScale.scaleVideoAndFillView(textureView, videoWidth, videoHeight, rotationDegrees);
    }

    public static void scaleVideoMaintainingAspectRatio(
            TextureView textureView, int videoWidth, int videoHeight) {
        VideoScale.scaleVideoMaintainingAspectRatio(textureView, videoWidth, videoHeight);
    }
}
