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
import android.hardware.Camera;
import android.os.Parcelable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

/**
 * A software rendered preview surface for the camera. This renders slower and causes more jank, so
 * HardwareCameraPreview is preferred if possible.
 *
 * <p>There is a significant amount of duplication between HardwareCameraPreview and
 * SoftwareCameraPreview which we can't easily share due to a lack of multiple inheritance, The
 * implementations of the shared methods are delegated to CameraPreview
 */
public class SoftwareCameraPreview extends SurfaceView implements CameraPreview.CameraPreviewHost {
    private final CameraPreview preview;

    public SoftwareCameraPreview(final Context context) {
        super(context);
        preview = new CameraPreview(this);
        getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(final SurfaceHolder surfaceHolder) {
                                CameraManager.get().setSurface(preview);
                            }

                            @Override
                            public void surfaceChanged(
                                    final SurfaceHolder surfaceHolder,
                                    final int format,
                                    final int width,
                                    final int height) {
                                CameraManager.get().setSurface(preview);
                            }

                            @Override
                            public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
                                CameraManager.get().setSurface(null);
                            }
                        });
    }

    @Override
    public void setShown() {
        preview.setShown();
    }

    @Override
    protected void onVisibilityChanged(final View changedView, final int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        preview.onVisibilityChanged(visibility);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        preview.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        preview.onAttachedToWindow();
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        super.onRestoreInstanceState(state);
        preview.onRestoreInstanceState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = preview.getWidthMeasureSpec(widthMeasureSpec, heightMeasureSpec);
        heightMeasureSpec = preview.getHeightMeasureSpec(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean isValid() {
        return getHolder() != null;
    }

    @Override
    public void startPreview(final Camera camera) throws IOException {
        camera.setPreviewDisplay(getHolder());
    }

    @Override
    public void onCameraPermissionGranted() {
        preview.onCameraPermissionGranted();
    }
}
