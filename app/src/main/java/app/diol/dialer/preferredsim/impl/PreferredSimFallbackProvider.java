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

package app.diol.dialer.preferredsim.impl;

import android.Manifest.permission;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;

import app.diol.dialer.preferredsim.PreferredSimFallbackContract;
import app.diol.dialer.preferredsim.PreferredSimFallbackContract.PreferredSim;

/**
 * Content provider for preferred SIM columns that is only available in ContactsProvider after P.
 * Only supports {@link PreferredSimFallbackContract#CONTENT_URI} without id. Insert and delete not
 * supported because there are no current use case.
 *
 * @see PreferredSimFallbackContract
 */
public class PreferredSimFallbackProvider extends ContentProvider {

    private static final String UPDATE_ID_SELECTION = PreferredSim.DATA_ID + " = ?";

    private static final ImmutableMap<String, String> PROJECTION_MAP =
            ImmutableMap.of(
                    PreferredSim.DATA_ID,
                    PreferredSim.DATA_ID,
                    PreferredSim.PREFERRED_PHONE_ACCOUNT_COMPONENT_NAME,
                    PreferredSim.PREFERRED_PHONE_ACCOUNT_COMPONENT_NAME,
                    PreferredSim.PREFERRED_PHONE_ACCOUNT_ID,
                    PreferredSim.PREFERRED_PHONE_ACCOUNT_ID);

    private PreferredSimDatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new PreferredSimDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {
        checkReadContactsPermission();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setStrict(true);
        queryBuilder.setProjectionMap(PROJECTION_MAP);
        queryBuilder.setTables(PreferredSimDatabaseHelper.TABLE);
        return queryBuilder.query(
                databaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new IllegalArgumentException("Unsupported operation");
    }

    /**
     * A row should only be deleted through {@link android.provider.ContactsContract.Data}. Since
     * {@link android.provider.ContactsContract.Data#_ID} is AUTOINCREMENT and could not be reused,
     * rows in this database will simply be orphaned and not cleaned up. To unset preference, update
     * {@link PreferredSim#PREFERRED_PHONE_ACCOUNT_COMPONENT_NAME} and {@link
     * PreferredSim#PREFERRED_PHONE_ACCOUNT_ID} to {@code null}. Delete is only allowed from dialer so
     * simulator can wipe all preference.
     */
    @Override
    public int delete(
            @NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        checkWriteContactsPermission();

        if (PreferredSimFallbackContract.CONTENT_URI.equals(uri)
                && selection == null
                && selectionArgs == null) {
            return databaseHelper
                    .getWritableDatabase()
                    .delete(PreferredSimDatabaseHelper.TABLE, null, null);
        }

        if (!TextUtils.equals(getContext().getPackageName(), getCallingPackage())) {
            throw new IllegalArgumentException("Unsupported operation");
        }

        return databaseHelper
                .getWritableDatabase()
                .delete(PreferredSimDatabaseHelper.TABLE, selection, selectionArgs);
    }

    /**
     * Data will be inserted if {@link PreferredSim#DATA_ID} does not already exist in the database.
     * During update the whole row will be replaced.
     *
     * @param uri           must be {@link PreferredSimFallbackContract#CONTENT_URI}
     * @param values        must contains exactly the keys {@link
     *                      PreferredSim#PREFERRED_PHONE_ACCOUNT_COMPONENT_NAME} {@link
     *                      PreferredSim#PREFERRED_PHONE_ACCOUNT_ID}. The value may be {@code null}
     * @param selection     must equals "data_id = ?"
     * @param selectionArgs must contains exactly the {@link PreferredSim#DATA_ID}
     */
    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {
        checkWriteContactsPermission();
        if (values == null) {
            return 0;
        }
        if (!UPDATE_ID_SELECTION.equals(selection)
                || selectionArgs == null
                || selectionArgs.length != 1) {
            throw new IllegalArgumentException("Unsupported operation");
        }
        values.put(PreferredSim.DATA_ID, selectionArgs[0]);
        if (databaseHelper.getWritableDatabase().replace(PreferredSimDatabaseHelper.TABLE, null, values)
                == -1) {
            throw new IllegalStateException("update failed");
        }
        getContext().getContentResolver().notifyChange(PreferredSimFallbackContract.CONTENT_URI, null);
        return 1;
    }

    private void checkReadContactsPermission() {
        if (getContext().checkCallingOrSelfPermission(permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {
            throw new SecurityException("READ_CONTACTS required");
        }
    }

    private void checkWriteContactsPermission() {
        if (getContext().checkCallingOrSelfPermission(permission.WRITE_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {
            throw new SecurityException("WRITE_CONTACTS required");
        }
    }
}
