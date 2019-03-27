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

package app.diol.dialer.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;

/**
 * Utility to ensure that only a certain number of notifications are shown for a particular
 * notification type. Once the limit is reached, older notifications are cancelled.
 */
class NotificationThrottler {
    /**
     * For gropued bundled notifications, the system UI will only display the last 8. For grouped
     * unbundled notifications, the system displays all notifications until a global maximum of 50 is
     * reached.
     */
    private static final int MAX_NOTIFICATIONS_PER_TAG = 8;

    private static final int HIGH_GLOBAL_NOTIFICATION_COUNT = 45;

    private static boolean didLogHighGlobalNotificationCountReached;

    private NotificationThrottler() {
    }

    /**
     * For all the active notifications in the same group as the provided notification, cancel the
     * earliest ones until the left ones is under limit.
     *
     * @param notification the provided notification to determine group
     * @return a set of cancelled notification
     */
    static Set<StatusBarNotification> throttle(
            @NonNull Context context, @NonNull Notification notification) {
        Assert.isNotNull(context);
        Assert.isNotNull(notification);
        Set<StatusBarNotification> throttledNotificationSet = new HashSet<>();

        // No limiting for non-grouped notifications.
        String groupKey = notification.getGroup();
        if (TextUtils.isEmpty(groupKey)) {
            return throttledNotificationSet;
        }

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        if (activeNotifications.length > HIGH_GLOBAL_NOTIFICATION_COUNT
                && !didLogHighGlobalNotificationCountReached) {
            LogUtil.i(
                    "NotificationThrottler.throttle",
                    "app has %d notifications, system may suppress future notifications",
                    activeNotifications.length);
            didLogHighGlobalNotificationCountReached = true;
            Logger.get(context)
                    .logImpression(DialerImpression.Type.HIGH_GLOBAL_NOTIFICATION_COUNT_REACHED);
        }

        // Count the number of notificatons for this group (excluding the summary).
        int count = 0;
        for (StatusBarNotification currentNotification : activeNotifications) {
            if (isNotificationInGroup(currentNotification, groupKey)) {
                count++;
            }
        }

        if (count > MAX_NOTIFICATIONS_PER_TAG) {
            LogUtil.i(
                    "NotificationThrottler.throttle",
                    "groupKey: %s is over limit, count: %d, limit: %d",
                    groupKey,
                    count,
                    MAX_NOTIFICATIONS_PER_TAG);
            List<StatusBarNotification> notifications = getSortedMatchingNotifications(context, groupKey);
            for (int i = 0; i < (count - MAX_NOTIFICATIONS_PER_TAG); i++) {
                notificationManager.cancel(notifications.get(i).getTag(), notifications.get(i).getId());
                throttledNotificationSet.add(notifications.get(i));
            }
        }
        return throttledNotificationSet;
    }

    private static List<StatusBarNotification> getSortedMatchingNotifications(
            @NonNull Context context, @NonNull String groupKey) {
        List<StatusBarNotification> notifications = new ArrayList<>();
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        for (StatusBarNotification notification : notificationManager.getActiveNotifications()) {
            if (isNotificationInGroup(notification, groupKey)) {
                notifications.add(notification);
            }
        }
        Collections.sort(
                notifications,
                new Comparator<StatusBarNotification>() {
                    @Override
                    public int compare(StatusBarNotification left, StatusBarNotification right) {
                        return Long.compare(left.getPostTime(), right.getPostTime());
                    }
                });
        return notifications;
    }

    private static boolean isNotificationInGroup(
            @NonNull StatusBarNotification notification, @NonNull String groupKey) {
        // Don't include group summaries.
        if ((notification.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) != 0) {
            return false;
        }

        return TextUtils.equals(groupKey, notification.getNotification().getGroup());
    }
}
