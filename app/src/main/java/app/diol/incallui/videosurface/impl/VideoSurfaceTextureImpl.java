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

package app.diol.incallui.videosurface.impl;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.util.Locale;
import java.util.Objects;

import app.diol.dialer.common.LogUtil;
import app.diol.incallui.videosurface.protocol.VideoSurfaceDelegate;
import app.diol.incallui.videosurface.protocol.VideoSurfaceTexture;

/**
 * Represents a {@link TextureView} and its associated {@link SurfaceTexture} and {@link Surface}.
 * Used to manage the lifecycle of these objects across device orientation changes.
 */
public class VideoSurfaceTextureImpl implements VideoSurfaceTexture {
    @SurfaceType
    private final int surfaceType;
    private final boolean isPixel2017;

    private VideoSurfaceDelegate delegate;
    private TextureView textureView;
    private Surface savedSurface;
    private SurfaceTexture savedSurfaceTexture;
    private Point surfaceDimensions;
    private Point sourceVideoDimensions;
    private boolean isDoneWithSurface;

    public VideoSurfaceTextureImpl(boolean isPixel2017, @SurfaceType int surfaceType) {
        this.isPixel2017 = isPixel2017;
        this.surfaceType = surfaceType;
    }

    @Override
    public void setDelegate(VideoSurfaceDelegate delegate) {
        LogUtil.i("VideoSurfaceTextureImpl.setDelegate", "delegate: " + delegate + " " + toString());
        this.delegate = delegate;
    }

    @Override
    public int getSurfaceType() {
        return surfaceType;
    }

    @Override
    public Surface getSavedSurface() {
        return savedSurface;
    }

    @Override
    public Point getSurfaceDimensions() {
        return surfaceDimensions;
    }

    @Override
    public void setSurfaceDimensions(Point surfaceDimensions) {
        LogUtil.i(
                "VideoSurfaceTextureImpl.setSurfaceDimensions",
                "surfaceDimensions: " + surfaceDimensions + " " + toString());
        this.surfaceDimensions = surfaceDimensions;
        if (surfaceDimensions != null && savedSurfaceTexture != null) {
            // Only do this on O (not at least O) because we expect this issue to be fixed in OMR1
            if (VERSION.SDK_INT == VERSION_CODES.O && isPixel2017) {
                LogUtil.i(
                        "VideoSurfaceTextureImpl.setSurfaceDimensions",
                        "skip setting default buffer size on Pixel 2017 ODR");
                return;
            }
            savedSurfaceTexture.setDefaultBufferSize(surfaceDimensions.x, surfaceDimensions.y);
        }
    }

    @Override
    public Point getSourceVideoDimensions() {
        return sourceVideoDimensions;
    }

    @Override
    public void setSourceVideoDimensions(Point sourceVideoDimensions) {
        this.sourceVideoDimensions = sourceVideoDimensions;
    }

    @Override
    public void attachToTextureView(TextureView textureView) {
        if (this.textureView == textureView) {
            return;
        }
        LogUtil.i("VideoSurfaceTextureImpl.attachToTextureView", toString());

        if (this.textureView != null) {
            this.textureView.setOnClickListener(null);
            this.textureView.setSurfaceTextureListener(null);
        }

        this.textureView = textureView;
        textureView.setSurfaceTextureListener(new SurfaceTextureListener());
        textureView.setOnClickListener(new OnClickListener());

        boolean areSameSurfaces = Objects.equals(savedSurfaceTexture, textureView.getSurfaceTexture());
        LogUtil.i("VideoSurfaceTextureImpl.attachToTextureView", "areSameSurfaces: " + areSameSurfaces);
        if (savedSurfaceTexture != null && !areSameSurfaces) {
            textureView.setSurfaceTexture(savedSurfaceTexture);
            if (surfaceDimensions != null && createSurface(surfaceDimensions.x, surfaceDimensions.y)) {
                onSurfaceCreated();
            }
        }
        isDoneWithSurface = false;
    }

    @Override
    public void setDoneWithSurface() {
        LogUtil.i("VideoSurfaceTextureImpl.setDoneWithSurface", toString());
        isDoneWithSurface = true;
        if (textureView != null && textureView.isAvailable()) {
            return;
        }
        if (savedSurface != null) {
            onSurfaceReleased();
            savedSurface.release();
            savedSurface = null;
        }
        if (savedSurfaceTexture != null) {
            savedSurfaceTexture.release();
            savedSurfaceTexture = null;
        }
    }

    private boolean createSurface(int width, int height) {
        LogUtil.i(
                "VideoSurfaceTextureImpl.createSurface",
                "width: " + width + ", height: " + height + " " + toString());
        savedSurfaceTexture.setDefaultBufferSize(width, height);
        if (savedSurface != null) {
            savedSurface.release();
        }
        savedSurface = new Surface(savedSurfaceTexture);
        return true;
    }

    private void onSurfaceCreated() {
        if (delegate != null) {
            delegate.onSurfaceCreated(this);
        } else {
            LogUtil.e("VideoSurfaceTextureImpl.onSurfaceCreated", "delegate is null. " + toString());
        }
    }

    private void onSurfaceReleased() {
        if (delegate != null) {
            delegate.onSurfaceReleased(this);
        } else {
            LogUtil.e("VideoSurfaceTextureImpl.onSurfaceReleased", "delegate is null. " + toString());
        }
    }

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "VideoSurfaceTextureImpl<%s%s%s%s>",
                (surfaceType == SURFACE_TYPE_LOCAL ? "local, " : "remote, "),
                (savedSurface == null ? "no-surface, " : ""),
                (savedSurfaceTexture == null ? "no-texture, " : ""),
                (surfaceDimensions == null
                        ? "(-1 x -1)"
                        : (surfaceDimensions.x + " x " + surfaceDimensions.y)));
    }

    private class SurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture newSurfaceTexture, int width, int height) {
            LogUtil.i(
                    "SurfaceTextureListener.onSurfaceTextureAvailable",
                    "newSurfaceTexture: "
                            + newSurfaceTexture
                            + " "
                            + VideoSurfaceTextureImpl.this.toString());

            // Where there is no saved {@link SurfaceTexture} available, use the newly created one.
            // If a saved {@link SurfaceTexture} is available, we are re-creating after an
            // orientation change.
            boolean surfaceCreated;
            if (savedSurfaceTexture == null) {
                savedSurfaceTexture = newSurfaceTexture;
                surfaceCreated = createSurface(width, height);
            } else {
                // A saved SurfaceTexture was found.
                LogUtil.i(
                        "SurfaceTextureListener.onSurfaceTextureAvailable", "replacing with cached surface...");
                textureView.setSurfaceTexture(savedSurfaceTexture);
                surfaceCreated = true;
            }

            // Inform the delegate that the surface is available.
            if (surfaceCreated) {
                onSurfaceCreated();
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture destroyedSurfaceTexture) {
            LogUtil.i(
                    "SurfaceTextureListener.onSurfaceTextureDestroyed",
                    "destroyedSurfaceTexture: %s, %s, isDoneWithSurface: %b",
                    destroyedSurfaceTexture,
                    VideoSurfaceTextureImpl.this.toString(),
                    isDoneWithSurface);
            if (delegate != null) {
                delegate.onSurfaceDestroyed(VideoSurfaceTextureImpl.this);
            } else {
                LogUtil.e("SurfaceTextureListener.onSurfaceTextureDestroyed", "delegate is null");
            }

            if (isDoneWithSurface) {
                onSurfaceReleased();
                if (savedSurface != null) {
                    savedSurface.release();
                    savedSurface = null;
                }
            }
            return isDoneWithSurface;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (delegate != null) {
                delegate.onSurfaceClick(VideoSurfaceTextureImpl.this);
            } else {
                LogUtil.e("OnClickListener.onClick", "delegate is null");
            }
        }
    }
}
