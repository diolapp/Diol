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

package app.diol.dialer.blocking;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.database.Database;
import app.diol.dialer.database.DialerDatabaseHelper;
import app.diol.dialer.database.FilteredNumberContract;
import app.diol.dialer.database.FilteredNumberContract.FilteredNumberColumns;
import app.diol.dialer.location.GeoUtil;

/**
 * Filtered number content provider.
 */
@Deprecated
public class FilteredNumberProvider extends ContentProvider {

    private static final int FILTERED_NUMBERS_TABLE = 1;
    private static final int FILTERED_NUMBERS_TABLE_ID = 2;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private DialerDatabaseHelper dialerDatabaseHelper;

    @Override
    public boolean onCreate() {
        dialerDatabaseHelper = Database.get(getContext()).getDatabaseHelper(getContext());
        if (dialerDatabaseHelper == null) {
            return false;
        }
        uriMatcher.addURI(
                FilteredNumberContract.AUTHORITY,
                FilteredNumberContract.FilteredNumber.FILTERED_NUMBERS_TABLE,
                FILTERED_NUMBERS_TABLE);
        uriMatcher.addURI(
                FilteredNumberContract.AUTHORITY,
                FilteredNumberContract.FilteredNumber.FILTERED_NUMBERS_TABLE + "/#",
                FILTERED_NUMBERS_TABLE_ID);
        return true;
    }

    @Override
    public Cursor query(
            Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = dialerDatabaseHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DialerDatabaseHelper.Tables.FILTERED_NUMBER_TABLE);
        final int match = uriMatcher.match(uri);
        switch (match) {
            case FILTERED_NUMBERS_TABLE:
                break;
            case FILTERED_NUMBERS_TABLE_ID:
                qb.appendWhere(FilteredNumberColumns._ID + "=" + ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);
        if (c != null) {
            c.setNotificationUri(
                    getContext().getContentResolver(), FilteredNumberContract.FilteredNumber.CONTENT_URI);
        } else {
            LogUtil.d("FilteredNumberProvider.query", "CURSOR WAS NULL");
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return FilteredNumberContract.FilteredNumber.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dialerDatabaseHelper.getWritableDatabase();
        setDefaultValues(values);
        long id = db.insert(DialerDatabaseHelper.Tables.FILTERED_NUMBER_TABLE, null, values);
        if (id < 0) {
            return null;
        }
        notifyChange(uri);
        return ContentUris.withAppendedId(uri, id);
    }

    @VisibleForTesting
    protected long getCurrentTimeMs() {
        return System.currentTimeMillis();
    }

    private void setDefaultValues(ContentValues values) {
        if (values.getAsString(FilteredNumberColumns.COUNTRY_ISO) == null) {
            values.put(FilteredNumberColumns.COUNTRY_ISO, GeoUtil.getCurrentCountryIso(getContext()));
        }
        if (values.getAsInteger(FilteredNumberColumns.TIMES_FILTERED) == null) {
            values.put(FilteredNumberContract.FilteredNumberColumns.TIMES_FILTERED, 0);
        }
        if (values.getAsLong(FilteredNumberColumns.CREATION_TIME) == null) {
            values.put(FilteredNumberColumns.CREATION_TIME, getCurrentTimeMs());
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dialerDatabaseHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case FILTERED_NUMBERS_TABLE:
                break;
            case FILTERED_NUMBERS_TABLE_ID:
                selection = getSelectionWithId(selection, ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        int rows =
                db.delete(DialerDatabaseHelper.Tables.FILTERED_NUMBER_TABLE, selection, selectionArgs);
        if (rows > 0) {
            notifyChange(uri);
        }
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dialerDatabaseHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case FILTERED_NUMBERS_TABLE:
                break;
            case FILTERED_NUMBERS_TABLE_ID:
                selection = getSelectionWithId(selection, ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        int rows =
                db.update(
                        DialerDatabaseHelper.Tables.FILTERED_NUMBER_TABLE, values, selection, selectionArgs);
        if (rows > 0) {
            notifyChange(uri);
        }
        return rows;
    }

    private String getSelectionWithId(String selection, long id) {
        if (TextUtils.isEmpty(selection)) {
            return FilteredNumberContract.FilteredNumberColumns._ID + "=" + id;
        } else {
            return selection + "AND " + FilteredNumberContract.FilteredNumberColumns._ID + "=" + id;
        }
    }

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }
}
