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

package app.diol.dialer.assisteddialing;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.os.UserManagerCompat;
import android.telephony.TelephonyManager;

import app.diol.R;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProvider;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.strictmode.StrictModeUtils;

/**
 * A Creator for AssistedDialingMediators.
 *
 * <p>This helps keep the dependencies required by AssistedDialingMediator for assisted dialing
 * explicit.
 */
public final class ConcreteCreator {

    // Ceiling set at P (version code 28) because this feature will ship as part of the framework in
    // Q.
    public static final int BUILD_CODE_CEILING = 28;

    /**
     * Creates a new AssistedDialingMediator
     *
     * @param telephonyManager The telephony manager used to determine user location.
     * @param context          The context used to determine whether or not a provided number is an emergency
     *                         number.
     * @return An AssistedDialingMediator
     */
    public static AssistedDialingMediator createNewAssistedDialingMediator(
            @NonNull TelephonyManager telephonyManager, @NonNull Context context) {

        ConfigProvider configProvider = ConfigProviderComponent.get(context).getConfigProvider();

        if (telephonyManager == null) {
            LogUtil.i(
                    "ConcreteCreator.createNewAssistedDialingMediator", "provided TelephonyManager was null");
            throw new NullPointerException("Provided TelephonyManager was null");
        }
        if (context == null) {
            LogUtil.i("ConcreteCreator.createNewAssistedDialingMediator", "provided context was null");
            throw new NullPointerException("Provided context was null");
        }

        if (!UserManagerCompat.isUserUnlocked(context)) {
            // To avoid any issues reading preferences, we disable the feature when the user is in a
            // locked state.
            LogUtil.i("ConcreteCreator.createNewAssistedDialingMediator", "user is locked");
            return new AssistedDialingMediatorStub();
        }

        if (!isAssistedDialingEnabled(configProvider)) {
            LogUtil.i("ConcreteCreator.createNewAssistedDialingMediator", "feature not enabled");
            return new AssistedDialingMediatorStub();
        }

        if (!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.assisted_dialing_setting_toggle_key), true)) {
            LogUtil.i("ConcreteCreator.createNewAssistedDialingMediator", "disabled by local setting");

            return new AssistedDialingMediatorStub();
        }

        Constraints constraints = new Constraints(context, getCountryCodeProvider(configProvider));
        return new AssistedDialingMediatorImpl(
                new LocationDetector(
                        telephonyManager,
                        StrictModeUtils.bypass(
                                () ->
                                        PreferenceManager.getDefaultSharedPreferences(context)
                                                .getString(
                                                        context.getString(R.string.assisted_dialing_setting_cc_key), null))),
                new NumberTransformer(constraints));
    }

    /**
     * Returns a boolean indicating whether or not the assisted dialing feature is enabled.
     */
    public static boolean isAssistedDialingEnabled(@NonNull ConfigProvider configProvider) {
        if (configProvider == null) {
            LogUtil.i("ConcreteCreator.isAssistedDialingEnabled", "provided configProvider was null");
            throw new NullPointerException("Provided configProvider was null");
        }

        return Build.VERSION.SDK_INT <= BUILD_CODE_CEILING
                && configProvider.getBoolean("assisted_dialing_enabled", false);
    }

    /**
     * Returns a CountryCodeProvider responsible for providing countries eligible for assisted Dialing
     */
    public static CountryCodeProvider getCountryCodeProvider(ConfigProvider configProvider) {
        if (configProvider == null) {
            LogUtil.i("ConcreteCreator.getCountryCodeProvider", "provided configProvider was null");
            throw new NullPointerException("Provided configProvider was null");
        }

        return new CountryCodeProvider(configProvider);
    }
}
