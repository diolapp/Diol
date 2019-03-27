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

package app.diol.dialer.callintent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.text.TextUtils;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.InvalidProtocolBufferException;

import app.diol.dialer.common.Assert;
import app.diol.dialer.performancereport.PerformanceReport;
import app.diol.dialer.util.CallUtil;

/**
 * Creates an intent to start a new outgoing call.
 */
@AutoValue
public abstract class CallIntent implements Parcelable {
    // @TODO(justinmcclain): Investigate deleting the parcelable logic and instead switching
    // to using an internal proto for serialization.
    public static final Creator<CallIntent> CREATOR =
            new Creator<CallIntent>() {
                @Override
                public CallIntent createFromParcel(Parcel source) {
                    CallIntent.Builder callIntentBuilder = builder();
                    ClassLoader classLoader = CallIntent.class.getClassLoader();
                    callIntentBuilder.setNumber(source.readParcelable(classLoader));
                    CallSpecificAppData data;
                    try {
                        data = CallSpecificAppData.parseFrom(source.createByteArray());
                    } catch (InvalidProtocolBufferException e) {
                        data = CallSpecificAppData.getDefaultInstance();
                    }
                    callIntentBuilder
                            .setCallSpecificAppData(data)
                            .setPhoneAccountHandle(source.readParcelable(classLoader))
                            .setIsVideoCall(source.readInt() != 0)
                            .setCallSubject(source.readString())
                            .setAllowAssistedDial(source.readInt() != 0);
                    Bundle stringInCallUiIntentExtrasBundle = source.readBundle(classLoader);
                    for (String key : stringInCallUiIntentExtrasBundle.keySet()) {
                        callIntentBuilder.addInCallUiIntentExtra(
                                key, stringInCallUiIntentExtrasBundle.getString(key));
                    }
                    Bundle longInCallUiIntentExtrasBundle = source.readBundle(classLoader);
                    for (String key : longInCallUiIntentExtrasBundle.keySet()) {
                        callIntentBuilder.addInCallUiIntentExtra(
                                key, longInCallUiIntentExtrasBundle.getLong(key));
                    }
                    return callIntentBuilder.autoBuild();
                }

                @Override
                public CallIntent[] newArray(int size) {
                    return new CallIntent[0];
                }
            };
    private static int lightbringerButtonAppearInExpandedCallLogItemCount = 0;
    private static int lightbringerButtonAppearInCollapsedCallLogItemCount = 0;
    private static int lightbringerButtonAppearInSearchCount = 0;

    public static Builder builder() {
        return new AutoValue_CallIntent.Builder().setIsVideoCall(false).setAllowAssistedDial(false);
    }

    public static void increaseLightbringerCallButtonAppearInExpandedCallLogItemCount() {
        CallIntent.lightbringerButtonAppearInExpandedCallLogItemCount++;
    }

    public static void increaseLightbringerCallButtonAppearInCollapsedCallLogItemCount() {
        CallIntent.lightbringerButtonAppearInCollapsedCallLogItemCount++;
    }

    public static void increaseLightbringerCallButtonAppearInSearchCount() {
        CallIntent.lightbringerButtonAppearInSearchCount++;
    }

    @VisibleForTesting
    public static int getLightbringerButtonAppearInExpandedCallLogItemCount() {
        return lightbringerButtonAppearInExpandedCallLogItemCount;
    }

    @VisibleForTesting
    public static int getLightbringerButtonAppearInCollapsedCallLogItemCount() {
        return lightbringerButtonAppearInCollapsedCallLogItemCount;
    }

    @VisibleForTesting
    public static int getLightbringerButtonAppearInSearchCount() {
        return lightbringerButtonAppearInSearchCount;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void clearLightbringerCounts() {
        lightbringerButtonAppearInCollapsedCallLogItemCount = 0;
        lightbringerButtonAppearInExpandedCallLogItemCount = 0;
        lightbringerButtonAppearInSearchCount = 0;
    }

    abstract Uri number();

    abstract CallSpecificAppData callSpecificAppData();

    @Nullable
    abstract PhoneAccountHandle phoneAccountHandle();

    abstract boolean isVideoCall();

    @Nullable
    abstract String callSubject();

    abstract boolean allowAssistedDial();

    abstract ImmutableMap<String, String> stringInCallUiIntentExtras();

    abstract ImmutableMap<String, Long> longInCallUiIntentExtras();

    abstract ImmutableMap<String, String> stringPlaceCallExtras();

    abstract ImmutableMap<String, Long> longPlaceCallExtras();

    public abstract Builder toBuilder();

    // Creates the intent which can start a call
    private Intent newIntent() {
        Intent intent = new Intent(Intent.ACTION_CALL, number());

        intent.putExtra(
                TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE,
                isVideoCall() ? VideoProfile.STATE_BIDIRECTIONAL : VideoProfile.STATE_AUDIO_ONLY);

        Bundle inCallUiIntentExtras = createInCallUiIntentExtras();
        inCallUiIntentExtras.putLong(
                Constants.EXTRA_CALL_CREATED_TIME_MILLIS, SystemClock.elapsedRealtime());

        intent.putExtra(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, inCallUiIntentExtras);

        if (phoneAccountHandle() != null) {
            intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle());
        }

        if (!TextUtils.isEmpty(callSubject())) {
            intent.putExtra(TelecomManager.EXTRA_CALL_SUBJECT, callSubject());
        }

        intent.putExtras(createPlaceCallExtras());

        return intent;
    }

    private Bundle createInCallUiIntentExtras() {
        Bundle bundle = new Bundle();
        stringInCallUiIntentExtras().forEach(bundle::putString);
        longInCallUiIntentExtras().forEach(bundle::putLong);
        CallIntentParser.putCallSpecificAppData(bundle, callSpecificAppData());
        return bundle;
    }

    private Bundle createPlaceCallExtras() {
        Bundle bundle = new Bundle();
        stringPlaceCallExtras().forEach(bundle::putString);
        longPlaceCallExtras().forEach(bundle::putLong);
        CallIntentParser.putCallSpecificAppData(bundle, callSpecificAppData());
        return bundle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(number(), flags);
        dest.writeByteArray(callSpecificAppData().toByteArray());
        dest.writeParcelable(phoneAccountHandle(), flags);
        dest.writeInt(isVideoCall() ? 1 : 0);
        dest.writeString(callSubject());
        dest.writeInt(allowAssistedDial() ? 1 : 0);
        Bundle stringInCallUiIntentExtrasBundle = new Bundle();
        stringInCallUiIntentExtras().forEach(stringInCallUiIntentExtrasBundle::putString);
        dest.writeBundle(stringInCallUiIntentExtrasBundle);
        Bundle longInCallUiIntentExtrasBundle = new Bundle();
        longInCallUiIntentExtras().forEach(longInCallUiIntentExtrasBundle::putLong);
        dest.writeBundle(longInCallUiIntentExtrasBundle);
    }

    /**
     * Builder class for CallIntent info.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public Builder setTelNumber(String number) {
            return setNumber(CallUtil.getCallUri(Assert.isNotNull(number)));
        }

        public Builder setVoicemailNumber(@Nullable PhoneAccountHandle phoneAccountHandle) {
            return setNumber(Uri.fromParts(PhoneAccount.SCHEME_VOICEMAIL, "", null))
                    .setPhoneAccountHandle(phoneAccountHandle);
        }

        public abstract Builder setNumber(@NonNull Uri number);

        public Builder setCallInitiationType(CallInitiationType.Type callInitiationType) {
            return setCallSpecificAppData(
                    CallSpecificAppData.newBuilder().setCallInitiationType(callInitiationType).build());
        }

        abstract CallSpecificAppData callSpecificAppData();

        public abstract Builder setCallSpecificAppData(
                @NonNull CallSpecificAppData callSpecificAppData);

        public abstract Builder setPhoneAccountHandle(PhoneAccountHandle phoneAccountHandle);

        public abstract Builder setIsVideoCall(boolean isVideoCall);

        public abstract Builder setCallSubject(String callSubject);

        public abstract Builder setAllowAssistedDial(boolean allowAssistedDial);

        abstract ImmutableMap.Builder<String, String> stringInCallUiIntentExtrasBuilder();

        abstract ImmutableMap.Builder<String, Long> longInCallUiIntentExtrasBuilder();

        public Builder addInCallUiIntentExtra(String key, String value) {
            stringInCallUiIntentExtrasBuilder().put(key, value);
            return this;
        }

        public Builder addInCallUiIntentExtra(String key, Long value) {
            longInCallUiIntentExtrasBuilder().put(key, value);
            return this;
        }

        abstract ImmutableMap.Builder<String, String> stringPlaceCallExtrasBuilder();

        abstract ImmutableMap.Builder<String, Long> longPlaceCallExtrasBuilder();

        public Builder addPlaceCallExtra(String key, String value) {
            stringPlaceCallExtrasBuilder().put(key, value);
            return this;
        }

        public Builder addPlaceCallExtra(String key, Long value) {
            longPlaceCallExtrasBuilder().put(key, value);
            return this;
        }

        abstract CallIntent autoBuild();

        public Intent build() {
            CallSpecificAppData.Builder builder =
                    CallSpecificAppData.newBuilder(callSpecificAppData())
                            .setLightbringerButtonAppearInExpandedCallLogItemCount(
                                    lightbringerButtonAppearInExpandedCallLogItemCount)
                            .setLightbringerButtonAppearInCollapsedCallLogItemCount(
                                    lightbringerButtonAppearInCollapsedCallLogItemCount)
                            .setLightbringerButtonAppearInSearchCount(lightbringerButtonAppearInSearchCount);
            lightbringerButtonAppearInExpandedCallLogItemCount = 0;
            lightbringerButtonAppearInCollapsedCallLogItemCount = 0;
            lightbringerButtonAppearInSearchCount = 0;

            if (PerformanceReport.isRecording()) {
                builder
                        .setTimeSinceAppLaunch(PerformanceReport.getTimeSinceAppLaunch())
                        .setTimeSinceFirstClick(PerformanceReport.getTimeSinceFirstClick())
                        .addAllUiActionsSinceAppLaunch(PerformanceReport.getActions())
                        .addAllUiActionTimestampsSinceAppLaunch(PerformanceReport.getActionTimestamps())
                        .setStartingTabIndex(PerformanceReport.getStartingTabIndex())
                        .build();
                PerformanceReport.stopRecording();
            }

            setCallSpecificAppData(builder.build());

            // Validate CallIntent.
            CallIntent callIntent = autoBuild();
            Assert.isNotNull(callIntent.number());
            Assert.isNotNull(callIntent.callSpecificAppData());
            Assert.checkArgument(
                    callIntent.callSpecificAppData().getCallInitiationType()
                            != CallInitiationType.Type.UNKNOWN_INITIATION);

            return autoBuild().newIntent();
        }
    }
}
