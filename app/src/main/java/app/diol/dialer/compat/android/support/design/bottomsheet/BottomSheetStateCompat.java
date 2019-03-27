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

package app.diol.dialer.compat.android.support.design.bottomsheet;

import android.support.design.widget.BottomSheetBehavior;

/**
 * Provides access to bottom sheet states.
 */
public final class BottomSheetStateCompat {

    /**
     * The bottom sheet is dragging.
     */
    public static final int STATE_DRAGGING = BottomSheetBehavior.STATE_DRAGGING;

    /**
     * The bottom sheet is settling.
     */
    public static final int STATE_SETTLING = BottomSheetBehavior.STATE_SETTLING;

    /**
     * The bottom sheet is expanded.
     */
    public static final int STATE_EXPANDED = BottomSheetBehavior.STATE_EXPANDED;

    /**
     * The bottom sheet is collapsed.
     */
    public static final int STATE_COLLAPSED = BottomSheetBehavior.STATE_COLLAPSED;

    /**
     * The bottom sheet is hidden.
     */
    public static final int STATE_HIDDEN = BottomSheetBehavior.STATE_HIDDEN;

    /**
     * The bottom sheet is half-expanded (not public yet).
     */
    public static final int STATE_HALF_EXPANDED = 6;

    private BottomSheetStateCompat() {
    }
}
