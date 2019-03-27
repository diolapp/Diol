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

package app.diol.dialer.contactsfragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.diol.R;

/**
 * Widget to add fast scrolling to {@link ContactsFragment}.
 */
public class FastScroller extends RelativeLayout {

    private final int touchTargetWidth;

    private ContactsAdapter adapter;
    private LinearLayoutManager layoutManager;

    private TextView container;
    private View scrollBar;

    private boolean dragStarted;

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchTargetWidth =
                context.getResources().getDimensionPixelSize(R.dimen.fast_scroller_touch_target_width);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        container = findViewById(R.id.fast_scroller_container);
        scrollBar = findViewById(R.id.fast_scroller_scroll_bar);
    }

    void setup(ContactsAdapter adapter, LinearLayoutManager layoutManager) {
        this.adapter = adapter;
        this.layoutManager = layoutManager;
        setVisibility(VISIBLE);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // Don't override if touch event isn't within desired touch target and dragging hasn't started.
        if (!dragStarted && getWidth() - touchTargetWidth - event.getX() > 0) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragStarted = true;
                container.setVisibility(VISIBLE);
                scrollBar.setSelected(true);
                // fall through
            case MotionEvent.ACTION_MOVE:
                setContainerAndScrollBarPosition(event.getY());
                setRecyclerViewPosition(event.getY());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragStarted = false;
                container.setVisibility(INVISIBLE);
                scrollBar.setSelected(false);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public boolean isDragStarted() {
        return dragStarted;
    }

    private void setRecyclerViewPosition(float y) {
        final int itemCount = adapter.getItemCount();
        float scrolledPosition = getScrolledPercentage(y) * (float) itemCount;
        int targetPos = getValueInRange(0, itemCount - 1, (int) scrolledPosition);
        layoutManager.scrollToPositionWithOffset(targetPos, 0);
        container.setText(adapter.getHeaderString(targetPos));
        adapter.refreshHeaders();
    }

    // Returns a float in range [0, 1] which represents the position of the scroller.
    private float getScrolledPercentage(float y) {
        if (scrollBar.getY() == 0) {
            return 0f;
        } else if (scrollBar.getY() + scrollBar.getHeight() >= getHeight()) {
            return 1f;
        } else {
            return y / (float) getHeight();
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    void updateContainerAndScrollBarPosition(RecyclerView recyclerView) {
        if (!scrollBar.isSelected()) {
            int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
            int verticalScrollRange = recyclerView.computeVerticalScrollRange();
            float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - getHeight());
            setContainerAndScrollBarPosition(getHeight() * proportion);
        }
    }

    private void setContainerAndScrollBarPosition(float y) {
        int scrollBarHeight = scrollBar.getHeight();
        int containerHeight = container.getHeight();
        scrollBar.setY(
                getValueInRange(0, getHeight() - scrollBarHeight, (int) (y - scrollBarHeight / 2)));
        container.setY(
                getValueInRange(
                        0, getHeight() - containerHeight - scrollBarHeight / 2, (int) (y - containerHeight)));
    }
}
