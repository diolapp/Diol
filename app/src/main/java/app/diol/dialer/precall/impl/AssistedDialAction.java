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

package app.diol.dialer.precall.impl;

import android.content.Context;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;

import java.util.Optional;

import app.diol.dialer.assisteddialing.AssistedDialingMediator;
import app.diol.dialer.assisteddialing.ConcreteCreator;
import app.diol.dialer.assisteddialing.TransformationInfo;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.compat.telephony.TelephonyManagerCompat;
import app.diol.dialer.configprovider.ConfigProvider;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.precall.PreCallAction;
import app.diol.dialer.precall.PreCallCoordinator;
import app.diol.dialer.telecom.TelecomUtil;
import app.diol.dialer.util.CallUtil;

/**
 * Rewrites the call URI with country code.
 */
public class AssistedDialAction implements PreCallAction {

    @Override
    public boolean requiresUi(Context context, CallIntentBuilder builder) {
        return false;
    }

    @Override
    public void runWithoutUi(Context context, CallIntentBuilder builder) {
        if (!builder.isAssistedDialAllowed()) {
            return;
        }

        AssistedDialingMediator assistedDialingMediator =
                ConcreteCreator.createNewAssistedDialingMediator(
                        getAssistedDialingTelephonyManager(context, builder), context);

        // Checks the platform is N+ and meets other pre-flight checks.
        if (!assistedDialingMediator.isPlatformEligible()) {
            return;
        }
        String phoneNumber =
                builder.getUri().getScheme().equals(PhoneAccount.SCHEME_TEL)
                        ? builder.getUri().getSchemeSpecificPart()
                        : "";
        Optional<TransformationInfo> transformedNumber =
                assistedDialingMediator.attemptAssistedDial(phoneNumber);
        if (transformedNumber.isPresent()) {
            builder
                    .getInCallUiIntentExtras()
                    .putBoolean(TelephonyManagerCompat.USE_ASSISTED_DIALING, true);
            Bundle assistedDialingExtras = transformedNumber.get().toBundle();
            builder
                    .getInCallUiIntentExtras()
                    .putBundle(TelephonyManagerCompat.ASSISTED_DIALING_EXTRAS, assistedDialingExtras);
            builder.setUri(
                    CallUtil.getCallUri(Assert.isNotNull(transformedNumber.get().transformedNumber())));
            LogUtil.i("AssistedDialAction.runWithoutUi", "assisted dialing was used.");
        }
    }

    /**
     * A convenience method to return the proper TelephonyManager in possible multi-sim environments.
     */
    private TelephonyManager getAssistedDialingTelephonyManager(
            Context context, CallIntentBuilder builder) {

        ConfigProvider configProvider = ConfigProviderComponent.get(context).getConfigProvider();
        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class);
        // None of this will be required in the framework because the PhoneAccountHandle
        // is already mapped to the request in the TelecomConnection.
        if (builder.getPhoneAccountHandle() == null) {
            return telephonyManager;
        }

        if (!configProvider.getBoolean("assisted_dialing_dual_sim_enabled", false)) {
            return telephonyManager;
        }

        com.google.common.base.Optional<SubscriptionInfo> subscriptionInfo =
                TelecomUtil.getSubscriptionInfo(context, builder.getPhoneAccountHandle());
        if (!subscriptionInfo.isPresent()) {
            LogUtil.i(
                    "AssistedDialAction.getAssistedDialingTelephonyManager", "subcriptionInfo was absent.");
            return telephonyManager;
        }
        TelephonyManager pinnedtelephonyManager =
                telephonyManager.createForSubscriptionId(subscriptionInfo.get().getSubscriptionId());
        if (pinnedtelephonyManager == null) {
            LogUtil.i(
                    "AssistedDialAction.getAssistedDialingTelephonyManager",
                    "createForSubscriptionId pinnedtelephonyManager was null.");
            return telephonyManager;
        }
        LogUtil.i(
                "AssistedDialAction.getAssistedDialingTelephonyManager",
                "createForPhoneAccountHandle using pinnedtelephonyManager from subscription id.");
        return pinnedtelephonyManager;
    }

    @Override
    public void runWithUi(PreCallCoordinator coordinator) {
        runWithoutUi(coordinator.getActivity(), coordinator.getBuilder());
    }

    @Override
    public void onDiscard() {
    }
}
