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

import app.diol.dialer.common.Assert;

/**
 * A module that inserts a grey line divider into {@link HistoryItemActionModule}. Layout it
 * provided in R.layout.divider_layout.xml
 */
public final class DividerModule implements HistoryItemActionModule {

    @Override
    public int getStringId() {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public int getDrawableId() {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public boolean onClick() {
        throw Assert.createUnsupportedOperationFailException();
    }
}
