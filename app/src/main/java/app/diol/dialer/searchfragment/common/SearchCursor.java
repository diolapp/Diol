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

package app.diol.dialer.searchfragment.common;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * Base cursor interface needed for all cursors used in search.
 */
public interface SearchCursor extends Cursor {

    String[] HEADER_PROJECTION = {"header_text"};

    int HEADER_TEXT_POSITION = 0;

    /**
     * Returns true if the current cursor position is a header
     */
    boolean isHeader();

    /**
     * Notifies the cursor that the query has updated.
     *
     * @return true if the data set has changed.
     */
    boolean updateQuery(@NonNull String query);

    /**
     * Returns an ID unique to the directory this cursor reads from. Generally this value will be
     * related to {@link android.provider.ContactsContract.Directory} but could differ depending on
     * the implementation.
     */
    long getDirectoryId();
}
