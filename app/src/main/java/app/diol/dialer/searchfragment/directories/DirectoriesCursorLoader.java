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

package app.diol.dialer.searchfragment.directories;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.util.PermissionsUtil;

/**
 * {@link CursorLoader} to load information about all directories (local and remote).
 *
 * <p>Information about a directory includes its ID, display name, etc, but doesn't include the
 * contacts in it.
 */
public final class DirectoriesCursorLoader extends CursorLoader {

    public static final String[] PROJECTION = {
            ContactsContract.Directory._ID,
            ContactsContract.Directory.DISPLAY_NAME,
            ContactsContract.Directory.PHOTO_SUPPORT,
    };

    // Indices of columns in PROJECTION
    private static final int ID = 0;
    private static final int DISPLAY_NAME = 1;
    private static final int PHOTO_SUPPORT = 2;

    public DirectoriesCursorLoader(Context context) {
        super(
                context,
                ContactsContract.Directory.ENTERPRISE_CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.Directory._ID);
    }

    /**
     * Creates a complete list of directories from the data set loaded by this loader.
     *
     * @param cursor A cursor pointing to the data set loaded by this loader. The caller must ensure
     *               the cursor is not null.
     * @return A list of directories.
     */
    public static List<Directory> toDirectories(Cursor cursor) {
        if (cursor == null) {
            LogUtil.i("DirectoriesCursorLoader.toDirectories", "Cursor was null");
            return new ArrayList<>();
        }

        List<Directory> directories = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            directories.add(
                    Directory.create(
                            cursor.getInt(ID),
                            cursor.getString(DISPLAY_NAME),
                            /* supportsPhotos = */ cursor.getInt(PHOTO_SUPPORT) != 0));
        }
        return directories;
    }

    @Override
    public Cursor loadInBackground() {
        if (!PermissionsUtil.hasContactsReadPermissions(getContext())) {
            LogUtil.i("DirectoriesCursorLoader.loadInBackground", "Contacts permission denied.");
            return null;
        }
        return super.loadInBackground();
    }

    /**
     * POJO representing the results returned from {@link DirectoriesCursorLoader}.
     */
    @AutoValue
    public abstract static class Directory {
        public static Directory create(long id, @Nullable String displayName, boolean supportsPhotos) {
            return new AutoValue_DirectoriesCursorLoader_Directory(id, displayName, supportsPhotos);
        }

        public abstract long getId();

        /**
         * Returns a user facing display name of the directory. Null if none exists.
         */
        public abstract @Nullable
        String getDisplayName();

        public abstract boolean supportsPhotos();
    }
}
