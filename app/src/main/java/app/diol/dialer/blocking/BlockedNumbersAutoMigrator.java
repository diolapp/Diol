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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.UserManagerCompat;

import app.diol.dialer.blocking.FilteredNumberAsyncQueryHandler.OnHasBlockedNumbersListener;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorFactory;

/**
 * Class responsible for checking if the user can be auto-migrated to {@link
 * android.provider.BlockedNumberContract} blocking. In order for this to happen, the user cannot
 * have any numbers that are blocked in the Dialer solution.
 */
@Deprecated
public class BlockedNumbersAutoMigrator {

    static final String HAS_CHECKED_AUTO_MIGRATE_KEY = "checkedAutoMigrate";

    @NonNull
    private final Context appContext;
    @NonNull
    private final FilteredNumberAsyncQueryHandler queryHandler;
    @NonNull
    private final DialerExecutorFactory dialerExecutorFactory;

    /**
     * Constructs the BlockedNumbersAutoMigrator with the given {@link
     * FilteredNumberAsyncQueryHandler}.
     *
     * @param queryHandler The FilteredNumberAsyncQueryHandler used to determine if there are blocked
     *                     numbers.
     * @throws NullPointerException if sharedPreferences or queryHandler are null.
     */
    public BlockedNumbersAutoMigrator(
            @NonNull Context appContext,
            @NonNull FilteredNumberAsyncQueryHandler queryHandler,
            @NonNull DialerExecutorFactory dialerExecutorFactory) {
        this.appContext = Assert.isNotNull(appContext);
        this.queryHandler = Assert.isNotNull(queryHandler);
        this.dialerExecutorFactory = Assert.isNotNull(dialerExecutorFactory);
    }

    public void asyncAutoMigrate() {
        dialerExecutorFactory
                .createNonUiTaskBuilder(new ShouldAttemptAutoMigrate(appContext))
                .onSuccess(this::autoMigrate)
                .build()
                .executeParallel(null);
    }

    /**
     * Attempts to perform the auto-migration. Auto-migration will only be attempted once and can be
     * performed only when the user has no blocked numbers. As a result of this method, the user will
     * be migrated to the framework blocking solution if blocked numbers don't exist.
     */
    private void autoMigrate(boolean shouldAttemptAutoMigrate) {
        if (!shouldAttemptAutoMigrate) {
            return;
        }

        LogUtil.i("BlockedNumbersAutoMigrator", "attempting to auto-migrate.");
        queryHandler.hasBlockedNumbers(
                new OnHasBlockedNumbersListener() {
                    @Override
                    public void onHasBlockedNumbers(boolean hasBlockedNumbers) {
                        if (hasBlockedNumbers) {
                            LogUtil.i("BlockedNumbersAutoMigrator", "not auto-migrating: blocked numbers exist.");
                            return;
                        }
                        LogUtil.i("BlockedNumbersAutoMigrator", "auto-migrating: no blocked numbers.");
                        FilteredNumberCompat.setHasMigratedToNewBlocking(appContext, true);
                    }
                });
    }

    private static class ShouldAttemptAutoMigrate implements Worker<Void, Boolean> {
        private final Context appContext;

        ShouldAttemptAutoMigrate(Context appContext) {
            this.appContext = appContext;
        }

        @Nullable
        @Override
        public Boolean doInBackground(@Nullable Void input) {
            if (!UserManagerCompat.isUserUnlocked(appContext)) {
                LogUtil.i("BlockedNumbersAutoMigrator", "not attempting auto-migrate: device is locked");
                return false;
            }
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(appContext);

            if (sharedPreferences.contains(HAS_CHECKED_AUTO_MIGRATE_KEY)) {
                LogUtil.v(
                        "BlockedNumbersAutoMigrator", "not attempting auto-migrate: already checked once.");
                return false;
            }

            if (!FilteredNumberCompat.canAttemptBlockOperations(appContext)) {
                // This may be the case where the user is on the lock screen, so we shouldn't record that
                // the migration status was checked.
                LogUtil.i(
                        "BlockedNumbersAutoMigrator", "not attempting auto-migrate: current user can't block");
                return false;
            }
            LogUtil.i(
                    "BlockedNumbersAutoMigrator", "updating state as already checked for auto-migrate.");
            sharedPreferences.edit().putBoolean(HAS_CHECKED_AUTO_MIGRATE_KEY, true).apply();

            if (!FilteredNumberCompat.canUseNewFiltering()) {
                LogUtil.i("BlockedNumbersAutoMigrator", "not attempting auto-migrate: not available.");
                return false;
            }

            if (FilteredNumberCompat.hasMigratedToNewBlocking(appContext)) {
                LogUtil.i("BlockedNumbersAutoMigrator", "not attempting auto-migrate: already migrated.");
                return false;
            }
            return true;
        }
    }
}
