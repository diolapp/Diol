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
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;

import java.util.List;

import javax.inject.Inject;

import app.diol.contacts.common.widget.SelectPhoneAccountDialogFragment;
import app.diol.contacts.common.widget.SelectPhoneAccountDialogFragment.SelectPhoneAccountListener;
import app.diol.contacts.common.widget.SelectPhoneAccountDialogOptions;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.logging.DialerImpression.Type;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.precall.PreCallAction;
import app.diol.dialer.precall.PreCallCoordinator;
import app.diol.dialer.precall.PreCallCoordinator.PendingAction;
import app.diol.dialer.preferredsim.PreferredAccountRecorder;
import app.diol.dialer.preferredsim.PreferredAccountWorker;
import app.diol.dialer.preferredsim.suggestion.SuggestionProvider;
import app.diol.dialer.preferredsim.suggestion.SuggestionProvider.Suggestion;

/**
 * PreCallAction to select which phone account to call with. Ignored if there's only one account
 */
@SuppressWarnings("MissingPermission")
public class CallingAccountSelector implements PreCallAction {

    @VisibleForTesting
    static final String TAG_CALLING_ACCOUNT_SELECTOR = "CallingAccountSelector";
    private final PreferredAccountWorker preferredAccountWorker;
    private SelectPhoneAccountDialogFragment selectPhoneAccountDialogFragment;
    private boolean isDiscarding;

    @Inject
    CallingAccountSelector(PreferredAccountWorker preferredAccountWorker) {
        this.preferredAccountWorker = preferredAccountWorker;
    }

    @Override
    public boolean requiresUi(Context context, CallIntentBuilder builder) {
        if (!ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getBoolean("precall_calling_account_selector_enabled", true)) {
            return false;
        }

        if (builder.getPhoneAccountHandle() != null) {
            return false;
        }
        if (PhoneNumberUtils.isEmergencyNumber(builder.getUri().getSchemeSpecificPart())) {
            return false;
        }

        TelecomManager telecomManager = context.getSystemService(TelecomManager.class);
        List<PhoneAccountHandle> accounts = telecomManager.getCallCapablePhoneAccounts();
        if (accounts.size() <= 1) {
            return false;
        }
        return true;
    }

    @Override
    public void runWithoutUi(Context context, CallIntentBuilder builder) {
        // do nothing.
    }

    @Override
    public void runWithUi(PreCallCoordinator coordinator) {
        CallIntentBuilder builder = coordinator.getBuilder();
        if (!requiresUi(coordinator.getActivity(), builder)) {
            return;
        }
        switch (builder.getUri().getScheme()) {
            case PhoneAccount.SCHEME_VOICEMAIL:
                showDialog(
                        coordinator,
                        coordinator.startPendingAction(),
                        preferredAccountWorker.getVoicemailDialogOptions(),
                        null,
                        null,
                        null);
                Logger.get(coordinator.getActivity()).logImpression(Type.DUAL_SIM_SELECTION_VOICEMAIL);
                break;
            case PhoneAccount.SCHEME_TEL:
                processPreferredAccount(coordinator);
                break;
            default:
                // might be PhoneAccount.SCHEME_SIP
                LogUtil.e(
                        "CallingAccountSelector.run",
                        "unable to process scheme " + builder.getUri().getScheme());
                break;
        }
    }

    /**
     * Initiates a background worker to find if there's any preferred account.
     */
    @MainThread
    private void processPreferredAccount(PreCallCoordinator coordinator) {
        Assert.isMainThread();
        CallIntentBuilder builder = coordinator.getBuilder();
        AppCompatActivity activity = coordinator.getActivity();
        String phoneNumber = builder.getUri().getSchemeSpecificPart();
        PendingAction pendingAction = coordinator.startPendingAction();

        coordinator.listen(
                preferredAccountWorker.selectAccount(
                        phoneNumber,
                        activity.getSystemService(TelecomManager.class).getCallCapablePhoneAccounts()),
                result -> {
                    if (isDiscarding) {
                        // pendingAction is dropped by the coordinator before onDiscard is triggered.
                        return;
                    }
                    if (result.getSelectedPhoneAccountHandle().isPresent()) {

                        if (result.getSuggestion().isPresent()
                                && result
                                .getSelectedPhoneAccountHandle()
                                .get()
                                .equals(result.getSuggestion().get().phoneAccountHandle)) {
                            builder
                                    .getInCallUiIntentExtras()
                                    .putString(
                                            SuggestionProvider.EXTRA_SIM_SUGGESTION_REASON,
                                            result.getSuggestion().get().reason.name());
                        }

                        coordinator
                                .getBuilder()
                                .setPhoneAccountHandle(result.getSelectedPhoneAccountHandle().get());
                        pendingAction.finish();
                        return;
                    }
                    showDialog(
                            coordinator,
                            pendingAction,
                            result.getDialogOptionsBuilder().get().build(),
                            result.getDataId().orNull(),
                            phoneNumber,
                            result.getSuggestion().orNull());
                },
                (throwable) -> {
                    throw new RuntimeException(throwable);
                });
    }

    @MainThread
    private void showDialog(
            PreCallCoordinator coordinator,
            PendingAction pendingAction,
            SelectPhoneAccountDialogOptions dialogOptions,
            @Nullable String dataId,
            @Nullable String number,
            @Nullable Suggestion suggestion) {
        Assert.isMainThread();

        selectPhoneAccountDialogFragment =
                SelectPhoneAccountDialogFragment.newInstance(
                        dialogOptions,
                        new SelectedListener(
                                coordinator,
                                pendingAction,
                                new PreferredAccountRecorder(number, suggestion, dataId)));
        selectPhoneAccountDialogFragment.show(
                coordinator.getActivity().getSupportFragmentManager(), TAG_CALLING_ACCOUNT_SELECTOR);
    }

    @MainThread
    @Override
    public void onDiscard() {
        isDiscarding = true;
        if (selectPhoneAccountDialogFragment != null) {
            selectPhoneAccountDialogFragment.dismiss();
        }
    }

    private class SelectedListener extends SelectPhoneAccountListener {

        private final PreCallCoordinator coordinator;
        private final PreCallCoordinator.PendingAction listener;
        private final PreferredAccountRecorder recorder;

        public SelectedListener(
                @NonNull PreCallCoordinator builder,
                @NonNull PreCallCoordinator.PendingAction listener,
                @NonNull PreferredAccountRecorder recorder) {
            this.coordinator = Assert.isNotNull(builder);
            this.listener = Assert.isNotNull(listener);
            this.recorder = Assert.isNotNull(recorder);
        }

        @MainThread
        @Override
        public void onPhoneAccountSelected(
                PhoneAccountHandle selectedAccountHandle, boolean setDefault, @Nullable String callId) {
            coordinator.getBuilder().setPhoneAccountHandle(selectedAccountHandle);
            recorder.record(coordinator.getActivity(), selectedAccountHandle, setDefault);
            listener.finish();
        }

        @MainThread
        @Override
        public void onDialogDismissed(@Nullable String callId) {
            if (isDiscarding) {
                return;
            }
            coordinator.abortCall();
            listener.finish();
        }
    }
}
