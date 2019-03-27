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

package app.diol.dialer.main.impl.toolbar;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupMenu;

import app.diol.R;

/**
 * Popup menu accessible from the search bar
 */
public final class MainToolbarMenu extends PopupMenu {

    public MainToolbarMenu(Context context, View anchor) {
        super(context, anchor, Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0);
    }

    public void showClearFrequents(boolean show) {
        getMenu().findItem(R.id.clear_frequents).setVisible(show);
    }

}
