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

package app.diol.dialer.contacts.hiresphoto;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.support.annotation.VisibleForTesting;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.common.database.Selection;
import app.diol.dialer.inject.ApplicationContext;

/**
 * Use the contacts sync adapter to load high resolution photos for a Google account.
 */
public class HighResolutionPhotoRequesterImpl implements HighResolutionPhotoRequester {

    @VisibleForTesting
    static final ComponentName SYNC_HIGH_RESOLUTION_PHOTO_SERVICE =
            new ComponentName(
                    "com.google.android.syncadapters.contacts",
                    "com.google.android.syncadapters.contacts.SyncHighResPhotoIntentService");
    private final Context appContext;
    private final ListeningExecutorService backgroundExecutor;
    @Inject
    HighResolutionPhotoRequesterImpl(
            @ApplicationContext Context appContext,
            @BackgroundExecutor ListeningExecutorService backgroundExecutor) {
        this.appContext = appContext;
        this.backgroundExecutor = backgroundExecutor;
    }

    @Override
    public ListenableFuture<Void> request(Uri contactUri) {
        return backgroundExecutor.submit(
                () -> {
                    try {
                        requestInternal(contactUri);
                    } catch (RequestFailedException e) {
                        LogUtil.e("HighResolutionPhotoRequesterImpl.request", "request failed", e);
                    }
                    return null;
                });
    }

    private void requestInternal(Uri contactUri) throws RequestFailedException {
        for (Long rawContactId : getGoogleRawContactIds(getContactId(contactUri))) {
            Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setComponent(SYNC_HIGH_RESOLUTION_PHOTO_SERVICE);
            intent.setDataAndType(rawContactUri, RawContacts.CONTENT_ITEM_TYPE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                LogUtil.i(
                        "HighResolutionPhotoRequesterImpl.requestInternal",
                        "requesting photo for " + rawContactUri);
                appContext.startService(intent);
            } catch (IllegalStateException | SecurityException e) {
                throw new RequestFailedException("unable to start sync adapter", e);
            }
        }
    }

    private long getContactId(Uri contactUri) throws RequestFailedException {
        try (Cursor cursor =
                     appContext
                             .getContentResolver()
                             .query(contactUri, new String[]{Contacts._ID}, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                throw new RequestFailedException("cannot get contact ID");
            }
            return cursor.getLong(0);
        }
    }

    private List<Long> getGoogleRawContactIds(long contactId) throws RequestFailedException {
        List<Long> result = new ArrayList<>();
        Selection selection =
                Selection.column(RawContacts.CONTACT_ID)
                        .is("=", contactId)
                        .buildUpon()
                        .and(Selection.column(RawContacts.ACCOUNT_TYPE).is("=", "com.google"))
                        .build();
        try (Cursor cursor =
                     appContext
                             .getContentResolver()
                             .query(
                                     RawContacts.CONTENT_URI,
                                     new String[]{RawContacts._ID, RawContacts.ACCOUNT_TYPE},
                                     selection.getSelection(),
                                     selection.getSelectionArgs(),
                                     null)) {
            if (cursor == null) {
                throw new RequestFailedException("null cursor from raw contact IDs");
            }
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                result.add(cursor.getLong(0));
            }
        }
        return result;
    }

    private static class RequestFailedException extends Exception {
        RequestFailedException(String message) {
            super(message);
        }

        RequestFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
