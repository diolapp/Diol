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

package app.diol.contacts.common.model.account;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import java.util.Objects;

/**
 * Encapsulates an "account type" string and a "data set" string.
 */
public class AccountTypeWithDataSet {

    private static final String[] ID_PROJECTION = new String[]{BaseColumns._ID};
    private static final Uri RAW_CONTACTS_URI_LIMIT_1 =
            RawContacts.CONTENT_URI
                    .buildUpon()
                    .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, "1")
                    .build();

    /**
     * account type. Can be null for fallback type.
     */
    public final String accountType;

    /**
     * dataSet may be null, but never be "".
     */
    public final String dataSet;

    private AccountTypeWithDataSet(String accountType, String dataSet) {
        this.accountType = TextUtils.isEmpty(accountType) ? null : accountType;
        this.dataSet = TextUtils.isEmpty(dataSet) ? null : dataSet;
    }

    public static AccountTypeWithDataSet get(String accountType, String dataSet) {
        return new AccountTypeWithDataSet(accountType, dataSet);
    }

    /**
     * Return true if there are any contacts in the database with this account type and data set.
     * Touches DB. Don't use in the UI thread.
     */
    public boolean hasData(Context context) {
        final String BASE_SELECTION = RawContacts.ACCOUNT_TYPE + " = ?";
        final String selection;
        final String[] args;
        if (TextUtils.isEmpty(dataSet)) {
            selection = BASE_SELECTION + " AND " + RawContacts.DATA_SET + " IS NULL";
            args = new String[]{accountType};
        } else {
            selection = BASE_SELECTION + " AND " + RawContacts.DATA_SET + " = ?";
            args = new String[]{accountType, dataSet};
        }

        final Cursor c =
                context
                        .getContentResolver()
                        .query(RAW_CONTACTS_URI_LIMIT_1, ID_PROJECTION, selection, args, null);
        if (c == null) {
            return false;
        }
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountTypeWithDataSet)) {
            return false;
        }

        AccountTypeWithDataSet other = (AccountTypeWithDataSet) o;
        return Objects.equals(accountType, other.accountType) && Objects.equals(dataSet, other.dataSet);
    }

    @Override
    public int hashCode() {
        return (accountType == null ? 0 : accountType.hashCode())
                ^ (dataSet == null ? 0 : dataSet.hashCode());
    }

    @Override
    public String toString() {
        return "[" + accountType + "/" + dataSet + "]";
    }
}
