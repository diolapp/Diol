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

package app.diol.dialer.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * {@link ViewPager} useful for disabled swiping between pages.
 */
public class LockableViewPager extends ViewPager {

    private boolean swipingLocked;

    public LockableViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public boolean isSwipingLocked() {
        return swipingLocked;
    }

    public void setSwipingLocked(boolean swipingLocked) {
        this.swipingLocked = swipingLocked;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return !swipingLocked && super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return !swipingLocked && super.onTouchEvent(motionEvent);
    }
}