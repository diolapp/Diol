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
package app.diol.voicemail.impl.sync;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.UserManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.PerAccountSharedPreferences;
import app.diol.dialer.common.concurrent.ThreadUtil;
import app.diol.dialer.storage.StorageComponent;
import app.diol.voicemail.VoicemailClient.ActivationStateListener;
import app.diol.voicemail.impl.OmtpConstants;
import app.diol.voicemail.impl.VisualVoicemailPreferences;
import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.sms.StatusMessage;

/**
 * Tracks the activation state of a visual voicemail phone account. An account
 * is considered activated if it has valid connection information from the
 * {@link StatusMessage} stored on the device. Once activation/provisioning is
 * completed, {@link #addAccount(Context, PhoneAccountHandle, StatusMessage)}
 * should be called to store the connection information. When an account is
 * removed or if the connection information is deemed invalid,
 * {@link #removeAccount(Context, PhoneAccountHandle)} should be called to clear
 * the connection information and allow reactivation.
 */
@TargetApi(VERSION_CODES.O)
public class VvmAccountManager {
    public static final String TAG = "VvmAccountManager";

    @VisibleForTesting
    static final String IS_ACCOUNT_ACTIVATED = "is_account_activated";

    private static final Set<ActivationStateListener> listeners = new ArraySet<>();

    public static void addAccount(Context context, PhoneAccountHandle phoneAccountHandle, StatusMessage statusMessage) {
        VisualVoicemailPreferences preferences = new VisualVoicemailPreferences(context, phoneAccountHandle);
        statusMessage.putStatus(preferences.edit()).apply();
        setAccountActivated(context, phoneAccountHandle, true);

        ThreadUtil.postOnUiThread(() -> {
            for (ActivationStateListener listener : listeners) {
                listener.onActivationStateChanged(phoneAccountHandle, true);
            }
        });
    }

    public static void removeAccount(Context context, PhoneAccountHandle phoneAccount) {
        VoicemailStatus.disable(context, phoneAccount);
        setAccountActivated(context, phoneAccount, false);
        VisualVoicemailPreferences preferences = new VisualVoicemailPreferences(context, phoneAccount);
        preferences.edit().putString(OmtpConstants.IMAP_USER_NAME, null).putString(OmtpConstants.IMAP_PASSWORD, null)
                .apply();
        ThreadUtil.postOnUiThread(() -> {
            for (ActivationStateListener listener : listeners) {
                listener.onActivationStateChanged(phoneAccount, false);
            }
        });
    }

    public static boolean isAccountActivated(Context context, PhoneAccountHandle phoneAccount) {
        Assert.isNotNull(phoneAccount);
        PerAccountSharedPreferences preferences = getPreferenceForActivationState(context, phoneAccount);
        migrateActivationState(context, preferences, phoneAccount);
        return preferences.getBoolean(IS_ACCOUNT_ACTIVATED, false);
    }

    @NonNull
    public static List<PhoneAccountHandle> getActiveAccounts(Context context) {
        List<PhoneAccountHandle> results = new ArrayList<>();
        for (PhoneAccountHandle phoneAccountHandle : context.getSystemService(TelecomManager.class)
                .getCallCapablePhoneAccounts()) {
            if (isAccountActivated(context, phoneAccountHandle)) {
                results.add(phoneAccountHandle);
            }
        }
        return results;
    }

    @MainThread
    public static void addListener(ActivationStateListener listener) {
        Assert.isMainThread();
        listeners.add(listener);
    }

    @MainThread
    public static void removeListener(ActivationStateListener listener) {
        Assert.isMainThread();
        listeners.remove(listener);
    }

    /**
     * The activation state is moved from credential protected storage to device
     * protected storage after v10, so it can be checked under FBE. The state should
     * be migrated to avoid reactivation.
     */
    private static void migrateActivationState(Context context, PerAccountSharedPreferences deviceProtectedPreference,
                                               PhoneAccountHandle phoneAccountHandle) {
        if (!context.getSystemService(UserManager.class).isUserUnlocked()) {
            return;
        }
        if (deviceProtectedPreference.contains(IS_ACCOUNT_ACTIVATED)) {
            return;
        }

        PerAccountSharedPreferences credentialProtectedPreference = new VisualVoicemailPreferences(context,
                phoneAccountHandle);

        deviceProtectedPreference.edit()
                .putBoolean(IS_ACCOUNT_ACTIVATED, credentialProtectedPreference.getBoolean(IS_ACCOUNT_ACTIVATED, false))
                .apply();
    }

    private static void setAccountActivated(Context context, PhoneAccountHandle phoneAccountHandle, boolean activated) {
        Assert.isNotNull(phoneAccountHandle);
        getPreferenceForActivationState(context, phoneAccountHandle).edit().putBoolean(IS_ACCOUNT_ACTIVATED, activated)
                .apply();
    }

    private static PerAccountSharedPreferences getPreferenceForActivationState(Context context,
                                                                               PhoneAccountHandle phoneAccountHandle) {
        return new PerAccountSharedPreferences(context, phoneAccountHandle,
                StorageComponent.get(context).unencryptedSharedPrefs());
    }
}
