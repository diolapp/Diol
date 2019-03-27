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

package app.diol.voicemail;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles permission checking for the voicemail module. Currently "phone" and "sms" permissions are
 * required.
 */
public class VoicemailPermissionHelper {

    /**
     * _VOICEMAIL permissions are auto-granted by being the default dialer.
     */
    private static final String[] VOICEMAIL_PERMISSIONS = {
            permission.ADD_VOICEMAIL,
            permission.WRITE_VOICEMAIL,
            permission.READ_VOICEMAIL,
            permission.READ_PHONE_STATE,
            permission.SEND_SMS
    };

    /**
     * Returns {@code true} if the app has all permissions required for the voicemail module to
     * operate.
     */
    public static boolean hasPermissions(Context context) {
        return getMissingPermissions(context).isEmpty();
    }

    /**
     * Returns a list of permission that is missing for the voicemail module to operate.
     */
    @NonNull
    public static List<String> getMissingPermissions(Context context) {
        List<String> result = new ArrayList<>();
        for (String permission : VOICEMAIL_PERMISSIONS) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                result.add(permission);
            }
        }
        return result;
    }
}
