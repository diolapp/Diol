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
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Class that handles enabling/disabling swiping between @{ViewPagerTabs}.
 */
public class DialerViewPager extends ViewPager {

    private boolean enableSwipingPages;

    public DialerViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        enableSwipingPages = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (enableSwipingPages) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enableSwipingPages) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    public void setEnableSwipingPages(boolean enabled) {
        enableSwipingPages = enabled;
    }
}
