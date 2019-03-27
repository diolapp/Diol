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

package app.diol.dialer.blocking;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BlockedNumberContract.BlockedNumbers;

import java.util.Objects;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.database.FilteredNumberContract;
import app.diol.dialer.database.FilteredNumberContract.FilteredNumber;
import app.diol.dialer.database.FilteredNumberContract.FilteredNumberColumns;

/**
 * Class which should be used to migrate numbers from {@link FilteredNumberContract} blocking to
 * {@link android.provider.BlockedNumberContract} blocking.
 */
@Deprecated
public class BlockedNumbersMigrator {

    private final Context context;

    /**
     * Creates a new BlockedNumbersMigrate, using the given {@link ContentResolver} to perform queries
     * against the blocked numbers tables.
     */
    public BlockedNumbersMigrator(Context context) {
        this.context = Objects.requireNonNull(context);
    }

    private static boolean migrateToNewBlockingInBackground(ContentResolver resolver) {
        try (Cursor cursor =
                     resolver.query(
                             FilteredNumber.CONTENT_URI,
                             new String[]{FilteredNumberColumns.NUMBER},
                             null,
                             null,
                             null)) {
            if (cursor == null) {
                LogUtil.i(
                        "BlockedNumbersMigrator.migrateToNewBlockingInBackground", "migrate - cursor was null");
                return false;
            }

            LogUtil.i(
                    "BlockedNumbersMigrator.migrateToNewBlockingInBackground",
                    "migrate - attempting to migrate " + cursor.getCount() + "numbers");

            int numMigrated = 0;
            while (cursor.moveToNext()) {
                String originalNumber =
                        cursor.getString(cursor.getColumnIndex(FilteredNumberColumns.NUMBER));
                if (isNumberInNewBlocking(resolver, originalNumber)) {
                    LogUtil.i(
                            "BlockedNumbersMigrator.migrateToNewBlockingInBackground",
                            "migrate - number was already blocked in new blocking");
                    continue;
                }
                ContentValues values = new ContentValues();
                values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, originalNumber);
                resolver.insert(BlockedNumbers.CONTENT_URI, values);
                ++numMigrated;
            }
            LogUtil.i(
                    "BlockedNumbersMigrator.migrateToNewBlockingInBackground",
                    "migrate - migration complete. " + numMigrated + " numbers migrated.");
            return true;
        }
    }

    private static boolean isNumberInNewBlocking(ContentResolver resolver, String originalNumber) {
        try (Cursor cursor =
                     resolver.query(
                             BlockedNumbers.CONTENT_URI,
                             new String[]{BlockedNumbers.COLUMN_ID},
                             BlockedNumbers.COLUMN_ORIGINAL_NUMBER + " = ?",
                             new String[]{originalNumber},
                             null)) {
            return cursor != null && cursor.getCount() != 0;
        }
    }

    /**
     * Copies all of the numbers in the {@link FilteredNumberContract} block list to the {@link
     * android.provider.BlockedNumberContract} block list.
     *
     * @param listener {@link Listener} called once the migration is complete.
     * @return {@code true} if the migrate can be attempted, {@code false} otherwise.
     * @throws NullPointerException if listener is null
     */
    public boolean migrate(final Listener listener) {
        LogUtil.i("BlockedNumbersMigrator.migrate", "migrate - start");
        if (!FilteredNumberCompat.canUseNewFiltering()) {
            LogUtil.i("BlockedNumbersMigrator.migrate", "migrate - can't use new filtering");
            return false;
        }
        Objects.requireNonNull(listener);
        new MigratorTask(listener).execute();
        return true;
    }

    /**
     * Listener for the operation to migrate from {@link FilteredNumberContract} blocking to {@link
     * android.provider.BlockedNumberContract} blocking.
     */
    public interface Listener {

        /**
         * Called when the migration operation is finished.
         */
        void onComplete();
    }

    private class MigratorTask extends AsyncTask<Void, Void, Boolean> {

        private final Listener listener;

        public MigratorTask(Listener listener) {
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            LogUtil.i("BlockedNumbersMigrator.doInBackground", "migrate - start background migration");
            return migrateToNewBlockingInBackground(context.getContentResolver());
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            LogUtil.i("BlockedNumbersMigrator.onPostExecute", "migrate - marking migration complete");
            FilteredNumberCompat.setHasMigratedToNewBlocking(context, isSuccessful);
            LogUtil.i("BlockedNumbersMigrator.onPostExecute", "migrate - calling listener");
            listener.onComplete();
        }
    }
}
