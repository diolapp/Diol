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

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Wrapper for an account that includes a data set (which may be null).
 */
public class AccountWithDataSet implements Parcelable {

    // For Parcelable
    public static final Creator<AccountWithDataSet> CREATOR =
            new Creator<AccountWithDataSet>() {
                public AccountWithDataSet createFromParcel(Parcel source) {
                    return new AccountWithDataSet(source);
                }

                public AccountWithDataSet[] newArray(int size) {
                    return new AccountWithDataSet[size];
                }
            };
    private static final String STRINGIFY_SEPARATOR = "\u0001";
    private static final String ARRAY_STRINGIFY_SEPARATOR = "\u0002";
    private static final Pattern STRINGIFY_SEPARATOR_PAT =
            Pattern.compile(Pattern.quote(STRINGIFY_SEPARATOR));
    private static final Pattern ARRAY_STRINGIFY_SEPARATOR_PAT =
            Pattern.compile(Pattern.quote(ARRAY_STRINGIFY_SEPARATOR));
    private static final String[] ID_PROJECTION = new String[]{BaseColumns._ID};
    private static final Uri RAW_CONTACTS_URI_LIMIT_1 =
            RawContacts.CONTENT_URI
                    .buildUpon()
                    .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, "1")
                    .build();
    public final String name;
    public final String type;
    public final String dataSet;
    private final AccountTypeWithDataSet mAccountTypeWithDataSet;

    public AccountWithDataSet(String name, String type, String dataSet) {
        this.name = emptyToNull(name);
        this.type = emptyToNull(type);
        this.dataSet = emptyToNull(dataSet);
        mAccountTypeWithDataSet = AccountTypeWithDataSet.get(type, dataSet);
    }

    public AccountWithDataSet(Parcel in) {
        this.name = in.readString();
        this.type = in.readString();
        this.dataSet = in.readString();
        mAccountTypeWithDataSet = AccountTypeWithDataSet.get(type, dataSet);
    }

    private static String emptyToNull(String text) {
        return TextUtils.isEmpty(text) ? null : text;
    }

    private static StringBuilder addStringified(StringBuilder sb, AccountWithDataSet account) {
        if (!TextUtils.isEmpty(account.name)) {
            sb.append(account.name);
        }
        sb.append(STRINGIFY_SEPARATOR);
        if (!TextUtils.isEmpty(account.type)) {
            sb.append(account.type);
        }
        sb.append(STRINGIFY_SEPARATOR);
        if (!TextUtils.isEmpty(account.dataSet)) {
            sb.append(account.dataSet);
        }

        return sb;
    }

    /**
     * Unpack a string created by {@link #stringify}.
     *
     * @throws IllegalArgumentException if it's an invalid string.
     */
    public static AccountWithDataSet unstringify(String s) {
        final String[] array = STRINGIFY_SEPARATOR_PAT.split(s, 3);
        if (array.length < 3) {
            throw new IllegalArgumentException("Invalid string " + s);
        }
        return new AccountWithDataSet(
                array[0], array[1], TextUtils.isEmpty(array[2]) ? null : array[2]);
    }

    /**
     * Pack a list of {@link AccountWithDataSet} into a string.
     */
    public static String stringifyList(List<AccountWithDataSet> accounts) {
        final StringBuilder sb = new StringBuilder();

        for (AccountWithDataSet account : accounts) {
            if (sb.length() > 0) {
                sb.append(ARRAY_STRINGIFY_SEPARATOR);
            }
            addStringified(sb, account);
        }

        return sb.toString();
    }

    /**
     * Unpack a list of {@link AccountWithDataSet} into a string.
     *
     * @throws IllegalArgumentException if it's an invalid string.
     */
    public static List<AccountWithDataSet> unstringifyList(String s) {
        final ArrayList<AccountWithDataSet> ret = new ArrayList<>();
        if (TextUtils.isEmpty(s)) {
            return ret;
        }

        final String[] array = ARRAY_STRINGIFY_SEPARATOR_PAT.split(s);

        for (int i = 0; i < array.length; i++) {
            ret.add(unstringify(array[i]));
        }

        return ret;
    }

    public boolean isLocalAccount() {
        return name == null && type == null;
    }

    public Account getAccountOrNull() {
        if (name != null && type != null) {
            return new Account(name, type);
        }
        return null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(dataSet);
    }

    public AccountTypeWithDataSet getAccountTypeWithDataSet() {
        return mAccountTypeWithDataSet;
    }

    /**
     * Return {@code true} if this account has any contacts in the database. Touches DB. Don't use in
     * the UI thread.
     */
    public boolean hasData(Context context) {
        final String BASE_SELECTION =
                RawContacts.ACCOUNT_TYPE + " = ?" + " AND " + RawContacts.ACCOUNT_NAME + " = ?";
        final String selection;
        final String[] args;
        if (TextUtils.isEmpty(dataSet)) {
            selection = BASE_SELECTION + " AND " + RawContacts.DATA_SET + " IS NULL";
            args = new String[]{type, name};
        } else {
            selection = BASE_SELECTION + " AND " + RawContacts.DATA_SET + " = ?";
            args = new String[]{type, name, dataSet};
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

    public boolean equals(Object obj) {
        if (obj instanceof AccountWithDataSet) {
            AccountWithDataSet other = (AccountWithDataSet) obj;
            return Objects.equals(name, other.name)
                    && Objects.equals(type, other.type)
                    && Objects.equals(dataSet, other.dataSet);
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dataSet != null ? dataSet.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "AccountWithDataSet {name=" + name + ", type=" + type + ", dataSet=" + dataSet + "}";
    }

    /**
     * Pack the instance into a string.
     */
    public String stringify() {
        return addStringified(new StringBuilder(), this).toString();
    }
}
