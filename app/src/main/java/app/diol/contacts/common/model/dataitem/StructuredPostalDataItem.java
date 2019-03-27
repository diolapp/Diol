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
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

/**
 * Represents a structured postal data item, wrapping the columns in {@link
 * ContactsContract.CommonDataKinds.StructuredPostal}.
 */
public class StructuredPostalDataItem extends DataItem {

    /* package */ StructuredPostalDataItem(ContentValues values) {
        super(values);
    }

    public String getFormattedAddress() {
        return getContentValues().getAsString(StructuredPostal.FORMATTED_ADDRESS);
    }

    public String getLabel() {
        return getContentValues().getAsString(StructuredPostal.LABEL);
    }

    public String getStreet() {
        return getContentValues().getAsString(StructuredPostal.STREET);
    }

    public String getPOBox() {
        return getContentValues().getAsString(StructuredPostal.POBOX);
    }

    public String getNeighborhood() {
        return getContentValues().getAsString(StructuredPostal.NEIGHBORHOOD);
    }

    public String getCity() {
        return getContentValues().getAsString(StructuredPostal.CITY);
    }

    public String getRegion() {
        return getContentValues().getAsString(StructuredPostal.REGION);
    }

    public String getPostcode() {
        return getContentValues().getAsString(StructuredPostal.POSTCODE);
    }

    public String getCountry() {
        return getContentValues().getAsString(StructuredPostal.COUNTRY);
    }
}
