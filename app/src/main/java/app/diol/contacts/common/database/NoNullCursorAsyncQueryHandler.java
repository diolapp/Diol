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

package app.diol.contacts.common.database;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@AsyncQueryHandler} that will never return a null cursor.
 *
 * <p>Instead, will return a {@link Cursor} with 0 records.
 */
public abstract class NoNullCursorAsyncQueryHandler extends AsyncQueryHandler {
    private static final AtomicInteger pendingQueryCount = new AtomicInteger();
    @Nullable
    private static PendingQueryCountChangedListener pendingQueryCountChangedListener;

    public NoNullCursorAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setPendingQueryCountChangedListener(
            @Nullable PendingQueryCountChangedListener listener) {
        pendingQueryCountChangedListener = listener;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static int getPendingQueryCount() {
        return pendingQueryCount.get();
    }

    @Override
    public void startQuery(
            int token,
            Object cookie,
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String orderBy) {
        pendingQueryCount.getAndIncrement();
        if (pendingQueryCountChangedListener != null) {
            pendingQueryCountChangedListener.onPendingQueryCountChanged();
        }

        final CookieWithProjection projectionCookie = new CookieWithProjection(cookie, projection);
        super.startQuery(token, projectionCookie, uri, projection, selection, selectionArgs, orderBy);
    }

    @Override
    protected final void onQueryComplete(int token, Object cookie, Cursor cursor) {
        CookieWithProjection projectionCookie = (CookieWithProjection) cookie;

        super.onQueryComplete(token, projectionCookie.originalCookie, cursor);

        if (cursor == null) {
            cursor = new EmptyCursor(projectionCookie.projection);
        }
        onNotNullableQueryComplete(token, projectionCookie.originalCookie, cursor);

        pendingQueryCount.getAndDecrement();
        if (pendingQueryCountChangedListener != null) {
            pendingQueryCountChangedListener.onPendingQueryCountChanged();
        }
    }

    protected abstract void onNotNullableQueryComplete(int token, Object cookie, Cursor cursor);

    /**
     * Callback to listen for changes in the number of queries that have not completed.
     */
    public interface PendingQueryCountChangedListener {
        void onPendingQueryCountChanged();
    }

    /**
     * Class to add projection to an existing cookie.
     */
    private static class CookieWithProjection {

        public final Object originalCookie;
        public final String[] projection;

        public CookieWithProjection(Object cookie, String[] projection) {
            this.originalCookie = cookie;
            this.projection = projection;
        }
    }
}
