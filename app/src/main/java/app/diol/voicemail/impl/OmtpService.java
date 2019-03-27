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

package app.diol.voicemail.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;
import android.telephony.VisualVoicemailService;
import android.telephony.VisualVoicemailSms;

import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.settings.VisualVoicemailSettingsUtil;
import app.diol.voicemail.impl.sms.LegacyModeSmsHandler;
import app.diol.voicemail.impl.sync.VvmAccountManager;

/**
 * Implements {@link VisualVoicemailService} to receive visual voicemail events
 */
@TargetApi(VERSION_CODES.O)
public class OmtpService extends VisualVoicemailService {

    public static final String ACTION_SMS_RECEIVED = "com.android.vociemailomtp.sms.sms_received";
    public static final String EXTRA_VOICEMAIL_SMS = "extra_voicemail_sms";
    private static final String TAG = "VvmOmtpService";
    private static final String IS_SHUTTING_DOWN = "app.diol.voicemail.impl.is_shutting_down";

    @MainThread
    static void onBoot(@NonNull Context context) {
        VvmLog.i(TAG, "onBoot");
        Assert.isTrue(isUserUnlocked(context));
        Assert.isMainThread();
        setShuttingDown(context, false);
    }

    @MainThread
    static void onShutdown(@NonNull Context context) {
        VvmLog.i(TAG, "onShutdown");
        Assert.isTrue(isUserUnlocked(context));
        Assert.isMainThread();
        setShuttingDown(context, true);
    }

    private static boolean isUserUnlocked(@NonNull Context context) {
        UserManager userManager = context.getSystemService(UserManager.class);
        return userManager.isUserUnlocked();
    }

    private static void setShuttingDown(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit()
                .putBoolean(IS_SHUTTING_DOWN, value).apply();
    }

    private static boolean isShuttingDown(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getBoolean(IS_SHUTTING_DOWN,
                false);
    }

    @Override
    public void onCellServiceConnected(VisualVoicemailTask task, final PhoneAccountHandle phoneAccountHandle) {
        VvmLog.i(TAG, "onCellServiceConnected");
        if (!isModuleEnabled()) {
            VvmLog.e(TAG, "onCellServiceConnected received when module is disabled");
            task.finish();
            return;
        }

        if (!isUserUnlocked(this)) {
            VvmLog.i(TAG, "onCellServiceConnected: user locked");
            task.finish();
            return;
        }

        if (!isServiceEnabled(phoneAccountHandle)) {
            disableFilter(phoneAccountHandle);
            task.finish();
            return;
        }

        Logger.get(this).logImpression(DialerImpression.Type.VVM_UNBUNDLED_EVENT_RECEIVED);
        ActivationTask.start(OmtpService.this, phoneAccountHandle, null);
        task.finish();
    }

    @Override
    public void onSmsReceived(VisualVoicemailTask task, final VisualVoicemailSms sms) {
        VvmLog.i(TAG, "onSmsReceived");
        if (!isModuleEnabled()) {
            VvmLog.e(TAG, "onSmsReceived received when module is disabled");
            task.finish();
            return;
        }

        if (!isUserUnlocked(this)) {
            LegacyModeSmsHandler.handle(this, sms);
            return;
        }

        if (!isServiceEnabled(sms.getPhoneAccountHandle())) {
            VvmLog.e(TAG, "onSmsReceived received when service is disabled");
            disableFilter(sms.getPhoneAccountHandle());
            task.finish();
            return;
        }

        // isUserUnlocked() is not checked. OmtpMessageReceiver will handle the locked
        // case.

        Logger.get(this).logImpression(DialerImpression.Type.VVM_UNBUNDLED_EVENT_RECEIVED);
        Intent intent = new Intent(ACTION_SMS_RECEIVED);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_VOICEMAIL_SMS, sms);
        sendBroadcast(intent);
        task.finish();
    }

    @Override
    public void onSimRemoved(final VisualVoicemailTask task, final PhoneAccountHandle phoneAccountHandle) {
        VvmLog.i(TAG, "onSimRemoved");
        if (!isModuleEnabled()) {
            VvmLog.e(TAG, "onSimRemoved called when module is disabled");
            task.finish();
            return;
        }

        if (!isUserUnlocked(this)) {
            VvmLog.i(TAG, "onSimRemoved: user locked");
            task.finish();
            return;
        }

        if (isShuttingDown(this)) {
            VvmLog.i(TAG, "onSimRemoved: system shutting down, ignoring");
            task.finish();
            return;
        }

        Logger.get(this).logImpression(DialerImpression.Type.VVM_UNBUNDLED_EVENT_RECEIVED);
        VvmAccountManager.removeAccount(this, phoneAccountHandle);
        task.finish();
    }

    @Override
    public void onStopped(VisualVoicemailTask task) {
        VvmLog.i(TAG, "onStopped");
        if (!isModuleEnabled()) {
            VvmLog.e(TAG, "onStopped called when module is disabled");
            task.finish();
            return;
        }
        if (!isUserUnlocked(this)) {
            VvmLog.i(TAG, "onStopped: user locked");
            task.finish();
            return;
        }
        Logger.get(this).logImpression(DialerImpression.Type.VVM_UNBUNDLED_EVENT_RECEIVED);
    }

    private boolean isModuleEnabled() {
        return VoicemailComponent.get(this).getVoicemailClient().isVoicemailModuleEnabled();
    }

    private boolean isServiceEnabled(PhoneAccountHandle phoneAccountHandle) {
        OmtpVvmCarrierConfigHelper config = new OmtpVvmCarrierConfigHelper(this, phoneAccountHandle);
        if (!config.isValid()) {
            VvmLog.i(TAG, "VVM not supported on " + phoneAccountHandle);
            return false;
        }
        if (!VisualVoicemailSettingsUtil.isEnabled(this, phoneAccountHandle) && !config.isLegacyModeEnabled()) {
            VvmLog.i(TAG, "VVM is disabled");
            return false;
        }
        return true;
    }

    private void disableFilter(PhoneAccountHandle phoneAccountHandle) {
        TelephonyManager telephonyManager = getSystemService(TelephonyManager.class)
                .createForPhoneAccountHandle(phoneAccountHandle);
        if (telephonyManager != null) {
            VvmLog.i(TAG, "disabling SMS filter");
            telephonyManager.setVisualVoicemailSmsFilterSettings(null);
        }
    }
}
