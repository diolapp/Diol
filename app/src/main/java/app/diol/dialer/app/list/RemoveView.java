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

package app.diol.dialer.app.list;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import app.diol.R;

public class RemoveView extends FrameLayout {

    DragDropController dragDropController;
    TextView removeText;
    ImageView removeIcon;
    int unhighlightedColor;
    int highlightedColor;
    Drawable removeDrawable;

    public RemoveView(Context context) {
        super(context);
    }

    public RemoveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        removeText = (TextView) findViewById(R.id.remove_view_text);
        removeIcon = (ImageView) findViewById(R.id.remove_view_icon);
        final Resources r = getResources();
        unhighlightedColor = r.getColor(android.R.color.white);
        highlightedColor = r.getColor(R.color.remove_highlighted_text_color);
        removeDrawable = r.getDrawable(R.drawable.ic_remove);
    }

    public void setDragDropController(DragDropController controller) {
        dragDropController = controller;
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        final int action = event.getAction();
        switch (action) {
            case DragEvent.ACTION_DRAG_ENTERED:
                // TODO: This is temporary solution and should be removed once accessibility for
                // drag and drop is supported by framework(a bug).
                sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                setAppearanceHighlighted();
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                setAppearanceNormal();
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                if (dragDropController != null) {
                    dragDropController.handleDragHovered(this, (int) event.getX(), (int) event.getY());
                }
                break;
            case DragEvent.ACTION_DROP:
                sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                if (dragDropController != null) {
                    dragDropController.handleDragFinished((int) event.getX(), (int) event.getY(), true);
                }
                setAppearanceNormal();
                break;
        }
        return true;
    }

    private void setAppearanceNormal() {
        removeText.setTextColor(unhighlightedColor);
        removeIcon.setColorFilter(unhighlightedColor);
        invalidate();
    }

    private void setAppearanceHighlighted() {
        removeText.setTextColor(highlightedColor);
        removeIcon.setColorFilter(highlightedColor);
        invalidate();
    }
}
