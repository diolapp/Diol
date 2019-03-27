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

package app.diol.contacts.common.model.dataitem;

import android.content.ContentValues;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;

/**
 * Represents a group memebership data item, wrapping the columns in {@link
 * ContactsContract.CommonDataKinds.GroupMembership}.
 */
public class GroupMembershipDataItem extends DataItem {

    /* package */ GroupMembershipDataItem(ContentValues values) {
        super(values);
    }

    public Long getGroupRowId() {
        return getContentValues().getAsLong(GroupMembership.GROUP_ROW_ID);
    }

    public String getGroupSourceId() {
        return getContentValues().getAsString(GroupMembership.GROUP_SOURCE_ID);
    }
}
