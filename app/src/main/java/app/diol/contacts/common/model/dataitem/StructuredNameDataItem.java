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
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;

/**
 * Represents a structured name data item, wrapping the columns in {@link
 * ContactsContract.CommonDataKinds.StructuredName}.
 */
public class StructuredNameDataItem extends DataItem {

    public StructuredNameDataItem() {
        super(new ContentValues());
        getContentValues().put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
    }

    /* package */ StructuredNameDataItem(ContentValues values) {
        super(values);
    }

    public String getDisplayName() {
        return getContentValues().getAsString(StructuredName.DISPLAY_NAME);
    }

    public void setDisplayName(String name) {
        getContentValues().put(StructuredName.DISPLAY_NAME, name);
    }

    public String getGivenName() {
        return getContentValues().getAsString(StructuredName.GIVEN_NAME);
    }

    public String getFamilyName() {
        return getContentValues().getAsString(StructuredName.FAMILY_NAME);
    }

    public String getPrefix() {
        return getContentValues().getAsString(StructuredName.PREFIX);
    }

    public String getMiddleName() {
        return getContentValues().getAsString(StructuredName.MIDDLE_NAME);
    }

    public String getSuffix() {
        return getContentValues().getAsString(StructuredName.SUFFIX);
    }

    public String getPhoneticGivenName() {
        return getContentValues().getAsString(StructuredName.PHONETIC_GIVEN_NAME);
    }

    public void setPhoneticGivenName(String name) {
        getContentValues().put(StructuredName.PHONETIC_GIVEN_NAME, name);
    }

    public String getPhoneticMiddleName() {
        return getContentValues().getAsString(StructuredName.PHONETIC_MIDDLE_NAME);
    }

    public void setPhoneticMiddleName(String name) {
        getContentValues().put(StructuredName.PHONETIC_MIDDLE_NAME, name);
    }

    public String getPhoneticFamilyName() {
        return getContentValues().getAsString(StructuredName.PHONETIC_FAMILY_NAME);
    }

    public void setPhoneticFamilyName(String name) {
        getContentValues().put(StructuredName.PHONETIC_FAMILY_NAME, name);
    }

    public String getFullNameStyle() {
        return getContentValues().getAsString(StructuredName.FULL_NAME_STYLE);
    }

    public boolean isSuperPrimary() {
        final ContentValues contentValues = getContentValues();
        return contentValues == null || !contentValues.containsKey(StructuredName.IS_SUPER_PRIMARY)
                ? false
                : contentValues.getAsBoolean(StructuredName.IS_SUPER_PRIMARY);
    }
}
