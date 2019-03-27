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

package app.diol.dialer.searchfragment.list;

import android.database.MatrixCursor;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import app.diol.dialer.common.Assert;
import app.diol.dialer.searchfragment.common.SearchCursor;

/**
 * Manages all of the cursors needed for {@link SearchAdapter}.
 *
 * <p>This class accepts four data sources:
 *
 * <ul>
 * <li>A contacts cursor {@link #setContactsCursor(SearchCursor)}
 * <li>A google search results cursor {@link #setNearbyPlacesCursor(SearchCursor)}
 * <li>A work directory cursor {@link #setCorpDirectoryCursor(SearchCursor)}
 * <li>A list of action to be performed on a number {@link #setSearchActions(List)}
 * </ul>
 *
 * <p>The key purpose of this class is to compose three aforementioned cursors together to function
 * as one cursor. The key methods needed to utilize this class as a cursor are:
 *
 * <ul>
 * <li>{@link #getCursor(int)}
 * <li>{@link #getCount()}
 * <li>{@link #getRowType(int)}
 * </ul>
 */
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public final class SearchCursorManager {

    private static final LocationPermissionCursor LOCATION_PERMISSION_CURSOR =
            new LocationPermissionCursor(new String[0]);
    private SearchCursor contactsCursor = null;
    private SearchCursor nearbyPlacesCursor = null;
    private SearchCursor corpDirectoryCursor = null;
    private List<Integer> searchActions = new ArrayList<>();
    private boolean showLocationPermissionRequest;

    /**
     * Returns true if the cursor changed.
     */
    boolean setContactsCursor(@Nullable SearchCursor cursor) {
        if (cursor == contactsCursor) {
            return false;
        }

        if (cursor != null) {
            contactsCursor = cursor;
        } else {
            contactsCursor = null;
        }
        return true;
    }

    /**
     * Returns true if the cursor changed.
     */
    boolean setNearbyPlacesCursor(@Nullable SearchCursor cursor) {
        if (cursor == nearbyPlacesCursor) {
            return false;
        }

        if (cursor != null) {
            nearbyPlacesCursor = cursor;
        } else {
            nearbyPlacesCursor = null;
        }
        return true;
    }

    /**
     * Returns true if the value changed.
     */
    boolean showLocationPermissionRequest(boolean enabled) {
        if (showLocationPermissionRequest == enabled) {
            return false;
        }
        showLocationPermissionRequest = enabled;
        return true;
    }

    /**
     * Returns true if a cursor changed.
     */
    boolean setCorpDirectoryCursor(@Nullable SearchCursor cursor) {
        if (cursor == corpDirectoryCursor) {
            return false;
        }

        if (cursor != null) {
            corpDirectoryCursor = cursor;
        } else {
            corpDirectoryCursor = null;
        }
        return true;
    }

    boolean setQuery(String query) {
        boolean updated = false;
        if (contactsCursor != null) {
            updated = contactsCursor.updateQuery(query);
        }

        if (nearbyPlacesCursor != null) {
            updated |= nearbyPlacesCursor.updateQuery(query);
        }

        if (corpDirectoryCursor != null) {
            updated |= corpDirectoryCursor.updateQuery(query);
        }
        return updated;
    }

    /**
     * Sets search actions, returning true if different from existing actions.
     */
    boolean setSearchActions(List<Integer> searchActions) {
        if (!this.searchActions.equals(searchActions)) {
            this.searchActions = searchActions;
            return true;
        }
        return false;
    }

    /**
     * Returns {@link SearchActionViewHolder.Action}.
     */
    int getSearchAction(int position) {
        return searchActions.get(position - getCount() + searchActions.size());
    }

    /**
     * Returns the sum of counts of all cursors, including headers.
     */
    int getCount() {
        int count = 0;
        if (contactsCursor != null) {
            count += contactsCursor.getCount();
        }

        if (showLocationPermissionRequest) {
            count++;
        } else if (nearbyPlacesCursor != null) {
            count += nearbyPlacesCursor.getCount();
        }

        if (corpDirectoryCursor != null) {
            count += corpDirectoryCursor.getCount();
        }

        return count + searchActions.size();
    }

    @RowType
    int getRowType(int position) {
        int cursorCount = getCount();
        if (position >= cursorCount) {
            throw Assert.createIllegalStateFailException(
                    String.format("Invalid position: %d, cursor count: %d", position, cursorCount));
        } else if (position >= cursorCount - searchActions.size()) {
            return RowType.SEARCH_ACTION;
        }

        SearchCursor cursor = getCursor(position);
        if (cursor == contactsCursor) {
            return cursor.isHeader() ? RowType.CONTACT_HEADER : RowType.CONTACT_ROW;
        }

        if (cursor == LOCATION_PERMISSION_CURSOR) {
            return RowType.LOCATION_REQUEST;
        }

        if (cursor == nearbyPlacesCursor) {
            return cursor.isHeader() ? RowType.NEARBY_PLACES_HEADER : RowType.NEARBY_PLACES_ROW;
        }

        if (cursor == corpDirectoryCursor) {
            return cursor.isHeader() ? RowType.DIRECTORY_HEADER : RowType.DIRECTORY_ROW;
        }
        throw Assert.createIllegalStateFailException("No valid row type.");
    }

    /**
     * Gets cursor corresponding to position in coalesced list of search cursors.
     *
     * @param position in coalesced list of search cursors
     * @return Cursor moved to position specific to passed in position.
     */
    SearchCursor getCursor(int position) {
        if (showLocationPermissionRequest) {
            if (position == 0) {
                return LOCATION_PERMISSION_CURSOR;
            }
            position--;
        }

        if (contactsCursor != null) {
            int count = contactsCursor.getCount();

            if (position - count < 0) {
                contactsCursor.moveToPosition(position);
                return contactsCursor;
            }
            position -= count;
        }

        if (!showLocationPermissionRequest && nearbyPlacesCursor != null) {
            int count = nearbyPlacesCursor.getCount();

            if (position - count < 0) {
                nearbyPlacesCursor.moveToPosition(position);
                return nearbyPlacesCursor;
            }
            position -= count;
        }

        if (corpDirectoryCursor != null) {
            int count = corpDirectoryCursor.getCount();

            if (position - count < 0) {
                corpDirectoryCursor.moveToPosition(position);
                return corpDirectoryCursor;
            }
            position -= count;
        }

        throw Assert.createIllegalStateFailException("No valid cursor.");
    }

    /**
     * removes all cursors.
     */
    void clear() {
        contactsCursor = null;
        nearbyPlacesCursor = null;
        corpDirectoryCursor = null;
    }

    /**
     * IntDef for the different types of rows that can be shown when searching.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SearchCursorManager.RowType.INVALID,
            SearchCursorManager.RowType.CONTACT_HEADER,
            SearchCursorManager.RowType.CONTACT_ROW,
            SearchCursorManager.RowType.NEARBY_PLACES_HEADER,
            SearchCursorManager.RowType.NEARBY_PLACES_ROW,
            SearchCursorManager.RowType.DIRECTORY_HEADER,
            SearchCursorManager.RowType.DIRECTORY_ROW,
            SearchCursorManager.RowType.SEARCH_ACTION,
            SearchCursorManager.RowType.LOCATION_REQUEST
    })
    @interface RowType {
        int INVALID = 0;
        // TODO(calderwoodra) add suggestions header and list
        /**
         * Header to mark the start of contact rows.
         */
        int CONTACT_HEADER = 1;
        /**
         * A row containing contact information for contacts stored locally on device.
         */
        int CONTACT_ROW = 2;
        /**
         * Header to mark the end of contact rows and start of nearby places rows.
         */
        int NEARBY_PLACES_HEADER = 3;
        /**
         * A row containing nearby places information/search results.
         */
        int NEARBY_PLACES_ROW = 4;
        /**
         * Header to mark the end of the previous row set and start of directory rows.
         */
        int DIRECTORY_HEADER = 5;
        /**
         * A row containing contact information for contacts stored externally in corp directories.
         */
        int DIRECTORY_ROW = 6;
        /**
         * A row containing a search action
         */
        int SEARCH_ACTION = 7;
        /**
         * A row which requests location permission
         */
        int LOCATION_REQUEST = 8;
    }

    /**
     * No-op implementation of {@link android.database.Cursor} and {@link SearchCursor} for
     * representing location permission request row elements.
     */
    private static class LocationPermissionCursor extends MatrixCursor implements SearchCursor {

        LocationPermissionCursor(String[] columnNames) {
            super(columnNames);
        }

        @Override
        public boolean isHeader() {
            return false;
        }

        @Override
        public boolean updateQuery(String query) {
            return false;
        }

        @Override
        public long getDirectoryId() {
            return 0;
        }
    }
}
