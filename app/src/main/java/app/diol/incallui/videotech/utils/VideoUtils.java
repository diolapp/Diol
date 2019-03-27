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

package app.diol.incallui.videotech.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import app.diol.dialer.util.PermissionsUtil;

public class VideoUtils {

    public static boolean hasSentVideoUpgradeRequest(@SessionModificationState int state) {
        return state == SessionModificationState.WAITING_FOR_UPGRADE_TO_VIDEO_RESPONSE
                || state == SessionModificationState.UPGRADE_TO_VIDEO_REQUEST_FAILED
                || state == SessionModificationState.REQUEST_REJECTED
                || state == SessionModificationState.UPGRADE_TO_VIDEO_REQUEST_TIMED_OUT;
    }

    public static boolean hasReceivedVideoUpgradeRequest(@SessionModificationState int state) {
        return state == SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST;
    }

    public static boolean hasCameraPermissionAndShownPrivacyToast(@NonNull Context context) {
        return PermissionsUtil.hasCameraPrivacyToastShown(context) && hasCameraPermission(context);
    }

    public static boolean hasCameraPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }
}
