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

package app.diol.dialer.compat.telephony;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.os.BuildCompat;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.telecom.TelecomUtil;

/**
 * Hidden APIs in {@link android.telephony.TelephonyManager}.
 */
public class TelephonyManagerCompat {

    // TODO(maxwelb): Use public API for these constants when available
    public static final String EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE =
            "android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE";
    public static final String EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI =
            "android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI";
    public static final String EVENT_HANDOVER_TO_WIFI_FAILED =
            "android.telephony.event.EVENT_HANDOVER_TO_WIFI_FAILED";
    public static final String EVENT_CALL_REMOTELY_HELD = "android.telecom.event.CALL_REMOTELY_HELD";
    public static final String EVENT_CALL_REMOTELY_UNHELD =
            "android.telecom.event.CALL_REMOTELY_UNHELD";
    public static final String EVENT_MERGE_START = "android.telecom.event.MERGE_START";
    public static final String EVENT_MERGE_COMPLETE = "android.telecom.event.MERGE_COMPLETE";

    public static final String EVENT_NOTIFY_INTERNATIONAL_CALL_ON_WFC =
            "android.telephony.event.EVENT_NOTIFY_INTERNATIONAL_CALL_ON_WFC";
    public static final String EVENT_CALL_FORWARDED = "android.telephony.event.EVENT_CALL_FORWARDED";

    public static final String TELEPHONY_MANAGER_CLASS = "android.telephony.TelephonyManager";
    /**
     * Indicates that the call being placed originated from a known contact.
     *
     * <p>This signals to the telephony platform that an outgoing call qualifies for assisted dialing.
     */
    public static final String USE_ASSISTED_DIALING = "android.telecom.extra.USE_ASSISTED_DIALING";

    // TODO(erfanian): a bug Replace with the platform/telecom constant when available.
    /**
     * Additional information relating to the assisted dialing transformation.
     */
    public static final String ASSISTED_DIALING_EXTRAS =
            "android.telecom.extra.ASSISTED_DIALING_EXTRAS";

    // TODO(erfanian): a bug Replace with the platform/telecom API when available.
    /**
     * Indicates the Connection/Call used assisted dialing.
     */
    public static final int PROPERTY_ASSISTED_DIALING_USED = 1 << 9;
    public static final String EXTRA_IS_REFRESH =
            BuildCompat.isAtLeastOMR1() ? "android.telephony.extra.IS_REFRESH" : "is_refresh";
    /**
     * Indicates the call underwent Assisted Dialing; typically set as a feature available from the
     * CallLog.
     */
    public static final Integer FEATURES_ASSISTED_DIALING = 1 << 4;
    /**
     * Flag specifying whether to show an alert dialog for video call charges. By default this value
     * is {@code false}. TODO(a bug): Replace with public API for these constants when available.
     */
    public static final String CARRIER_CONFIG_KEY_SHOW_VIDEO_CALL_CHARGES_ALERT_DIALOG_BOOL =
            "show_video_call_charges_alert_dialog_bool";
    private static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";

    /**
     * Returns the number of phones available. Returns 1 for Single standby mode (Single SIM
     * functionality) Returns 2 for Dual standby mode.(Dual SIM functionality)
     *
     * <p>Returns 1 if the method or telephonyManager is not available.
     *
     * @param telephonyManager The telephony manager instance to use for method calls.
     */
    public static int getPhoneCount(@Nullable TelephonyManager telephonyManager) {
        if (telephonyManager == null) {
            return 1;
        }
        return telephonyManager.getPhoneCount();
    }

    /**
     * Whether the phone supports TTY mode.
     *
     * @param telephonyManager The telephony manager instance to use for method calls.
     * @return {@code true} if the device supports TTY mode, and {@code false} otherwise.
     */
    public static boolean isTtyModeSupported(@Nullable TelephonyManager telephonyManager) {
        return telephonyManager != null && telephonyManager.isTtyModeSupported();
    }

    /**
     * Whether the phone supports hearing aid compatibility.
     *
     * @param telephonyManager The telephony manager instance to use for method calls.
     * @return {@code true} if the device supports hearing aid compatibility, and {@code false}
     * otherwise.
     */
    public static boolean isHearingAidCompatibilitySupported(
            @Nullable TelephonyManager telephonyManager) {
        return telephonyManager != null && telephonyManager.isHearingAidCompatibilitySupported();
    }

    /**
     * Returns the URI for the per-account voicemail ringtone set in Phone settings.
     *
     * @param telephonyManager The telephony manager instance to use for method calls.
     * @param accountHandle    The handle for the {@link android.telecom.PhoneAccount} for which to
     *                         retrieve the voicemail ringtone.
     * @return The URI for the ringtone to play when receiving a voicemail from a specific
     * PhoneAccount.
     */
    @Nullable
    @RequiresApi(VERSION_CODES.N)
    public static Uri getVoicemailRingtoneUri(
            TelephonyManager telephonyManager, PhoneAccountHandle accountHandle) {
        return telephonyManager.getVoicemailRingtoneUri(accountHandle);
    }

    /**
     * Returns whether vibration is set for voicemail notification in Phone settings.
     *
     * @param telephonyManager The telephony manager instance to use for method calls.
     * @param accountHandle    The handle for the {@link android.telecom.PhoneAccount} for which to
     *                         retrieve the voicemail vibration setting.
     * @return {@code true} if the vibration is set for this PhoneAccount, {@code false} otherwise.
     */
    @RequiresApi(VERSION_CODES.N)
    public static boolean isVoicemailVibrationEnabled(
            TelephonyManager telephonyManager, PhoneAccountHandle accountHandle) {
        return telephonyManager.isVoicemailVibrationEnabled(accountHandle);
    }

    /**
     * This method uses a new system API to enable or disable visual voicemail. TODO(twyen): restrict
     * to N MR1, not needed in future SDK.
     */
    public static void setVisualVoicemailEnabled(
            TelephonyManager telephonyManager, PhoneAccountHandle handle, boolean enabled) {
        try {
            TelephonyManager.class
                    .getMethod("setVisualVoicemailEnabled", PhoneAccountHandle.class, boolean.class)
                    .invoke(telephonyManager, handle, enabled);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LogUtil.e("TelephonyManagerCompat.setVisualVoicemailEnabled", "failed", e);
        }
    }

    /**
     * This method uses a new system API to check if visual voicemail is enabled TODO(twyen): restrict
     * to N MR1, not needed in future SDK.
     */
    public static boolean isVisualVoicemailEnabled(
            TelephonyManager telephonyManager, PhoneAccountHandle handle) {
        try {
            return (boolean)
                    TelephonyManager.class
                            .getMethod("isVisualVoicemailEnabled", PhoneAccountHandle.class)
                            .invoke(telephonyManager, handle);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LogUtil.e("TelephonyManagerCompat.setVisualVoicemailEnabled", "failed", e);
        }
        return false;
    }

    /**
     * Handles secret codes to launch arbitrary activities.
     *
     * @param context    the context to use
     * @param secretCode the secret code without the "*#*#" prefix and "#*#*" suffix
     */
    public static void handleSecretCode(Context context, String secretCode) {
        // Must use system service on O+ to avoid using broadcasts, which are not allowed on O+.
        if (BuildCompat.isAtLeastO()) {
            if (!TelecomUtil.isDefaultDialer(context)) {
                LogUtil.e(
                        "TelephonyManagerCompat.handleSecretCode",
                        "not default dialer, cannot send special code");
                return;
            }
            context.getSystemService(TelephonyManager.class).sendDialerSpecialCode(secretCode);
        } else {
            // System service call is not supported pre-O, so must use a broadcast for N-.
            Intent intent =
                    new Intent(SECRET_CODE_ACTION, Uri.parse("android_secret_code://" + secretCode));
            context.sendBroadcast(intent);
        }
    }

    /**
     * Returns network country iso for given {@code PhoneAccountHandle} for O+ devices and country iso
     * for default sim for pre-O devices.
     */
    public static String getNetworkCountryIsoForPhoneAccountHandle(
            Context context, @Nullable PhoneAccountHandle phoneAccountHandle) {
        return getTelephonyManagerForPhoneAccountHandle(context, phoneAccountHandle)
                .getNetworkCountryIso();
    }

    /**
     * Returns TelephonyManager for given {@code PhoneAccountHandle} for O+ devices and default {@code
     * TelephonyManager} for pre-O devices.
     */
    public static TelephonyManager getTelephonyManagerForPhoneAccountHandle(
            Context context, @Nullable PhoneAccountHandle phoneAccountHandle) {
        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class);
        if (phoneAccountHandle == null) {
            return telephonyManager;
        }
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            TelephonyManager telephonyManagerForPhoneAccount =
                    telephonyManager.createForPhoneAccountHandle(phoneAccountHandle);
            if (telephonyManagerForPhoneAccount != null) {
                return telephonyManagerForPhoneAccount;
            }
        }
        return telephonyManager;
    }
}
