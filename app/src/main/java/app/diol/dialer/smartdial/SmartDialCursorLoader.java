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

package app.diol.dialer.smartdial;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.database.Database;
import app.diol.dialer.database.DialerDatabaseHelper;
import app.diol.dialer.database.DialerDatabaseHelper.ContactNumber;
import app.diol.dialer.smartdial.util.SmartDialNameMatcher;
import app.diol.dialer.util.PermissionsUtil;

/**
 * Implements a Loader<Cursor> class to asynchronously load SmartDial search results.
 */
public class SmartDialCursorLoader extends AsyncTaskLoader<Cursor> {

    private static final String TAG = "SmartDialCursorLoader";
    private static final boolean DEBUG = false;

    private final Context context;

    private Cursor cursor;

    private String query;
    private SmartDialNameMatcher nameMatcher;

    private boolean showEmptyListForNullQuery = true;

    public SmartDialCursorLoader(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Configures the query string to be used to find SmartDial matches.
     *
     * @param query The query string user typed.
     */
    public void configureQuery(String query) {
        if (DEBUG) {
            LogUtil.v(TAG, "Configure new query to be " + query);
        }
        this.query = SmartDialNameMatcher.normalizeNumber(context, query);

        /** Constructs a name matcher object for matching names. */
        nameMatcher = new SmartDialNameMatcher(this.query);
        nameMatcher.setShouldMatchEmptyQuery(!showEmptyListForNullQuery);
    }

    /**
     * Queries the SmartDial database and loads results in background.
     *
     * @return Cursor of contacts that matches the SmartDial query.
     */
    @Override
    public Cursor loadInBackground() {
        if (DEBUG) {
            LogUtil.v(TAG, "Load in background " + query);
        }

        if (!PermissionsUtil.hasContactsReadPermissions(context)) {
            return new MatrixCursor(PhoneQuery.PROJECTION_PRIMARY);
        }

        /** Loads results from the database helper. */
        final DialerDatabaseHelper dialerDatabaseHelper =
                Database.get(context).getDatabaseHelper(context);
        final ArrayList<ContactNumber> allMatches =
                dialerDatabaseHelper.getLooseMatches(query, nameMatcher);

        if (DEBUG) {
            LogUtil.v(TAG, "Loaded matches " + allMatches.size());
        }

        /** Constructs a cursor for the returned array of results. */
        final MatrixCursor cursor = new MatrixCursor(PhoneQuery.PROJECTION_PRIMARY);
        Object[] row = new Object[PhoneQuery.PROJECTION_PRIMARY.length];
        for (ContactNumber contact : allMatches) {
            row[PhoneQuery.PHONE_ID] = contact.dataId;
            row[PhoneQuery.PHONE_NUMBER] = contact.phoneNumber;
            row[PhoneQuery.CONTACT_ID] = contact.id;
            row[PhoneQuery.LOOKUP_KEY] = contact.lookupKey;
            row[PhoneQuery.PHOTO_ID] = contact.photoId;
            row[PhoneQuery.DISPLAY_NAME] = contact.displayName;
            row[PhoneQuery.CARRIER_PRESENCE] = contact.carrierPresence;
            cursor.addRow(row);
        }
        return cursor;
    }

    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            /** The Loader has been reset; ignore the result and invalidate the data. */
            releaseResources(cursor);
            return;
        }

        /** Hold a reference to the old data so it doesn't get garbage collected. */
        Cursor oldCursor = this.cursor;
        this.cursor = cursor;

        if (isStarted()) {
            /** If the Loader is in a started state, deliver the results to the client. */
            super.deliverResult(cursor);
        }

        /** Invalidate the old data as we don't need it any more. */
        if (oldCursor != null && oldCursor != cursor) {
            releaseResources(oldCursor);
        }
    }

    @Override
    protected void onStartLoading() {
        if (cursor != null) {
            /** Deliver any previously loaded data immediately. */
            deliverResult(cursor);
        }
        if (cursor == null) {
            /** Force loads every time as our results change with queries. */
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        /** The Loader is in a stopped state, so we should attempt to cancel the current load. */
        cancelLoad();
    }

    @Override
    protected void onReset() {
        /** Ensure the loader has been stopped. */
        onStopLoading();

        /** Release all previously saved query results. */
        if (cursor != null) {
            releaseResources(cursor);
            cursor = null;
        }
    }

    @Override
    public void onCanceled(Cursor cursor) {
        super.onCanceled(cursor);

        /** The load has been canceled, so we should release the resources associated with 'data'. */
        releaseResources(cursor);
    }

    private void releaseResources(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public void setShowEmptyListForNullQuery(boolean show) {
        showEmptyListForNullQuery = show;
        if (nameMatcher != null) {
            nameMatcher.setShouldMatchEmptyQuery(!show);
        }
    }

    /**
     * Moved from contacts/common, contains all of the projections needed for Smart Dial queries.
     */
    public static class PhoneQuery {

        public static final String[] PROJECTION_PRIMARY_INTERNAL =
                new String[]{
                        Phone._ID, // 0
                        Phone.TYPE, // 1
                        Phone.LABEL, // 2
                        Phone.NUMBER, // 3
                        Phone.CONTACT_ID, // 4
                        Phone.LOOKUP_KEY, // 5
                        Phone.PHOTO_ID, // 6
                        Phone.DISPLAY_NAME_PRIMARY, // 7
                        Phone.PHOTO_THUMBNAIL_URI, // 8
                };

        public static final String[] PROJECTION_PRIMARY;
        public static final String[] PROJECTION_ALTERNATIVE_INTERNAL =
                new String[]{
                        Phone._ID, // 0
                        Phone.TYPE, // 1
                        Phone.LABEL, // 2
                        Phone.NUMBER, // 3
                        Phone.CONTACT_ID, // 4
                        Phone.LOOKUP_KEY, // 5
                        Phone.PHOTO_ID, // 6
                        Phone.DISPLAY_NAME_ALTERNATIVE, // 7
                        Phone.PHOTO_THUMBNAIL_URI, // 8
                };
        public static final String[] PROJECTION_ALTERNATIVE;
        public static final int PHONE_ID = 0;
        public static final int PHONE_TYPE = 1;
        public static final int PHONE_LABEL = 2;
        public static final int PHONE_NUMBER = 3;
        public static final int CONTACT_ID = 4;
        public static final int LOOKUP_KEY = 5;
        public static final int PHOTO_ID = 6;
        public static final int DISPLAY_NAME = 7;
        public static final int PHOTO_URI = 8;
        public static final int CARRIER_PRESENCE = 9;

        static {
            final List<String> projectionList =
                    new ArrayList<>(Arrays.asList(PROJECTION_PRIMARY_INTERNAL));
            projectionList.add(Phone.CARRIER_PRESENCE); // 9
            PROJECTION_PRIMARY = projectionList.toArray(new String[projectionList.size()]);
        }

        static {
            final List<String> projectionList =
                    new ArrayList<>(Arrays.asList(PROJECTION_ALTERNATIVE_INTERNAL));
            projectionList.add(Phone.CARRIER_PRESENCE); // 9
            PROJECTION_ALTERNATIVE = projectionList.toArray(new String[projectionList.size()]);
        }
    }
}
