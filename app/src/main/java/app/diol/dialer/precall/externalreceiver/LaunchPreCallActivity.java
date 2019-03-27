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

package app.diol.dialer.precall.externalreceiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;

import com.google.common.collect.ImmutableList;

import app.diol.dialer.callintent.CallInitiationType.Type;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProvider;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.precall.PreCall;

/**
 * Activity that forwards to {@link PreCall#start(Context, CallIntentBuilder)} so the pre-call flow
 * can be initiated by external apps. This activity is exported but can only be started by apps with
 * {@link android.Manifest.permission#CALL_PHONE}. Keyguard will be triggered if phone is locked.
 *
 * @see CallIntentBuilder
 */
public class LaunchPreCallActivity extends AppCompatActivity {

    public static final String ACTION_LAUNCH_PRE_CALL = "com.android.dialer.LAUNCH_PRE_CALL";

    public static final String EXTRA_PHONE_ACCOUNT_HANDLE = "phone_account_handle";

    public static final String EXTRA_IS_VIDEO_CALL = "is_video_call";

    public static final String EXTRA_CALL_SUBJECT = "call_subject";

    public static final String EXTRA_ALLOW_ASSISTED_DIAL = "allow_assisted_dial";

    private static final ImmutableList<String> HANDLED_INTENT_EXTRAS =
            ImmutableList.of(
                    TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE,
                    TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS,
                    TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
                    TelecomManager.EXTRA_CALL_SUBJECT,
                    EXTRA_PHONE_ACCOUNT_HANDLE,
                    EXTRA_IS_VIDEO_CALL,
                    EXTRA_CALL_SUBJECT,
                    EXTRA_ALLOW_ASSISTED_DIAL);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.get(this).logImpression(DialerImpression.Type.PRECALL_INITIATED_EXTERNAL);

        ConfigProvider configProvider =
                ConfigProviderComponent.get(getApplicationContext()).getConfigProvider();
        Intent intent = getIntent();
        CallIntentBuilder builder = new CallIntentBuilder(intent.getData(), Type.EXTERNAL_INITIATION);

        PhoneAccountHandle phoneAccountHandle = intent.getParcelableExtra(EXTRA_PHONE_ACCOUNT_HANDLE);
        if (phoneAccountHandle == null) {
            phoneAccountHandle = intent.getParcelableExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE);
        }

        builder
                .setPhoneAccountHandle(phoneAccountHandle)
                .setIsVideoCall(intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false))
                .setCallSubject(intent.getStringExtra(EXTRA_CALL_SUBJECT))
                .setAllowAssistedDial(
                        intent.getBooleanExtra(
                                EXTRA_ALLOW_ASSISTED_DIAL,
                                configProvider.getBoolean("assisted_dialing_default_precall_state", false)));
        filterExtras(intent.getExtras(), builder);
        PreCall.start(this, builder);
        finish();
    }

    /**
     * Move key-value pairs that {@link CallIntentBuilder} can handle from {@code intentExtras} to
     * {@code builder}
     */
    private void filterExtras(@Nullable Bundle intentExtras, CallIntentBuilder builder) {
        if (intentExtras == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putAll(intentExtras);

        if (intentExtras.containsKey(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE)) {
            int videoState = intentExtras.getInt(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE);
            switch (videoState) {
                case VideoProfile.STATE_BIDIRECTIONAL:
                    builder.setIsVideoCall(true);
                    break;
                case VideoProfile.STATE_AUDIO_ONLY:
                    builder.setIsVideoCall(false);
                    break;
                case VideoProfile.STATE_RX_ENABLED:
                case VideoProfile.STATE_TX_ENABLED:
                    LogUtil.w(
                            "LaunchPreCallActivity.filterExtras",
                            "unsupported video state " + videoState + ", overriding to STATE_BIDIRECTIONAL");
                    builder.setIsVideoCall(true);
                    break;
                default:
                    LogUtil.w("LaunchPreCallActivity.filterExtras", "unknown video state " + videoState);
                    builder.setIsVideoCall(false);
            }
        }

        if (intentExtras.containsKey(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS)) {
            builder
                    .getInCallUiIntentExtras()
                    .putAll(intentExtras.getBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS));
        }

        if (intentExtras.containsKey(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE)) {
            builder.setPhoneAccountHandle(
                    intentExtras.getParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE));
        }

        if (intentExtras.containsKey(TelecomManager.EXTRA_CALL_SUBJECT)) {
            builder.setCallSubject(intentExtras.getString(TelecomManager.EXTRA_CALL_SUBJECT));
        }

        for (String handledKey : HANDLED_INTENT_EXTRAS) {
            bundle.remove(handledKey);
        }
        builder.getPlaceCallExtras().putAll(bundle);
    }
}
