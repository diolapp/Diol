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
package app.diol.dialer.notification.missedcalls;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.notification.DialerNotificationManager;
import app.diol.dialer.notification.NotificationManagerUtils;

/**
 * Cancels missed calls notifications.
 */
public final class MissedCallNotificationCanceller {

    /**
     * Cancels all missed call notifications.
     */
    public static void cancelAll(@NonNull Context context) {
        NotificationManagerUtils.cancelAllInGroup(context, MissedCallConstants.GROUP_KEY);
    }

    /**
     * Cancels a missed call notification for a single call.
     */
    public static void cancelSingle(@NonNull Context context, @Nullable Uri callUri) {
        if (callUri == null) {
            LogUtil.e(
                    "MissedCallNotificationCanceller.cancelSingle",
                    "unable to cancel notification, uri is null");
            return;
        }
        // This will also dismiss the group summary if there are no more missed call notifications.
        DialerNotificationManager.cancel(
                context,
                MissedCallNotificationTags.getNotificationTagForCallUri(callUri),
                MissedCallConstants.NOTIFICATION_ID);
    }
}
