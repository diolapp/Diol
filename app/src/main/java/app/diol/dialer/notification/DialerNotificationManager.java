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
import android.support.annotation.Nullable;
import android.support.v4.os.BuildCompat;
import android.text.TextUtils;
import android.util.Pair;

import java.util.HashSet;
import java.util.Set;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Wrapper around the notification manager APIs. The wrapper ensures that channels are set and that
 * notifications are limited to 10 per group.
 */
public final class DialerNotificationManager {

    private static final Set<StatusBarNotification> throttledNotificationSet = new HashSet<>();

    private DialerNotificationManager() {
    }

    public static void notify(@NonNull Context context, int id, @NonNull Notification notification) {
        Assert.isNotNull(context);
        Assert.isNotNull(notification);
        throw Assert.createUnsupportedOperationFailException("all notifications must have tags");
    }

    public static void notify(
            @NonNull Context context, @NonNull String tag, int id, @NonNull Notification notification) {
        Assert.isNotNull(context);
        Assert.isNotNull(notification);
        Assert.checkArgument(!TextUtils.isEmpty(tag));

        if (BuildCompat.isAtLeastO()) {
            Assert.checkArgument(!TextUtils.isEmpty(notification.getChannelId()));
        }

        getNotificationManager(context).notify(tag, id, notification);
        throttledNotificationSet.addAll(NotificationThrottler.throttle(context, notification));
    }

    public static void cancel(@NonNull Context context, int id) {
        Assert.isNotNull(context);
        throw Assert.createUnsupportedOperationFailException(
                "notification IDs are not unique across the app, a tag must be specified");
    }

    public static void cancel(@NonNull Context context, @NonNull String tag, int id) {
        Assert.isNotNull(context);
        Assert.checkArgument(!TextUtils.isEmpty(tag));

        NotificationManager notificationManager = getNotificationManager(context);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();

        String groupKey = findGroupKey(notifications, tag, id);
        if (!TextUtils.isEmpty(groupKey)) {
            Pair<StatusBarNotification, Integer> groupSummaryAndCount =
                    getGroupSummaryAndCount(notifications, groupKey);
            if (groupSummaryAndCount.first != null && groupSummaryAndCount.second <= 1) {
                LogUtil.i(
                        "DialerNotificationManager.cancel",
                        "last notification in group (%s) removed, also removing group summary",
                        groupKey);
                notificationManager.cancel(
                        groupSummaryAndCount.first.getTag(), groupSummaryAndCount.first.getId());
            }
        }

        notificationManager.cancel(tag, id);
    }

    public static void cancelAll(Context context, String prefix) {
        NotificationManager notificationManager = getNotificationManager(context);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getTag() != null && notification.getTag().startsWith(prefix)) {
                notificationManager.cancel(notification.getTag(), notification.getId());
            }
        }
    }

    public static StatusBarNotification[] getActiveNotifications(@NonNull Context context) {
        Assert.isNotNull(context);
        return getNotificationManager(context).getActiveNotifications();
    }

    @Nullable
    private static String findGroupKey(
            @NonNull StatusBarNotification[] notifications, @NonNull String tag, int id) {
        for (StatusBarNotification notification : notifications) {
            if (TextUtils.equals(tag, notification.getTag()) && id == notification.getId()) {
                return notification.getNotification().getGroup();
            }
        }
        return null;
    }

    @NonNull
    private static Pair<StatusBarNotification, Integer> getGroupSummaryAndCount(
            @NonNull StatusBarNotification[] notifications, @NonNull String groupKey) {
        StatusBarNotification groupSummaryNotification = null;
        int groupCount = 0;
        for (StatusBarNotification notification : notifications) {
            if (TextUtils.equals(groupKey, notification.getNotification().getGroup())) {
                if ((notification.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) != 0) {
                    groupSummaryNotification = notification;
                } else {
                    groupCount++;
                }
            }
        }
        return new Pair<>(groupSummaryNotification, groupCount);
    }

    @NonNull
    private static NotificationManager getNotificationManager(@NonNull Context context) {
        return context.getSystemService(NotificationManager.class);
    }

    public static Set<StatusBarNotification> getThrottledNotificationSet() {
        return throttledNotificationSet;
    }
}
