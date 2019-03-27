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

import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import app.diol.dialer.common.Assert;

/**
 * Utilities to manage notifications.
 */
public final class NotificationManagerUtils {
    private NotificationManagerUtils() {
    }

    public static void cancelAllInGroup(@NonNull Context context, @NonNull String groupKey) {
        Assert.isNotNull(context);
        Assert.checkArgument(!TextUtils.isEmpty(groupKey));

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        for (StatusBarNotification notification : notificationManager.getActiveNotifications()) {
            if (TextUtils.equals(groupKey, notification.getNotification().getGroup())) {
                notificationManager.cancel(notification.getTag(), notification.getId());
            }
        }
    }
}
