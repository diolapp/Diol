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

package app.diol.dialer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import java.util.List;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;

/**
 * Utilities related to calls that can be used by non system apps.
 */
public class CallUtil {

    /**
     * Indicates that the video calling is not available.
     */
    public static final int VIDEO_CALLING_DISABLED = 0;

    /**
     * Indicates that video calling is enabled, regardless of presence status.
     */
    public static final int VIDEO_CALLING_ENABLED = 1;

    /**
     * Indicates that video calling is enabled, but the availability of video call affordances is
     * determined by the presence status associated with contacts.
     */
    public static final int VIDEO_CALLING_PRESENCE = 2;

    private static boolean hasInitializedIsVideoEnabledState;
    private static boolean cachedIsVideoEnabledState;

    /**
     * Return Uri with an appropriate scheme, accepting both SIP and usual phone call numbers.
     */
    public static Uri getCallUri(String number) {
        if (PhoneNumberHelper.isUriNumber(number)) {
            return Uri.fromParts(PhoneAccount.SCHEME_SIP, number, null);
        }
        return Uri.fromParts(PhoneAccount.SCHEME_TEL, number, null);
    }

    /**
     * Determines if video calling is available, and if so whether presence checking is available as
     * well.
     *
     * <p>Returns a bitmask with {@link #VIDEO_CALLING_ENABLED} to indicate that video calling is
     * available, and {@link #VIDEO_CALLING_PRESENCE} if presence indication is also available.
     *
     * @param context The context
     * @return A bit-mask describing the current video capabilities.
     */
    @SuppressLint("MissingPermission")
    public static int getVideoCallingAvailability(Context context) {
        if (!PermissionsUtil.hasPermission(context, android.Manifest.permission.READ_PHONE_STATE)) {
            return VIDEO_CALLING_DISABLED;
        }
        TelecomManager telecommMgr = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecommMgr == null) {
            return VIDEO_CALLING_DISABLED;
        }

        List<PhoneAccountHandle> accountHandles = telecommMgr.getCallCapablePhoneAccounts();
        for (PhoneAccountHandle accountHandle : accountHandles) {
            PhoneAccount account = telecommMgr.getPhoneAccount(accountHandle);
            if (account != null) {
                if (account.hasCapabilities(PhoneAccount.CAPABILITY_VIDEO_CALLING)) {
                    int videoCapabilities = VIDEO_CALLING_ENABLED;
                    if (account.hasCapabilities(PhoneAccount.CAPABILITY_VIDEO_CALLING_RELIES_ON_PRESENCE)) {
                        videoCapabilities |= VIDEO_CALLING_PRESENCE;
                    }
                    return videoCapabilities;
                }
            }
        }
        return VIDEO_CALLING_DISABLED;
    }

    /**
     * Determines if one of the call capable phone accounts defined supports video calling.
     *
     * @param context The context.
     * @return {@code true} if one of the call capable phone accounts supports video calling, {@code
     * false} otherwise.
     */
    public static boolean isVideoEnabled(Context context) {
        boolean isVideoEnabled = (getVideoCallingAvailability(context) & VIDEO_CALLING_ENABLED) != 0;

        // Log everytime the video enabled state changes.
        if (!hasInitializedIsVideoEnabledState) {
            LogUtil.i("CallUtil.isVideoEnabled", "isVideoEnabled: " + isVideoEnabled);
            hasInitializedIsVideoEnabledState = true;
            cachedIsVideoEnabledState = isVideoEnabled;
        } else if (cachedIsVideoEnabledState != isVideoEnabled) {
            LogUtil.i(
                    "CallUtil.isVideoEnabled",
                    "isVideoEnabled changed from %b to %b",
                    cachedIsVideoEnabledState,
                    isVideoEnabled);
            cachedIsVideoEnabledState = isVideoEnabled;
        }

        return isVideoEnabled;
    }

    /**
     * Determines if one of the call capable phone accounts defined supports calling with a subject
     * specified.
     *
     * @param context The context.
     * @return {@code true} if one of the call capable phone accounts supports calling with a subject
     * specified, {@code false} otherwise.
     */
    @SuppressLint("MissingPermission")
    public static boolean isCallWithSubjectSupported(Context context) {
        if (!PermissionsUtil.hasPermission(context, android.Manifest.permission.READ_PHONE_STATE)) {
            return false;
        }
        TelecomManager telecommMgr = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecommMgr == null) {
            return false;
        }

        List<PhoneAccountHandle> accountHandles = telecommMgr.getCallCapablePhoneAccounts();
        for (PhoneAccountHandle accountHandle : accountHandles) {
            PhoneAccount account = telecommMgr.getPhoneAccount(accountHandle);
            if (account != null && account.hasCapabilities(PhoneAccount.CAPABILITY_CALL_SUBJECT)) {
                return true;
            }
        }
        return false;
    }
}
