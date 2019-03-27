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

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Centralized source of all notification channels used by Dialer.
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({
        NotificationChannelId.INCOMING_CALL,
        NotificationChannelId.ONGOING_CALL,
        NotificationChannelId.MISSED_CALL,
        NotificationChannelId.DEFAULT,
})
public @interface NotificationChannelId {
    // This value is white listed in the system.
    // See /vendor/google/nexus_overlay/common/frameworks/base/core/res/res/values/config.xml
    String INCOMING_CALL = "phone_incoming_call";

    String ONGOING_CALL = "phone_ongoing_call";

    String MISSED_CALL = "phone_missed_call";

    String DEFAULT = "phone_default";
}
