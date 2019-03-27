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

package app.diol.dialer.app.settings;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.VisibleForTesting;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import java.util.List;

/**
 * Preference screen that lists SIM phone accounts to select from, and forwards the selected account
 * to {@link #PARAM_TARGET_FRAGMENT}. Can only be used in a {@link PreferenceActivity}
 */
public class PhoneAccountSelectionFragment extends PreferenceFragment {

    /**
     * The {@link PreferenceFragment} to launch after the account is selected.
     */
    public static final String PARAM_TARGET_FRAGMENT = "target_fragment";

    /**
     * The arguments bundle to pass to the {@link #PARAM_TARGET_FRAGMENT}
     *
     * @see Fragment#getArguments()
     */
    public static final String PARAM_ARGUMENTS = "arguments";

    /**
     * The key to insert the selected {@link PhoneAccountHandle} to bundle in {@link #PARAM_ARGUMENTS}
     */
    public static final String PARAM_PHONE_ACCOUNT_HANDLE_KEY = "phone_account_handle_key";

    /**
     * The title of the {@link #PARAM_TARGET_FRAGMENT} once it is launched with {@link
     * PreferenceActivity#startWithFragment(String, Bundle, Fragment, int)}, as a string resource ID.
     */
    public static final String PARAM_TARGET_TITLE_RES = "target_title_res";

    private String targetFragment;
    private Bundle arguments;
    private String phoneAccountHandleKey;
    private int titleRes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        targetFragment = getArguments().getString(PARAM_TARGET_FRAGMENT);
        arguments = new Bundle();
        arguments.putAll(getArguments().getBundle(PARAM_ARGUMENTS));
        phoneAccountHandleKey = getArguments().getString(PARAM_PHONE_ACCOUNT_HANDLE_KEY);
        titleRes = getArguments().getInt(PARAM_TARGET_TITLE_RES, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
        PreferenceScreen screen = getPreferenceScreen();

        TelecomManager telecomManager = getContext().getSystemService(TelecomManager.class);

        List<PhoneAccountHandle> accountHandles = telecomManager.getCallCapablePhoneAccounts();

        Context context = getActivity();
        for (PhoneAccountHandle handle : accountHandles) {
            PhoneAccount account = telecomManager.getPhoneAccount(handle);
            if (account != null) {
                final boolean isSimAccount =
                        0 != (account.getCapabilities() & PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION);
                if (isSimAccount) {
                    screen.addPreference(new AccountPreference(context, handle, account));
                }
            }
        }
    }

    final class AccountPreference extends Preference {
        private final PhoneAccountHandle phoneAccountHandle;

        public AccountPreference(
                Context context, PhoneAccountHandle phoneAccountHandle, PhoneAccount phoneAccount) {
            super(context);
            this.phoneAccountHandle = phoneAccountHandle;
            setTitle(phoneAccount.getLabel());
            setSummary(phoneAccount.getShortDescription());
            Icon icon = phoneAccount.getIcon();
            if (icon != null) {
                setIcon(icon.loadDrawable(context));
            }
        }

        @VisibleForTesting
        void click() {
            onClick();
        }

        @Override
        protected void onClick() {
            super.onClick();
            PreferenceActivity preferenceActivity = (PreferenceActivity) getActivity();
            arguments.putParcelable(phoneAccountHandleKey, phoneAccountHandle);
            preferenceActivity.startWithFragment(targetFragment, arguments, null, 0, titleRes, 0);
        }
    }
}
