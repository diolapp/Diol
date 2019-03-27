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

package app.diol.incallui.answer.impl;

import android.content.res.Configuration;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.TextureView;
import android.view.View;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.FragmentUtils;
import app.diol.dialer.common.LogUtil;
import app.diol.incallui.video.protocol.VideoCallScreen;
import app.diol.incallui.video.protocol.VideoCallScreenDelegate;
import app.diol.incallui.video.protocol.VideoCallScreenDelegateFactory;
import app.diol.incallui.videosurface.bindings.VideoSurfaceBindings;

/**
 * Shows a video preview for an incoming call.
 */
public class AnswerVideoCallScreen implements VideoCallScreen {
    @NonNull
    private final String callId;
    @NonNull
    private final Fragment fragment;
    @NonNull
    private final TextureView textureView;
    @NonNull
    private final VideoCallScreenDelegate delegate;

    public AnswerVideoCallScreen(
            @NonNull String callId, @NonNull Fragment fragment, @NonNull View view) {
        this.callId = Assert.isNotNull(callId);
        this.fragment = Assert.isNotNull(fragment);

        textureView =
                Assert.isNotNull((TextureView) view.findViewById(R.id.incoming_preview_texture_view));
        View overlayView =
                Assert.isNotNull(view.findViewById(R.id.incoming_preview_texture_view_overlay));
        view.setBackgroundColor(0xff000000);
        delegate =
                FragmentUtils.getParentUnsafe(fragment, VideoCallScreenDelegateFactory.class)
                        .newVideoCallScreenDelegate(this);
        delegate.initVideoCallScreenDelegate(fragment.getContext(), this);

        textureView.setVisibility(View.VISIBLE);
        overlayView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideoScreenStart() {
        LogUtil.i("AnswerVideoCallScreen.onStart", null);
        delegate.onVideoCallScreenUiReady();
        delegate.getLocalVideoSurfaceTexture().attachToTextureView(textureView);
    }

    @Override
    public void onVideoScreenStop() {
        LogUtil.i("AnswerVideoCallScreen.onStop", null);
        delegate.onVideoCallScreenUiUnready();
    }

    @Override
    public void showVideoViews(
            boolean shouldShowPreview, boolean shouldShowRemote, boolean isRemotelyHeld) {
        LogUtil.i(
                "AnswerVideoCallScreen.showVideoViews",
                "showPreview: %b, shouldShowRemote: %b",
                shouldShowPreview,
                shouldShowRemote);
    }

    @Override
    public void onLocalVideoDimensionsChanged() {
        LogUtil.i("AnswerVideoCallScreen.onLocalVideoDimensionsChanged", null);
        updatePreviewVideoScaling();
    }

    @Override
    public void onRemoteVideoDimensionsChanged() {
    }

    @Override
    public void onLocalVideoOrientationChanged() {
        LogUtil.i("AnswerVideoCallScreen.onLocalVideoOrientationChanged", null);
        updatePreviewVideoScaling();
    }

    @Override
    public void updateFullscreenAndGreenScreenMode(
            boolean shouldShowFullscreen, boolean shouldShowGreenScreen) {
    }

    @Override
    public Fragment getVideoCallScreenFragment() {
        return fragment;
    }

    @NonNull
    @Override
    public String getCallId() {
        return callId;
    }

    @Override
    public void onHandoverFromWiFiToLte() {
    }

    private void updatePreviewVideoScaling() {
        if (textureView.getWidth() == 0 || textureView.getHeight() == 0) {
            LogUtil.i(
                    "AnswerVideoCallScreen.updatePreviewVideoScaling", "view layout hasn't finished yet");
            return;
        }
        Point cameraDimensions = delegate.getLocalVideoSurfaceTexture().getSurfaceDimensions();
        if (cameraDimensions == null) {
            LogUtil.i("AnswerVideoCallScreen.updatePreviewVideoScaling", "camera dimensions not set");
            return;
        }
        if (isLandscape()) {
            VideoSurfaceBindings.scaleVideoAndFillView(
                    textureView, cameraDimensions.x, cameraDimensions.y, delegate.getDeviceOrientation());
        } else {
            // Landscape, so dimensions are swapped
            //noinspection SuspiciousNameCombination
            VideoSurfaceBindings.scaleVideoAndFillView(
                    textureView, cameraDimensions.y, cameraDimensions.x, delegate.getDeviceOrientation());
        }
    }

    private boolean isLandscape() {
        return fragment.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }
}
