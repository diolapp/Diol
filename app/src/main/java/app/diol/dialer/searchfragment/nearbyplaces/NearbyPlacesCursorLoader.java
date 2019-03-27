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

package app.diol.dialer.searchfragment.nearbyplaces;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import java.util.List;

import app.diol.contacts.common.extensions.PhoneDirectoryExtenderAccessor;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.searchfragment.common.Projections;

/**
 * Cursor loader for nearby places search results.
 */
public final class NearbyPlacesCursorLoader extends CursorLoader {

    private static final String MAX_RESULTS = "3";
    private static final long INVALID_DIRECTORY_ID = Long.MAX_VALUE;
    private final long directoryId;

    /**
     * @param directoryIds List of directoryIds associated with all directories on device. Required in
     *                     order to find a directory ID for the nearby places cursor that doesn't collide with
     *                     existing directories.
     */
    public NearbyPlacesCursorLoader(Context context, String query, @NonNull List<Long> directoryIds) {
        super(context, getContentUri(context, query), Projections.DATA_PROJECTION, null, null, null);
        this.directoryId = getDirectoryId(directoryIds);
    }

    private static Uri getContentUri(Context context, String query) {
        return PhoneDirectoryExtenderAccessor.get(context)
                .getContentUri()
                .buildUpon()
                .appendPath(query)
                .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, MAX_RESULTS)
                .build();
    }

    private static long getDirectoryId(List<Long> directoryIds) {
        if (directoryIds.isEmpty()) {
            return INVALID_DIRECTORY_ID;
        }

        // The Directory.LOCAL_INVISIBLE might not be a directory we use, but we can't reuse it's
        // "special" ID.
        long maxId = ContactsContract.Directory.LOCAL_INVISIBLE;
        for (int i = 0, n = directoryIds.size(); i < n; i++) {
            long id = directoryIds.get(i);
            if (id > maxId) {
                maxId = id;
            }
        }
        // Add one so that the nearby places ID doesn't collide with extended directory IDs.
        return maxId + 1;
    }

    @Override
    public Cursor loadInBackground() {
        if (directoryId == INVALID_DIRECTORY_ID) {
            LogUtil.i("NearbyPlacesCursorLoader.loadInBackground", "directory id not set.");
            return null;
        }
        return NearbyPlacesCursor.newInstance(getContext(), super.loadInBackground(), directoryId);
    }
}
