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

package app.diol.contacts.common.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

/**
 * Utility methods for the {@link ContactLoader}.
 */
public final class ContactLoaderUtils {

    /**
     * Static helper, not instantiable.
     */
    private ContactLoaderUtils() {
    }

    /**
     * Transforms the given Uri and returns a Lookup-Uri that represents the contact. For legacy
     * contacts, a raw-contact lookup is performed. An {@link IllegalArgumentException} can be thrown
     * if the URI is null or the authority is not recognized.
     *
     * <p>Do not call from the UI thread.
     */
    @SuppressWarnings("deprecation")
    public static Uri ensureIsContactUri(final ContentResolver resolver, final Uri uri)
            throws IllegalArgumentException {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }

        final String authority = uri.getAuthority();

        // Current Style Uri?
        if (ContactsContract.AUTHORITY.equals(authority)) {
            final String type = resolver.getType(uri);
            // Contact-Uri? Good, return it
            if (ContactsContract.Contacts.CONTENT_ITEM_TYPE.equals(type)) {
                return uri;
            }

            // RawContact-Uri? Transform it to ContactUri
            if (RawContacts.CONTENT_ITEM_TYPE.equals(type)) {
                final long rawContactId = ContentUris.parseId(uri);
                return RawContacts.getContactLookupUri(
                        resolver, ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
            }

            // Anything else? We don't know what this is
            throw new IllegalArgumentException("uri format is unknown");
        }

        // Legacy Style? Convert to RawContact
        final String OBSOLETE_AUTHORITY = Contacts.AUTHORITY;
        if (OBSOLETE_AUTHORITY.equals(authority)) {
            // Legacy Format. Convert to RawContact-Uri and then lookup the contact
            final long rawContactId = ContentUris.parseId(uri);
            return RawContacts.getContactLookupUri(
                    resolver, ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
        }

        throw new IllegalArgumentException("uri authority is unknown");
    }
}
