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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Focusing overlay.
 */
public class RenderOverlay extends FrameLayout {

    private RenderView renderView;
    private List<Renderer> clients;
    // reverse list of touch clients
    private List<Renderer> touchClients;
    private int[] position = new int[2];
    public RenderOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        renderView = new RenderView(context);
        addView(renderView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        clients = new ArrayList<>(10);
        touchClients = new ArrayList<>(10);
        setWillNotDraw(false);

        addRenderer(new PieRenderer(context));
    }

    public PieRenderer getPieRenderer() {
        for (Renderer renderer : clients) {
            if (renderer instanceof PieRenderer) {
                return (PieRenderer) renderer;
            }
        }
        return null;
    }

    public void addRenderer(Renderer renderer) {
        clients.add(renderer);
        renderer.setOverlay(this);
        if (renderer.handlesTouch()) {
            touchClients.add(0, renderer);
        }
        renderer.layout(getLeft(), getTop(), getRight(), getBottom());
    }

    public void addRenderer(int pos, Renderer renderer) {
        clients.add(pos, renderer);
        renderer.setOverlay(this);
        renderer.layout(getLeft(), getTop(), getRight(), getBottom());
    }

    public void remove(Renderer renderer) {
        clients.remove(renderer);
        renderer.setOverlay(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        return false;
    }

    private void adjustPosition() {
        getLocationInWindow(position);
    }

    public void update() {
        renderView.invalidate();
    }

    /**
     * Render interface.
     */
    interface Renderer {
        boolean handlesTouch();

        boolean onTouchEvent(MotionEvent evt);

        void setOverlay(RenderOverlay overlay);

        void layout(int left, int top, int right, int bottom);

        void draw(Canvas canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    private class RenderView extends View {

        public RenderView(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            if (touchClients != null) {
                boolean res = false;
                for (Renderer client : touchClients) {
                    res |= client.onTouchEvent(evt);
                }
                return res;
            }
            return false;
        }

        @Override
        public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            adjustPosition();
            super.onLayout(changed, left, top, right, bottom);
            if (clients == null) {
                return;
            }
            for (Renderer renderer : clients) {
                renderer.layout(left, top, right, bottom);
            }
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (clients == null) {
                return;
            }
            boolean redraw = false;
            for (Renderer renderer : clients) {
                renderer.draw(canvas);
                redraw = redraw || ((OverlayRenderer) renderer).isVisible();
            }
            if (redraw) {
                invalidate();
            }
        }
    }
}
