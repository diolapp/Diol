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

package app.diol.dialer.phonenumbercache;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.InputStream;

import app.diol.dialer.logging.ContactSource;

public interface CachedNumberLookupService {

    CachedContactInfo buildCachedContactInfo(ContactInfo info);

    /**
     * Perform a lookup using the cached number lookup service to return contact information stored in
     * the cache that corresponds to the given number.
     *
     * @param context Valid context
     * @param number  Phone number to lookup the cache for
     * @return A {@link CachedContactInfo} containing the contact information if the phone number is
     * found in the cache, {@link ContactInfo#EMPTY} if the phone number was not found in the
     * cache, and null if there was an error when querying the cache.
     */
    @WorkerThread
    CachedContactInfo lookupCachedContactFromNumber(Context context, String number);

    void addContact(Context context, CachedContactInfo info);

    boolean isBusiness(ContactSource.Type sourceType);

    boolean canReportAsInvalid(ContactSource.Type sourceType, String objectId);

    boolean reportAsInvalid(Context context, CachedContactInfo cachedContactInfo);

    /**
     * @return return {@link Uri} to the photo or return {@code null} when failing to add photo
     */
    @Nullable
    Uri addPhoto(Context context, String number, InputStream in);

    /**
     * Remove all cached phone number entries from the cache, regardless of how old they are.
     *
     * @param context Valid context
     */
    void clearAllCacheEntries(Context context);

    interface CachedContactInfo {

        @NonNull
        ContactInfo getContactInfo();

        void setSource(ContactSource.Type sourceType, String name, long directoryId);

        void setDirectorySource(String name, long directoryId);

        void setLookupKey(String lookupKey);
    }
}
