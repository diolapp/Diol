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
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.text.TextUtils;

/**
 * Represents an event data item, wrapping the columns in {@link
 * ContactsContract.CommonDataKinds.Event}.
 */
public class EventDataItem extends DataItem {

    /* package */ EventDataItem(ContentValues values) {
        super(values);
    }

    public String getStartDate() {
        return getContentValues().getAsString(Event.START_DATE);
    }

    public String getLabel() {
        return getContentValues().getAsString(Event.LABEL);
    }

    @Override
    public boolean shouldCollapseWith(DataItem t, Context context) {
        if (!(t instanceof EventDataItem) || mKind == null || t.getDataKind() == null) {
            return false;
        }
        final EventDataItem that = (EventDataItem) t;
        // Events can be different (anniversary, birthday) but have the same start date
        if (!TextUtils.equals(getStartDate(), that.getStartDate())) {
            return false;
        } else if (!hasKindTypeColumn(mKind) || !that.hasKindTypeColumn(that.getDataKind())) {
            return hasKindTypeColumn(mKind) == that.hasKindTypeColumn(that.getDataKind());
        } else if (getKindTypeColumn(mKind) != that.getKindTypeColumn(that.getDataKind())) {
            return false;
        } else if (getKindTypeColumn(mKind) == Event.TYPE_CUSTOM
                && !TextUtils.equals(getLabel(), that.getLabel())) {
            // Check if custom types are not the same
            return false;
        }
        return true;
    }
}
