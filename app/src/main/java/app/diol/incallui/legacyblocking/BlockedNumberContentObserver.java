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

package app.diol.incallui.legacyblocking;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.CallLog;
import android.support.annotation.NonNull;

import java.util.Objects;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.AsyncTaskExecutor;
import app.diol.dialer.common.concurrent.AsyncTaskExecutors;
import app.diol.dialer.util.PermissionsUtil;

/**
 * Observes the {@link CallLog} to delete the CallLog entry for a blocked call after it is added.
 * Automatically de-registers itself {@link #TIMEOUT_MS} ms after registration or if the entry is
 * found and deleted.
 */
public class BlockedNumberContentObserver extends ContentObserver
        implements DeleteBlockedCallTask.Listener {

    /**
     * The time after which a {@link BlockedNumberContentObserver} will be automatically unregistered.
     */
    public static final int TIMEOUT_MS = 5000;

    @NonNull
    private final Context context;
    @NonNull
    private final Handler handler;
    private final String number;
    private final long timeAddedMillis;
    private final Runnable timeoutRunnable =
            new Runnable() {
                @Override
                public void run() {
                    unregister();
                }
            };

    private final AsyncTaskExecutor asyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();

    /**
     * Creates the BlockedNumberContentObserver to delete the new {@link CallLog} entry from the given
     * blocked number.
     *
     * @param number          The blocked number.
     * @param timeAddedMillis The time at which the call from the blocked number was placed.
     */
    public BlockedNumberContentObserver(
            @NonNull Context context, @NonNull Handler handler, String number, long timeAddedMillis) {
        super(handler);
        this.context = Objects.requireNonNull(context, "context").getApplicationContext();
        this.handler = Objects.requireNonNull(handler);
        this.number = number;
        this.timeAddedMillis = timeAddedMillis;
    }

    @Override
    public void onChange(boolean selfChange) {
        LogUtil.i(
                "BlockedNumberContentObserver.onChange",
                "attempting to remove call log entry from blocked number");
        asyncTaskExecutor.submit(
                DeleteBlockedCallTask.IDENTIFIER,
                new DeleteBlockedCallTask(context, this, number, timeAddedMillis));
    }

    @Override
    public void onDeleteBlockedCallTaskComplete(boolean didFindEntry) {
        if (didFindEntry) {
            unregister();
        }
    }

    /**
     * Registers this {@link ContentObserver} to listen for changes to the {@link CallLog}. If the
     * CallLog entry is not found before {@link #TIMEOUT_MS}, this ContentObserver automatically
     * un-registers itself.
     */
    public void register() {
        LogUtil.i("BlockedNumberContentObserver.register", null);
        if (PermissionsUtil.hasCallLogReadPermissions(context)
                && PermissionsUtil.hasCallLogWritePermissions(context)) {
            context.getContentResolver().registerContentObserver(CallLog.CONTENT_URI, true, this);
            handler.postDelayed(timeoutRunnable, TIMEOUT_MS);
        } else {
            LogUtil.w("BlockedNumberContentObserver.register", "no call log read/write permissions.");
        }
    }

    private void unregister() {
        LogUtil.i("BlockedNumberContentObserver.unregister", null);
        handler.removeCallbacks(timeoutRunnable);
        context.getContentResolver().unregisterContentObserver(this);
    }
}
