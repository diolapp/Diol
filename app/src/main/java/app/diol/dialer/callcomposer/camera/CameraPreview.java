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

package app.diol.dialer.callcomposer.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;

import java.io.IOException;

import app.diol.dialer.common.Assert;
import app.diol.dialer.util.PermissionsUtil;

/**
 * Contains shared code for SoftwareCameraPreview and HardwareCameraPreview, cannot use inheritance
 * because those classes must inherit from separate Views, so those classes delegate calls to this
 * helper class. Specifics for each implementation are in CameraPreviewHost
 */
public class CameraPreview {
    private final CameraPreviewHost host;
    private int cameraWidth = -1;
    private int cameraHeight = -1;
    private boolean tabHasBeenShown = false;
    private OnTouchListener listener;

    public CameraPreview(final CameraPreviewHost host) {
        Assert.isNotNull(host);
        Assert.isNotNull(host.getView());
        this.host = host;
    }

    // This is set when the tab is actually selected.
    public void setShown() {
        tabHasBeenShown = true;
        maybeOpenCamera();
    }

    // Opening camera is very expensive. Most of the ANR reports seem to be related to the camera.
    // So we delay until the camera is actually needed.  See a bug
    private void maybeOpenCamera() {
        boolean visible = host.getView().getVisibility() == View.VISIBLE;
        if (tabHasBeenShown && visible && PermissionsUtil.hasCameraPermissions(getContext())) {
            CameraManager.get().openCamera();
        }
    }

    public void setSize(final Camera.Size size, final int orientation) {
        switch (orientation) {
            case 0:
            case 180:
                cameraWidth = size.width;
                cameraHeight = size.height;
                break;
            case 90:
            case 270:
            default:
                cameraWidth = size.height;
                cameraHeight = size.width;
        }
        host.getView().requestLayout();
    }

    public int getWidthMeasureSpec(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (cameraHeight >= 0) {
            final int width = View.MeasureSpec.getSize(widthMeasureSpec);
            return MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        } else {
            return widthMeasureSpec;
        }
    }

    public int getHeightMeasureSpec(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (cameraHeight >= 0) {
            final int orientation = getContext().getResources().getConfiguration().orientation;
            final int width = View.MeasureSpec.getSize(widthMeasureSpec);
            final float aspectRatio = (float) cameraWidth / (float) cameraHeight;
            int height;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                height = (int) (width * aspectRatio);
            } else {
                height = (int) (width / aspectRatio);
            }
            return View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        } else {
            return heightMeasureSpec;
        }
    }

    // onVisibilityChanged is set to Visible when the tab is _created_,
    //   which may be when the user is viewing a different tab.
    public void onVisibilityChanged(final int visibility) {
        if (PermissionsUtil.hasCameraPermissions(getContext())) {
            if (visibility == View.VISIBLE) {
                maybeOpenCamera();
            } else {
                CameraManager.get().closeCamera();
            }
        }
    }

    public Context getContext() {
        return host.getView().getContext();
    }

    public void setOnTouchListener(final View.OnTouchListener listener) {
        this.listener = listener;
        host.getView().setOnTouchListener(listener);
    }

    public void setFocusable(boolean focusable) {
        host.getView().setOnTouchListener(focusable ? listener : null);
    }

    public int getHeight() {
        return host.getView().getHeight();
    }

    public void onAttachedToWindow() {
        maybeOpenCamera();
    }

    public void onDetachedFromWindow() {
        CameraManager.get().closeCamera();
    }

    public void onRestoreInstanceState() {
        maybeOpenCamera();
    }

    public void onCameraPermissionGranted() {
        maybeOpenCamera();
    }

    /**
     * @return True if the view is valid and prepared for the camera to start showing the preview
     */
    public boolean isValid() {
        return host.isValid();
    }

    /**
     * Starts the camera preview on the current surface. Abstracts out the differences in API from the
     * CameraManager
     *
     * @throws IOException Which is caught by the CameraManager to display an error
     */
    public void startPreview(final Camera camera) throws IOException {
        host.startPreview(camera);
    }

    /**
     * Implemented by the camera for rendering.
     */
    public interface CameraPreviewHost {
        View getView();

        boolean isValid();

        void startPreview(final Camera camera) throws IOException;

        void onCameraPermissionGranted();

        void setShown();
    }
}
