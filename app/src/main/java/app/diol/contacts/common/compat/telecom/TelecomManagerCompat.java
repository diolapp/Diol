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

package app.diol.contacts.common.compat.telecom;

import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import java.lang.reflect.Field;

/**
 * Compatibility class for {@link android.telecom.TelecomManager}.
 */
public class TelecomManagerCompat {

    // Constants from http://cs/android/frameworks/base/telecomm/java/android/telecom/Call.java.
    public static final String EVENT_REQUEST_HANDOVER = "android.telecom.event.REQUEST_HANDOVER";
    public static final String EXTRA_HANDOVER_PHONE_ACCOUNT_HANDLE =
            "android.telecom.extra.HANDOVER_PHONE_ACCOUNT_HANDLE";
    public static final String EXTRA_HANDOVER_VIDEO_STATE =
            "android.telecom.extra.HANDOVER_VIDEO_STATE";

    // This is a hidden constant in android.telecom.DisconnectCause. Telecom sets this as a disconnect
    // reason if it wants us to prompt the user that the video call is not available.
    // TODO(wangqi): Reference it to constant in android.telecom.DisconnectCause.
    public static final String REASON_IMS_ACCESS_BLOCKED = "REASON_IMS_ACCESS_BLOCKED";

    /**
     * Returns the current SIM call manager. Apps must be prepared for this method to return null,
     * indicating that there currently exists no registered SIM call manager.
     *
     * @param telecomManager the {@link TelecomManager} to use to fetch the SIM call manager.
     * @return The phone account handle of the current sim call manager.
     */
    @Nullable
    public static PhoneAccountHandle getSimCallManager(TelecomManager telecomManager) {
        if (telecomManager != null) {
            return telecomManager.getSimCallManager();
        }
        return null;
    }

    /**
     * Handovers are supported from Android O-DR onward. Since there is no API bump from O to O-DR, we
     * need to use reflection to check the existence of TelecomManager.EXTRA_IS_HANDOVER in
     * http://cs/android/frameworks/base/telecomm/java/android/telecom/TelecomManager.java.
     */
    public static boolean supportsHandover() {
        //
        try {
            Field field = TelecomManager.class.getDeclaredField("EXTRA_IS_HANDOVER");
            return "android.telecom.extra.IS_HANDOVER".equals(field.get(null /* obj (static field) */));
        } catch (Exception e) {
            // Do nothing
        }
        return false;
    }
}
