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

package app.diol.dialer.callcomposer.camera.camerafocus;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Abstract class that all Camera overlays should implement.
 */
public abstract class OverlayRenderer implements RenderOverlay.Renderer {

    protected RenderOverlay overlay;

    private int left;
    private int top;
    private int right;
    private int bottom;
    private boolean visible;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean vis) {
        visible = vis;
        update();
    }

    // default does not handle touch
    @Override
    public boolean handlesTouch() {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        return false;
    }

    public abstract void onDraw(Canvas canvas);

    @Override
    public void draw(Canvas canvas) {
        if (visible) {
            onDraw(canvas);
        }
    }

    @Override
    public void setOverlay(RenderOverlay overlay) {
        this.overlay = overlay;
    }

    @Override
    public void layout(int left, int top, int right, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    protected Context getContext() {
        if (overlay != null) {
            return overlay.getContext();
        } else {
            return null;
        }
    }

    public int getWidth() {
        return right - left;
    }

    public int getHeight() {
        return bottom - top;
    }

    protected void update() {
        if (overlay != null) {
            overlay.update();
        }
    }
}
