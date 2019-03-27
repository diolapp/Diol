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

package app.diol.dialer.preferredsim;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProviderComponent;

/**
 * Utilities for looking up and validating preferred {@link PhoneAccountHandle}. Contacts should
 * follow the same logic.
 */
public class PreferredAccountUtil {

    /**
     * Validates {@code componentNameString} and {@code idString} maps to SIM that is present on the
     * device.
     */
    @NonNull
    public static Optional<PhoneAccountHandle> getValidPhoneAccount(
            @NonNull Context context, @Nullable String componentNameString, @Nullable String idString) {
        if (TextUtils.isEmpty(componentNameString) || TextUtils.isEmpty(idString)) {
            LogUtil.i("PreferredAccountUtil.getValidPhoneAccount", "empty componentName or id");
            return Optional.absent();
        }
        ComponentName componentName = ComponentName.unflattenFromString(componentNameString);
        if (componentName == null) {
            LogUtil.e("PreferredAccountUtil.getValidPhoneAccount", "cannot parse component name");
            return Optional.absent();
        }
        PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(componentName, idString);

        if (isPhoneAccountValid(context, phoneAccountHandle)) {
            return Optional.of(phoneAccountHandle);
        }
        return Optional.absent();
    }

    public static boolean isPhoneAccountValid(
            Context context, PhoneAccountHandle phoneAccountHandle) {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            return context
                    .getSystemService(TelephonyManager.class)
                    .createForPhoneAccountHandle(phoneAccountHandle)
                    != null;
        }

        PhoneAccount phoneAccount =
                context.getSystemService(TelecomManager.class).getPhoneAccount(phoneAccountHandle);
        if (phoneAccount == null) {
            LogUtil.e("PreferredAccountUtil.isPhoneAccountValid", "invalid phone account");
            return false;
        }

        if (!phoneAccount.isEnabled()) {
            LogUtil.e("PreferredAccountUtil.isPhoneAccountValid", "disabled phone account");
            return false;
        }
        for (SubscriptionInfo info :
                SubscriptionManager.from(context).getActiveSubscriptionInfoList()) {
            if (phoneAccountHandle.getId().startsWith(info.getIccId())) {
                LogUtil.i("PreferredAccountUtil.isPhoneAccountValid", "sim found");
                return true;
            }
        }
        return false;
    }

    /**
     * Return a set of {@link android.accounts.Account#type} that is known to have writable contacts.
     * This is a light weight implementation of {@link
     * app.diol.contacts.common.model.AccountTypeManager#getAccountTypes(boolean)}. External
     * accounts are not supported.
     */
    public static ImmutableSet<String> getValidAccountTypes(Context context) {
        return ImmutableSet.copyOf(
                ConfigProviderComponent.get(context)
                        .getConfigProvider()
                        .getString(
                                "preferred_sim_valid_account_types",
                                "com.google;"
                                        + "com.osp.app.signin;"
                                        + "com.android.exchange;"
                                        + "com.google.android.exchange;"
                                        + "com.google.android.gm.exchange")
                        .split(";"));
    }
}
