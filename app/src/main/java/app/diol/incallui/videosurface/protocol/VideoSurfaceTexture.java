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

package app.diol.incallui.videosurface.protocol;

import android.graphics.Point;
import android.support.annotation.IntDef;
import android.view.Surface;
import android.view.TextureView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a surface texture for a video feed.
 */
public interface VideoSurfaceTexture {

    int SURFACE_TYPE_LOCAL = 1;
    int SURFACE_TYPE_REMOTE = 2;

    void setDelegate(VideoSurfaceDelegate delegate);

    int getSurfaceType();

    Surface getSavedSurface();

    Point getSurfaceDimensions();

    void setSurfaceDimensions(Point surfaceDimensions);

    Point getSourceVideoDimensions();

    void setSourceVideoDimensions(Point sourceVideoDimensions);

    void attachToTextureView(TextureView textureView);

    void setDoneWithSurface();

    /**
     * Whether this represents the preview or remote display.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SURFACE_TYPE_LOCAL,
            SURFACE_TYPE_REMOTE,
    })
    @interface SurfaceType {
    }
}
