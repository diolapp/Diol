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
import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.text.TextUtils;

/**
 * Represents a relation data item, wrapping the columns in {@link
 * ContactsContract.CommonDataKinds.Relation}.
 */
public class RelationDataItem extends DataItem {

    /* package */ RelationDataItem(ContentValues values) {
        super(values);
    }

    public String getName() {
        return getContentValues().getAsString(Relation.NAME);
    }

    public String getLabel() {
        return getContentValues().getAsString(Relation.LABEL);
    }

    @Override
    public boolean shouldCollapseWith(DataItem t, Context context) {
        if (!(t instanceof RelationDataItem) || mKind == null || t.getDataKind() == null) {
            return false;
        }
        final RelationDataItem that = (RelationDataItem) t;
        // Relations can have different types (assistant, father) but have the same name
        if (!TextUtils.equals(getName(), that.getName())) {
            return false;
        } else if (!hasKindTypeColumn(mKind) || !that.hasKindTypeColumn(that.getDataKind())) {
            return hasKindTypeColumn(mKind) == that.hasKindTypeColumn(that.getDataKind());
        } else if (getKindTypeColumn(mKind) != that.getKindTypeColumn(that.getDataKind())) {
            return false;
        } else if (getKindTypeColumn(mKind) == Relation.TYPE_CUSTOM
                && !TextUtils.equals(getLabel(), that.getLabel())) {
            // Check if custom types are not the same
            return false;
        }
        return true;
    }
}
