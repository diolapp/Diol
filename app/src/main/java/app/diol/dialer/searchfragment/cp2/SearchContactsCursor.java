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

package app.diol.dialer.searchfragment.cp2;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.provider.ContactsContract.Directory;
import android.support.annotation.Nullable;

import app.diol.R;
import app.diol.dialer.searchfragment.common.SearchCursor;

/**
 * {@link SearchCursor} implementation for displaying on device contacts.
 *
 * <p>Inserts header "All Contacts" at position 0.
 */
final class SearchContactsCursor extends MergeCursor implements SearchCursor {

    private final ContactFilterCursor contactFilterCursor;
    private final Context context;

    private SearchContactsCursor(Cursor[] cursors, Context context) {
        super(cursors);
        this.contactFilterCursor = (ContactFilterCursor) cursors[1];
        this.context = context;
    }

    static SearchContactsCursor newInstance(
            Context context, ContactFilterCursor contactFilterCursor) {
        MatrixCursor headerCursor = new MatrixCursor(HEADER_PROJECTION);
        headerCursor.addRow(new String[]{context.getString(R.string.all_contacts)});
        return new SearchContactsCursor(new Cursor[]{headerCursor, contactFilterCursor}, context);
    }

    @Override
    public boolean isHeader() {
        return isFirst();
    }

    @Override
    public boolean updateQuery(@Nullable String query) {
        contactFilterCursor.filter(query, context);
        return true;
    }

    @Override
    public long getDirectoryId() {
        return Directory.DEFAULT;
    }

    @Override
    public int getCount() {
        // If we don't have any contents, we don't want to show the header
        int count = contactFilterCursor.getCount();
        return count == 0 ? 0 : count + 1;
    }
}
