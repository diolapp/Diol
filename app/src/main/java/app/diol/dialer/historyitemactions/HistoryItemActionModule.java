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

package app.diol.dialer.historyitemactions;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Modules used to build {@link HistoryItemActionBottomSheet}.
 *
 * <p>A history item is one that is displayed in the call log or the voicemail fragment.
 */
public interface HistoryItemActionModule {

    @StringRes
    int getStringId();

    @DrawableRes
    int getDrawableId();

    /**
     * Returns true if tint can be applied to the drawable.
     */
    default boolean tintDrawable() {
        return true;
    }

    /**
     * @return true if the bottom sheet should close, false otherwise
     */
    boolean onClick();
}
