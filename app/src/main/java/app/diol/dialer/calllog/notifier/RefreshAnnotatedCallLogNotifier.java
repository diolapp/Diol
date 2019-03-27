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

package app.diol.dialer.calllog.notifier;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.diol.dialer.calllog.constants.IntentNames;
import app.diol.dialer.calllog.constants.SharedPrefKeys;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.storage.Unencrypted;

/**
 * Notifies that a refresh of the annotated call log needs to be started/cancelled.
 *
 * <p>Methods in this class are usually invoked when the underlying data backing the annotated call
 * log change.
 *
 * <p>For example, a {@link android.database.ContentObserver} for the system call log can use {@link
 * #markDirtyAndNotify()} to force the annotated call log to be rebuilt.
 */
@Singleton
public class RefreshAnnotatedCallLogNotifier {

    private final Context appContext;
    private final SharedPreferences sharedPreferences;

    @Inject
    RefreshAnnotatedCallLogNotifier(
            @ApplicationContext Context appContext, @Unencrypted SharedPreferences sharedPreferences) {
        this.appContext = appContext;
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Mark the annotated call log as "dirty" and notify that it needs to be refreshed.
     *
     * <p>This will force a rebuild by skip checking whether the annotated call log is "dirty".
     */
    public void markDirtyAndNotify() {
        LogUtil.enterBlock("RefreshAnnotatedCallLogNotifier.markDirtyAndNotify");

        sharedPreferences.edit().putBoolean(SharedPrefKeys.FORCE_REBUILD, true).apply();
        notify(/* checkDirty = */ false);
    }

    /**
     * Notifies that the annotated call log needs to be refreshed.
     *
     * <p>Note that the notification is sent as a broadcast, which means the annotated call log might
     * not be refreshed if there is no corresponding receiver registered.
     *
     * @param checkDirty Whether to check if the annotated call log is "dirty" before proceeding to
     *                   rebuild it.
     */
    public void notify(boolean checkDirty) {
        LogUtil.i("RefreshAnnotatedCallLogNotifier.notify", "checkDirty = %s", checkDirty);

        Intent intent = new Intent();
        intent.setAction(IntentNames.ACTION_REFRESH_ANNOTATED_CALL_LOG);
        intent.putExtra(IntentNames.EXTRA_CHECK_DIRTY, checkDirty);

        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
    }

    /**
     * Notifies to cancel refreshing the annotated call log.
     *
     * <p>Note that this method does not guarantee the job to be cancelled. As the notification is
     * sent as a broadcast, please see the corresponding receiver for details about cancelling the
     * job.
     */
    public void cancel() {
        LogUtil.enterBlock("RefreshAnnotatedCallLogNotifier.cancel");

        Intent intent = new Intent();
        intent.setAction(IntentNames.ACTION_CANCEL_REFRESHING_ANNOTATED_CALL_LOG);

        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
    }
}
