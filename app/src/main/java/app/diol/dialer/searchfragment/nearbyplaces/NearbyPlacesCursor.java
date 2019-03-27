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
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.support.annotation.Nullable;

import app.diol.R;
import app.diol.dialer.searchfragment.common.SearchCursor;

/**
 * {@link SearchCursor} implementation for displaying on nearby places.
 */
final class NearbyPlacesCursor extends MergeCursor implements SearchCursor {

    private final Cursor nearbyPlacesCursor;
    private final long directoryId;

    private NearbyPlacesCursor(Cursor[] cursors, long directoryId) {
        super(cursors);
        nearbyPlacesCursor = cursors[1];
        this.directoryId = directoryId;
    }

    /**
     * @param directoryId unique directory id that doesn't collide with other remote/local
     *                    directories. directoryIds are needed to load the correct quick contact card.
     */
    static NearbyPlacesCursor newInstance(
            Context context, Cursor nearbyPlacesCursor, long directoryId) {
        MatrixCursor headerCursor = new MatrixCursor(HEADER_PROJECTION);
        headerCursor.addRow(new String[]{context.getString(R.string.nearby_places)});
        return new NearbyPlacesCursor(new Cursor[]{headerCursor, nearbyPlacesCursor}, directoryId);
    }

    @Override
    public boolean isHeader() {
        return isFirst();
    }

    @Override
    public boolean updateQuery(@Nullable String query) {
        // When the query changes, a new network request is made for nearby places. Meaning this cursor
        // will be closed and another created, so return false.
        return false;
    }

    @Override
    public int getCount() {
        // If we don't have any contents, we don't want to show the header
        if (nearbyPlacesCursor == null || nearbyPlacesCursor.isClosed()) {
            return 0;
        }

        int count = nearbyPlacesCursor.getCount();
        return count == 0 ? 0 : count + 1;
    }

    @Override
    public long getDirectoryId() {
        return directoryId;
    }
}
