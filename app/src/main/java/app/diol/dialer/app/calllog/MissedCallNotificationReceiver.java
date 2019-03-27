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

package app.diol.dialer.app.calllog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Receives broadcasts that should trigger a refresh of the missed call notification. This includes
 * both an explicit broadcast from Telecom and a reboot.
 */
public class MissedCallNotificationReceiver extends BroadcastReceiver {

    // TODO: Use compat class for these methods.
    public static final String ACTION_SHOW_MISSED_CALLS_NOTIFICATION =
            "android.telecom.action.SHOW_MISSED_CALLS_NOTIFICATION";

    public static final String EXTRA_NOTIFICATION_COUNT = "android.telecom.extra.NOTIFICATION_COUNT";

    public static final String EXTRA_NOTIFICATION_PHONE_NUMBER =
            "android.telecom.extra.NOTIFICATION_PHONE_NUMBER";

    private static void updateBadgeCount(Context context, int count) {
        boolean success = ShortcutBadger.applyCount(context, count);
        LogUtil.i(
                "MissedCallNotificationReceiver.updateBadgeCount",
                "update badge count: %d success: %b",
                count,
                success);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!ACTION_SHOW_MISSED_CALLS_NOTIFICATION.equals(action)) {
            return;
        }

        LogUtil.enterBlock("MissedCallNotificationReceiver.onReceive");

        int count =
                intent.getIntExtra(
                        EXTRA_NOTIFICATION_COUNT, CallLogNotificationsService.UNKNOWN_MISSED_CALL_COUNT);
        String phoneNumber = intent.getStringExtra(EXTRA_NOTIFICATION_PHONE_NUMBER);

        PendingResult pendingResult = goAsync();

        DialerExecutorComponent.get(context)
                .dialerExecutorFactory()
                .createNonUiTaskBuilder(MissedCallNotifier.getInstance(context))
                .onSuccess(
                        output -> {
                            LogUtil.i(
                                    "MissedCallNotificationReceiver.onReceive",
                                    "update missed call notifications successful");
                            updateBadgeCount(context, count);
                            pendingResult.finish();
                        })
                .onFailure(
                        throwable -> {
                            LogUtil.i(
                                    "MissedCallNotificationReceiver.onReceive",
                                    "update missed call notifications failed");
                            pendingResult.finish();
                        })
                .build()
                .executeParallel(new Pair<>(count, phoneNumber));
    }
}
