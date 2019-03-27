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

import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

/**
 * AccessibilityDelegate that will filter out TYPE_WINDOW_CONTENT_CHANGED Used to suppress "Showing
 * items x of y" from firing of ListView whenever it's content changes. AccessibilityEvent can only
 * be rejected at a view's parent once it is generated, use addToParent() to add this delegate to
 * the parent.
 */
public class ContentChangedFilter extends AccessibilityDelegate {

    // the view we don't want TYPE_WINDOW_CONTENT_CHANGED to fire.
    private View view;

    private ContentChangedFilter(View view) {
        super();
        this.view = view;
    }

    /**
     * Add this delegate to the parent of @param view to filter out TYPE_WINDOW_CONTENT_CHANGED
     */
    public static void addToParent(View view) {
        View parent = (View) view.getParent();
        parent.setAccessibilityDelegate(new ContentChangedFilter(view));
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(
            ViewGroup host, View child, AccessibilityEvent event) {
        if (child == view) {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                return false;
            }
        }
        return super.onRequestSendAccessibilityEvent(host, child, event);
    }
}
