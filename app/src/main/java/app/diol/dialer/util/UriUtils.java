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

package app.diol.dialer.util;

import android.net.Uri;
import android.provider.ContactsContract;

import java.util.List;

/**
 * Utility methods for dealing with URIs.
 */
public class UriUtils {

    private static final String LOOKUP_URI_ENCODED = "encoded";

    /**
     * Static helper, not instantiable.
     */
    private UriUtils() {
    }

    /**
     * Checks whether two URI are equal, taking care of the case where either is null.
     */
    public static boolean areEqual(Uri uri1, Uri uri2) {
        if (uri1 == null && uri2 == null) {
            return true;
        }
        if (uri1 == null || uri2 == null) {
            return false;
        }
        return uri1.equals(uri2);
    }

    /**
     * Parses a string into a URI and returns null if the given string is null.
     */
    public static Uri parseUriOrNull(String uriString) {
        if (uriString == null) {
            return null;
        }
        return Uri.parse(uriString);
    }

    /**
     * Converts a URI into a string, returns null if the given URI is null.
     */
    public static String uriToString(Uri uri) {
        return uri == null ? null : uri.toString();
    }

    public static boolean isEncodedContactUri(Uri uri) {
        if (uri == null) {
            return false;
        }
        final String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment == null) {
            return false;
        }
        return lastPathSegment.equals(LOOKUP_URI_ENCODED);
    }

    /**
     * @return {@code uri} as-is if the authority is of contacts provider. Otherwise or {@code uri} is
     * null, return null otherwise
     */
    public static Uri nullForNonContactsUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        return ContactsContract.AUTHORITY.equals(uri.getAuthority()) ? uri : null;
    }

    /**
     * Parses the given URI to determine the original lookup key of the contact.
     */
    public static String getLookupKeyFromUri(Uri lookupUri) {
        // Would be nice to be able to persist the lookup key somehow to avoid having to parse
        // the uri entirely just to retrieve the lookup key, but every uri is already parsed
        // once anyway to check if it is an encoded JSON uri, so this has negligible effect
        // on performance.
        if (lookupUri != null && !UriUtils.isEncodedContactUri(lookupUri)) {
            final List<String> segments = lookupUri.getPathSegments();
            // This returns the third path segment of the uri, where the lookup key is located.
            // See {@link android.provider.ContactsContract.Contacts#CONTENT_LOOKUP_URI}.
            return (segments.size() < 3) ? null : Uri.encode(segments.get(2));
        } else {
            return null;
        }
    }
}
