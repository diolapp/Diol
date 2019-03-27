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

package app.diol.dialer.callcomposer.cameraui;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import app.diol.R;
import app.diol.dialer.callcomposer.camera.CameraManager;
import app.diol.dialer.callcomposer.camera.HardwareCameraPreview;
import app.diol.dialer.callcomposer.camera.SoftwareCameraPreview;
import app.diol.dialer.common.LogUtil;

/**
 * Used to display the view of the camera.
 */
public class CameraMediaChooserView extends FrameLayout {
    private static final String STATE_CAMERA_INDEX = "camera_index";
    private static final String STATE_SUPER = "super";

    // True if we have at least queued an update to the view tree to support software rendering
    // fallback
    private boolean isSoftwareFallbackActive;

    public CameraMediaChooserView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        final int cameraIndex = CameraManager.get().getCameraIndex();
        LogUtil.i("CameraMediaChooserView.onSaveInstanceState", "saving camera index:" + cameraIndex);
        bundle.putInt(STATE_CAMERA_INDEX, cameraIndex);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (!(state instanceof Bundle)) {
            return;
        }

        final Bundle bundle = (Bundle) state;
        final int cameraIndex = bundle.getInt(STATE_CAMERA_INDEX);
        super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));

        LogUtil.i(
                "CameraMediaChooserView.onRestoreInstanceState", "restoring camera index:" + cameraIndex);
        if (cameraIndex != -1) {
            CameraManager.get().selectCameraByIndex(cameraIndex);
        } else {
            resetState();
        }
    }

    public void resetState() {
        CameraManager.get().selectCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        // If the canvas isn't hardware accelerated, we have to replace the HardwareCameraPreview
        // with a SoftwareCameraPreview which supports software rendering
        if (!canvas.isHardwareAccelerated() && !isSoftwareFallbackActive) {
            isSoftwareFallbackActive = true;
            // Post modifying the tree since we can't modify the view tree during a draw pass
            post(
                    new Runnable() {
                        @Override
                        public void run() {
                            final HardwareCameraPreview cameraPreview =
                                    (HardwareCameraPreview) findViewById(R.id.camera_preview);
                            if (cameraPreview == null) {
                                return;
                            }
                            final ViewGroup parent = ((ViewGroup) cameraPreview.getParent());
                            final int index = parent.indexOfChild(cameraPreview);
                            final SoftwareCameraPreview softwareCameraPreview =
                                    new SoftwareCameraPreview(getContext());
                            // Be sure to remove the hardware view before adding the software view to
                            // prevent having 2 camera previews active at the same time
                            parent.removeView(cameraPreview);
                            parent.addView(softwareCameraPreview, index);
                        }
                    });
        }
    }
}
