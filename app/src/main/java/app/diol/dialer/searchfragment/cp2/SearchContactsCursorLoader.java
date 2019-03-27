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
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Directory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import app.diol.R;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.contacts.ContactsComponent;
import app.diol.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder;
import app.diol.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder;
import app.diol.dialer.searchfragment.common.Projections;
import app.diol.dialer.searchfragment.common.SearchCursor;
import app.diol.dialer.smartdial.SmartDialCursorLoader;
import app.diol.dialer.util.PermissionsUtil;

/**
 * Cursor Loader for CP2 contacts.
 */
public final class SearchContactsCursorLoader extends CursorLoader {

    private final String query;
    private final boolean isRegularSearch;

    /**
     * @param query Contacts cursor will be filtered based on this query.
     */
    public SearchContactsCursorLoader(
            Context context, @Nullable String query, boolean isRegularSearch) {
        super(
                context,
                buildUri(query),
                getProjection(context),
                getWhere(context),
                null,
                getSortKey(context) + " ASC");
        this.query = TextUtils.isEmpty(query) ? "" : query;
        this.isRegularSearch = isRegularSearch;
    }

    private static String[] getProjection(Context context) {
        boolean displayOrderPrimary =
                (ContactsComponent.get(context).contactDisplayPreferences().getDisplayOrder()
                        == DisplayOrder.PRIMARY);
        return displayOrderPrimary
                ? Projections.CP2_PROJECTION
                : Projections.CP2_PROJECTION_ALTERNATIVE;
    }

    private static String getWhere(Context context) {
        String where = getProjection(context)[Projections.DISPLAY_NAME] + " IS NOT NULL";
        where += " AND " + Phone.NUMBER + " IS NOT NULL";
        return where;
    }

    private static String getSortKey(Context context) {
        boolean sortOrderPrimary =
                (ContactsComponent.get(context).contactDisplayPreferences().getSortOrder()
                        == SortOrder.BY_PRIMARY);
        return sortOrderPrimary ? Phone.SORT_KEY_PRIMARY : Phone.SORT_KEY_ALTERNATIVE;
    }

    private static Uri buildUri(String query) {
        return Phone.CONTENT_FILTER_URI.buildUpon().appendPath(query).build();
    }

    @Override
    public Cursor loadInBackground() {
        if (!PermissionsUtil.hasContactsReadPermissions(getContext())) {
            LogUtil.i("SearchContactsCursorLoader.loadInBackground", "Contacts permission denied.");
            return null;
        }
        return isRegularSearch ? regularSearchLoadInBackground() : dialpadSearchLoadInBackground();
    }

    private Cursor regularSearchLoadInBackground() {
        return RegularSearchCursor.newInstance(getContext(), super.loadInBackground());
    }

    private Cursor dialpadSearchLoadInBackground() {
        SmartDialCursorLoader loader = new SmartDialCursorLoader(getContext());
        loader.configureQuery(query);
        Cursor cursor = loader.loadInBackground();
        return SmartDialCursor.newInstance(getContext(), cursor);
    }

    static class SmartDialCursor extends MergeCursor implements SearchCursor {

        private SmartDialCursor(Cursor[] cursors) {
            super(cursors);
        }

        static SmartDialCursor newInstance(Context context, Cursor smartDialCursor) {
            if (smartDialCursor == null || smartDialCursor.getCount() == 0) {
                LogUtil.i("SmartDialCursor.newInstance", "Cursor was null or empty");
                return new SmartDialCursor(new Cursor[]{new MatrixCursor(Projections.CP2_PROJECTION)});
            }

            MatrixCursor headerCursor = new MatrixCursor(HEADER_PROJECTION);
            headerCursor.addRow(new String[]{context.getString(R.string.all_contacts)});
            return new SmartDialCursor(
                    new Cursor[]{headerCursor, convertSmartDialCursorToSearchCursor(smartDialCursor)});
        }

        private static MatrixCursor convertSmartDialCursorToSearchCursor(Cursor smartDialCursor) {
            MatrixCursor cursor = new MatrixCursor(Projections.CP2_PROJECTION);
            if (!smartDialCursor.moveToFirst()) {
                return cursor;
            }

            do {
                Object[] newRow = new Object[Projections.CP2_PROJECTION.length];
                for (int i = 0; i < Projections.CP2_PROJECTION.length; i++) {
                    String column = Projections.CP2_PROJECTION[i];
                    int index = smartDialCursor.getColumnIndex(column);
                    if (index != -1) {
                        switch (smartDialCursor.getType(index)) {
                            case FIELD_TYPE_INTEGER:
                                newRow[i] = smartDialCursor.getInt(index);
                                break;
                            case FIELD_TYPE_STRING:
                                newRow[i] = smartDialCursor.getString(index);
                                break;
                            case FIELD_TYPE_FLOAT:
                                newRow[i] = smartDialCursor.getFloat(index);
                                break;
                            case FIELD_TYPE_BLOB:
                                newRow[i] = smartDialCursor.getBlob(index);
                                break;
                            case FIELD_TYPE_NULL:
                            default:
                                // No-op
                                break;
                        }
                    }
                }
                cursor.addRow(newRow);
            } while (smartDialCursor.moveToNext());
            return cursor;
        }

        @Override
        public boolean isHeader() {
            return isFirst();
        }

        @Override
        public boolean updateQuery(@Nullable String query) {
            return false;
        }

        @Override
        public long getDirectoryId() {
            return Directory.DEFAULT;
        }
    }

    static class RegularSearchCursor extends MergeCursor implements SearchCursor {

        public RegularSearchCursor(Cursor[] cursors) {
            super(cursors);
        }

        static RegularSearchCursor newInstance(Context context, Cursor regularSearchCursor) {
            if (regularSearchCursor == null || regularSearchCursor.getCount() == 0) {
                LogUtil.i("RegularSearchCursor.newInstance", "Cursor was null or empty");
                return new RegularSearchCursor(new Cursor[]{new MatrixCursor(Projections.CP2_PROJECTION)});
            }

            MatrixCursor headerCursor = new MatrixCursor(HEADER_PROJECTION);
            headerCursor.addRow(new String[]{context.getString(R.string.all_contacts)});
            return new RegularSearchCursor(new Cursor[]{headerCursor, regularSearchCursor});
        }

        @Override
        public boolean isHeader() {
            return isFirst();
        }

        @Override
        public boolean updateQuery(@NonNull String query) {
            return false; // no-op
        }

        @Override
        public long getDirectoryId() {
            return 0; // no-op
        }
    }
}
