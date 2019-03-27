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

package app.diol.contacts.common.list;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import app.diol.R;
import app.diol.dialer.theme.base.ThemeComponent;

public class ViewPagerTabStrip extends LinearLayout {

    private final Paint mSelectedUnderlinePaint;
    private int mSelectedUnderlineThickness;
    private int mIndexForSelection;
    private float mSelectionOffset;

    public ViewPagerTabStrip(Context context) {
        this(context, null);
    }

    public ViewPagerTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);

        final Resources res = context.getResources();

        mSelectedUnderlineThickness = res.getDimensionPixelSize(R.dimen.tab_selected_underline_height);
        int underlineColor = ThemeComponent.get(context).theme().getColorAccent();
        int backgroundColor = ThemeComponent.get(context).theme().getColorPrimary();

        mSelectedUnderlinePaint = new Paint();
        mSelectedUnderlinePaint.setColor(underlineColor);

        setBackgroundColor(backgroundColor);
        setWillNotDraw(false);
    }

    /**
     * Notifies this view that view pager has been scrolled. We save the tab index and selection
     * offset for interpolating the position and width of selection underline.
     */
    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mIndexForSelection = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int childCount = getChildCount();

        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mIndexForSelection);

            if (selectedTitle == null) {
                // The view pager's tab count changed but we weren't notified yet. Ignore this draw
                // pass, when we get a new selection we will update and draw the selection strip in
                // the correct place.
                return;
            }
            int selectedLeft = selectedTitle.getLeft();
            int selectedRight = selectedTitle.getRight();
            final boolean isRtl = isRtl();
            final boolean hasNextTab =
                    isRtl ? mIndexForSelection > 0 : (mIndexForSelection < (getChildCount() - 1));
            if ((mSelectionOffset > 0.0f) && hasNextTab) {
                // Draw the selection partway between the tabs
                View nextTitle = getChildAt(mIndexForSelection + (isRtl ? -1 : 1));
                int nextLeft = nextTitle.getLeft();
                int nextRight = nextTitle.getRight();

                selectedLeft =
                        (int) (mSelectionOffset * nextLeft + (1.0f - mSelectionOffset) * selectedLeft);
                selectedRight =
                        (int) (mSelectionOffset * nextRight + (1.0f - mSelectionOffset) * selectedRight);
            }

            int height = getHeight();
            canvas.drawRect(
                    selectedLeft,
                    height - mSelectedUnderlineThickness,
                    selectedRight,
                    height,
                    mSelectedUnderlinePaint);
        }
    }

    private boolean isRtl() {
        return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }
}
