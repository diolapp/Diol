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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.settings.VisualVoicemailSettingsUtil;

/**
 * Handles migration of data from the visual voicemail client in telephony
 * before O.
 */
public final class PreOMigrationHandler {

    @VisibleForTesting
    static final String PRE_O_MIGRATION_FINISHED = "pre_o_migration_finished";
    // Hidden system APIs to access pre O VVM data
    // Bundle getVisualVoicemailSettings()
    private static final String METHOD_GET_VISUAL_VOICEMAIL_SETTINGS = "getVisualVoicemailSettings";
    /**
     * Key in bundle returned by {@link #METHOD_GET_VISUAL_VOICEMAIL_SETTINGS},
     * indicating whether visual voicemail was enabled or disabled by the user. If
     * the user never explicitly changed this setting, this key will not exist.
     */
    private static final String EXTRA_VISUAL_VOICEMAIL_ENABLED_BY_USER_BOOL = "android.telephony.extra.VISUAL_VOICEMAIL_ENABLED_BY_USER_BOOL";
    /**
     * Key in bundle returned by {@link #METHOD_GET_VISUAL_VOICEMAIL_SETTINGS},
     * indicating the voicemail access PIN scrambled during the auto provisioning
     * process. The user is expected to reset their PIN if this value is not
     * {@code null}.
     */
    private static final String EXTRA_VOICEMAIL_SCRAMBLED_PIN_STRING = "android.telephony.extra.VOICEMAIL_SCRAMBLED_PIN_STRING";

    @WorkerThread
    public static void migrate(Context context, PhoneAccountHandle phoneAccountHandle) {
        Assert.isNotMainThread();
        VisualVoicemailPreferences preferences = new VisualVoicemailPreferences(context, phoneAccountHandle);
        if (preferences.getBoolean(PRE_O_MIGRATION_FINISHED, false)) {
            VvmLog.i("PreOMigrationHandler", phoneAccountHandle + " already migrated");
            return;
        }
        VvmLog.i("PreOMigrationHandler", "migrating " + phoneAccountHandle);
        migrateSettings(context, phoneAccountHandle);

        preferences.edit().putBoolean(PRE_O_MIGRATION_FINISHED, true).apply();
    }

    private static void migrateSettings(Context context, PhoneAccountHandle phoneAccountHandle) {
        VvmLog.i("PreOMigrationHandler.migrateSettings", "migrating settings");
        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class)
                .createForPhoneAccountHandle(phoneAccountHandle);
        if (telephonyManager == null) {
            VvmLog.e("PreOMigrationHandler.migrateSettings", "invalid PhoneAccountHandle");
            return;
        }
        Bundle legacySettings;
        try {
            Method method = TelephonyManager.class.getMethod(METHOD_GET_VISUAL_VOICEMAIL_SETTINGS);
            legacySettings = (Bundle) method.invoke(telephonyManager);
        } catch (ReflectiveOperationException | ClassCastException e) {
            VvmLog.i("PreOMigrationHandler.migrateSettings", "unable to retrieve settings from system");
            return;
        }

        if (legacySettings.containsKey(EXTRA_VISUAL_VOICEMAIL_ENABLED_BY_USER_BOOL)) {
            boolean enabled = legacySettings.getBoolean(EXTRA_VISUAL_VOICEMAIL_ENABLED_BY_USER_BOOL);
            VvmLog.i("PreOMigrationHandler.migrateSettings", "setting VVM enabled to " + enabled);
            VisualVoicemailSettingsUtil.setEnabled(context, phoneAccountHandle, enabled);
        }

        if (legacySettings.containsKey(EXTRA_VOICEMAIL_SCRAMBLED_PIN_STRING)) {
            String scrambledPin = legacySettings.getString(EXTRA_VOICEMAIL_SCRAMBLED_PIN_STRING);
            if (!TextUtils.isEmpty(scrambledPin)) {
                VvmLog.i("PreOMigrationHandler.migrateSettings", "migrating scrambled PIN");
                VoicemailComponent.get(context).getVoicemailClient().createPinChanger(context, phoneAccountHandle)
                        .setScrambledPin(scrambledPin);
            }
        }
    }
}
