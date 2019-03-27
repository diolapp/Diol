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
import android.provider.ContactsContract.CommonDataKinds.Phone;

import app.diol.dialer.phonenumberutil.PhoneNumberHelper;

/**
 * Represents a phone data item, wrapping the columns in {@link
 * android.provider.ContactsContract.CommonDataKinds.Phone}.
 */
public class PhoneDataItem extends DataItem {

    private static final String KEY_FORMATTED_PHONE_NUMBER = "formattedPhoneNumber";

    /* package */ PhoneDataItem(ContentValues values) {
        super(values);
    }

    public String getNumber() {
        return getContentValues().getAsString(Phone.NUMBER);
    }

    /**
     * Returns the normalized phone number in E164 format.
     */
    public String getNormalizedNumber() {
        return getContentValues().getAsString(Phone.NORMALIZED_NUMBER);
    }

    public String getFormattedPhoneNumber() {
        return getContentValues().getAsString(KEY_FORMATTED_PHONE_NUMBER);
    }

    public String getLabel() {
        return getContentValues().getAsString(Phone.LABEL);
    }

    public void computeFormattedPhoneNumber(Context context, String defaultCountryIso) {
        final String phoneNumber = getNumber();
        if (phoneNumber != null) {
            final String formattedPhoneNumber =
                    PhoneNumberHelper.formatNumber(
                            context, phoneNumber, getNormalizedNumber(), defaultCountryIso);
            getContentValues().put(KEY_FORMATTED_PHONE_NUMBER, formattedPhoneNumber);
        }
    }

    /**
     * Returns the formatted phone number (if already computed using {@link
     * #computeFormattedPhoneNumber}). Otherwise this method returns the unformatted phone number.
     */
    @Override
    public String buildDataStringForDisplay(Context context, DataKind kind) {
        final String formatted = getFormattedPhoneNumber();
        if (formatted != null) {
            return formatted;
        } else {
            return getNumber();
        }
    }
}
