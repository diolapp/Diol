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
import android.provider.ContactsContract.CommonDataKinds.SipAddress;

/**
 * Represents a sip address data item, wrapping the columns in {@link
 * ContactsContract.CommonDataKinds.SipAddress}.
 */
public class SipAddressDataItem extends DataItem {

    /* package */ SipAddressDataItem(ContentValues values) {
        super(values);
    }

    public String getSipAddress() {
        return getContentValues().getAsString(SipAddress.SIP_ADDRESS);
    }

    public String getLabel() {
        return getContentValues().getAsString(SipAddress.LABEL);
    }
}
