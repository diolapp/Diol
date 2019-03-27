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

package app.diol.dialer.contactsfragment;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;

import app.diol.dialer.contacts.ContactsComponent;

/**
 * Cursor Loader for {@link ContactsFragment}.
 */
final class ContactsCursorLoader extends CursorLoader {

    public static final int CONTACT_ID = 0;
    public static final int CONTACT_DISPLAY_NAME = 1;
    public static final int CONTACT_PHOTO_ID = 2;
    public static final int CONTACT_PHOTO_URI = 3;
    public static final int CONTACT_LOOKUP_KEY = 4;

    public static final String[] CONTACTS_PROJECTION_DISPLAY_NAME_PRIMARY =
            new String[]{
                    Contacts._ID, // 0
                    Contacts.DISPLAY_NAME_PRIMARY, // 1
                    Contacts.PHOTO_ID, // 2
                    Contacts.PHOTO_THUMBNAIL_URI, // 3
                    Contacts.LOOKUP_KEY, // 4
            };

    public static final String[] CONTACTS_PROJECTION_DISPLAY_NAME_ALTERNATIVE =
            new String[]{
                    Contacts._ID, // 0
                    Contacts.DISPLAY_NAME_ALTERNATIVE, // 1
                    Contacts.PHOTO_ID, // 2
                    Contacts.PHOTO_THUMBNAIL_URI, // 3
                    Contacts.LOOKUP_KEY, // 4
            };

    ContactsCursorLoader(Context context, boolean hasPhoneNumbers) {
        super(
                context,
                buildUri(""),
                getProjection(context),
                getWhere(context, hasPhoneNumbers),
                null,
                getSortKey(context) + " ASC");
    }

    private static String[] getProjection(Context context) {
        switch (ContactsComponent.get(context).contactDisplayPreferences().getDisplayOrder()) {
            case PRIMARY:
                return CONTACTS_PROJECTION_DISPLAY_NAME_PRIMARY;
            case ALTERNATIVE:
                return CONTACTS_PROJECTION_DISPLAY_NAME_ALTERNATIVE;
        }
        throw new AssertionError("exhaustive switch");
    }

    private static String getWhere(Context context, boolean hasPhoneNumbers) {
        String where = getProjection(context)[CONTACT_DISPLAY_NAME] + " IS NOT NULL";
        if (hasPhoneNumbers) {
            where += " AND " + Contacts.HAS_PHONE_NUMBER + "=1";
        }
        return where;
    }

    private static String getSortKey(Context context) {

        switch (ContactsComponent.get(context).contactDisplayPreferences().getSortOrder()) {
            case BY_PRIMARY:
                return Contacts.SORT_KEY_PRIMARY;
            case BY_ALTERNATIVE:
                return Contacts.SORT_KEY_ALTERNATIVE;
        }
        throw new AssertionError("exhaustive switch");
    }

    private static Uri buildUri(String query) {
        Uri.Builder baseUri;
        if (TextUtils.isEmpty(query)) {
            baseUri = Contacts.CONTENT_URI.buildUpon();
        } else {
            baseUri = Contacts.CONTENT_FILTER_URI.buildUpon().appendPath(query);
        }
        return baseUri.appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true").build();
    }

    /**
     * Update cursor loader to filter contacts based on the provided query.
     */
    public void setQuery(String query) {
        setUri(buildUri(query));
    }
}
