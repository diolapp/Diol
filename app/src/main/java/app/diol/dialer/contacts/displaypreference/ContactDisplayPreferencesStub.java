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

package app.diol.dialer.contacts.displaypreference;

import javax.inject.Inject;

/**
 * Stub implementation of {@link ContactDisplayPreferences} that always returns primary. Used when
 * the device is locked and regular storage is not available.
 */
public final class ContactDisplayPreferencesStub implements ContactDisplayPreferences {

    @Inject
    ContactDisplayPreferencesStub() {
    }

    @Override
    public DisplayOrder getDisplayOrder() {
        return DisplayOrder.PRIMARY;
    }

    @Override
    public void setDisplayOrder(DisplayOrder displayOrder) {
        // Do nothing
    }

    @Override
    public SortOrder getSortOrder() {
        return SortOrder.BY_PRIMARY;
    }

    @Override
    public void setSortOrder(SortOrder sortOrder) {
        // Do nothing
    }
}
