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

package app.diol.dialer.shortcuts;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.AsyncTaskExecutor;
import app.diol.dialer.common.concurrent.AsyncTaskExecutors;

/**
 * Reports outgoing calls as shortcut usage.
 *
 * <p>Note that all outgoing calls are considered shortcut usage, no matter where they are initiated
 * from (i.e. from anywhere in the dialer app, or even from other apps).
 *
 * <p>This allows launcher applications to provide users with shortcut suggestions, even if the user
 * isn't already using shortcuts.
 */
@TargetApi(VERSION_CODES.N_MR1) // Shortcuts introduced in N_MR1
public class ShortcutUsageReporter {

    private static final AsyncTaskExecutor EXECUTOR = AsyncTaskExecutors.createThreadPoolExecutor();

    /**
     * Called when an outgoing call is added to the call list in order to report outgoing calls as
     * shortcut usage. This should be called exactly once for each outgoing call.
     *
     * <p>Asynchronously queries the contacts database for the contact's lookup key which corresponds
     * to the provided phone number, and uses that to report shortcut usage.
     *
     * @param context     used to access ShortcutManager system service
     * @param phoneNumber the phone number being called
     */
    @MainThread
    public static void onOutgoingCallAdded(@NonNull Context context, @Nullable String phoneNumber) {
        Assert.isMainThread();
        Assert.isNotNull(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 || TextUtils.isEmpty(phoneNumber)) {
            return;
        }

        EXECUTOR.submit(Task.ID, new Task(context), phoneNumber);
    }

    private static final class Task extends AsyncTask<String, Void, Void> {
        private static final String ID = "ShortcutUsageReporter.Task";

        private final Context context;

        public Task(Context context) {
            this.context = context;
        }

        /**
         * @param phoneNumbers array with exactly one non-empty phone number
         */
        @Override
        @WorkerThread
        protected Void doInBackground(@NonNull String... phoneNumbers) {
            Assert.isWorkerThread();

            String lookupKey = queryForLookupKey(phoneNumbers[0]);
            if (!TextUtils.isEmpty(lookupKey)) {
                LogUtil.i("ShortcutUsageReporter.backgroundLogUsage", "%s", lookupKey);
                ShortcutManager shortcutManager =
                        (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);

                // Note: There may not currently exist a shortcut with the provided key, but it is logged
                // anyway, so that launcher applications at least have the information should the shortcut
                // be created in the future.
                shortcutManager.reportShortcutUsed(lookupKey);
            }
            return null;
        }

        @Nullable
        @WorkerThread
        private String queryForLookupKey(String phoneNumber) {
            Assert.isWorkerThread();

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                LogUtil.i("ShortcutUsageReporter.queryForLookupKey", "No contact permissions");
                return null;
            }

            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            try (Cursor cursor =
                         context
                                 .getContentResolver()
                                 .query(uri, new String[]{Contacts.LOOKUP_KEY}, null, null, null)) {

                if (cursor == null || !cursor.moveToNext()) {
                    return null; // No contact for dialed number
                }
                // Arbitrarily use first result.
                return cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
            }
        }
    }
}
