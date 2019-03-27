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

package app.diol.incallui.videotech.duo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telecom.Call;
import android.telecom.PhoneAccountHandle;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DefaultFutureCallback;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.duo.Duo;
import app.diol.dialer.duo.DuoListener;
import app.diol.dialer.logging.DialerImpression;
import app.diol.incallui.video.protocol.VideoCallScreen;
import app.diol.incallui.video.protocol.VideoCallScreenDelegate;
import app.diol.incallui.videotech.VideoTech;
import app.diol.incallui.videotech.utils.SessionModificationState;

public class DuoVideoTech implements VideoTech, DuoListener {
    private final Duo duo;
    private final VideoTechListener listener;
    private final Call call;
    private final String callingNumber;
    private int callState = Call.STATE_NEW;
    private boolean isRemoteUpgradeAvailabilityQueried;

    public DuoVideoTech(
            @NonNull Duo duo,
            @NonNull VideoTechListener listener,
            @NonNull Call call,
            @NonNull String callingNumber) {
        this.duo = Assert.isNotNull(duo);
        this.listener = Assert.isNotNull(listener);
        this.call = Assert.isNotNull(call);
        this.callingNumber = Assert.isNotNull(callingNumber);

        duo.registerListener(this);
    }

    @Override
    public boolean isAvailable(Context context, PhoneAccountHandle phoneAccountHandle) {
        if (!ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getBoolean("enable_lightbringer_video_upgrade", true)) {
            LogUtil.v("DuoVideoTech.isAvailable", "upgrade disabled by flag");
            return false;
        }

        if (callState != Call.STATE_ACTIVE) {
            LogUtil.v("DuoVideoTech.isAvailable", "upgrade unavailable, call must be active");
            return false;
        }
        Optional<Boolean> localResult = duo.supportsUpgrade(context, callingNumber, phoneAccountHandle);
        if (localResult.isPresent()) {
            LogUtil.v(
                    "DuoVideoTech.isAvailable", "upgrade supported in local cache: " + localResult.get());
            return localResult.get();
        }

        if (!isRemoteUpgradeAvailabilityQueried) {
            LogUtil.v("DuoVideoTech.isAvailable", "reachability unknown, starting remote query");
            isRemoteUpgradeAvailabilityQueried = true;
            Futures.addCallback(
                    duo.updateReachability(context, ImmutableList.of(callingNumber)),
                    new DefaultFutureCallback<>(),
                    MoreExecutors.directExecutor());
        }

        return false;
    }

    @Override
    public boolean isTransmittingOrReceiving() {
        return false;
    }

    @Override
    public boolean isSelfManagedCamera() {
        return false;
    }

    @Override
    public boolean shouldUseSurfaceView() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public VideoCallScreenDelegate createVideoCallScreenDelegate(
            Context context, VideoCallScreen videoCallScreen) {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public void onCallStateChanged(
            Context context, int newState, PhoneAccountHandle phoneAccountHandle) {
        if (newState == Call.STATE_DISCONNECTING) {
            duo.unregisterListener(this);
        }

        callState = newState;
    }

    @Override
    public void onRemovedFromCallList() {
        duo.unregisterListener(this);
    }

    @Override
    public int getSessionModificationState() {
        return SessionModificationState.NO_REQUEST;
    }

    @Override
    public void upgradeToVideo(@NonNull Context context) {
        listener.onImpressionLoggingNeeded(DialerImpression.Type.LIGHTBRINGER_UPGRADE_REQUESTED);
        duo.requestUpgrade(context, call);
    }

    @Override
    public void acceptVideoRequest(@NonNull Context context) {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public void acceptVideoRequestAsAudio() {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public void declineVideoRequest() {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public boolean isTransmitting() {
        return false;
    }

    @Override
    public void stopTransmission() {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public void resumeTransmission(@NonNull Context context) {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
    }

    @Override
    public void setCamera(@Nullable String cameraId) {
        throw Assert.createUnsupportedOperationFailException();
    }

    @Override
    public void becomePrimary() {
        listener.onImpressionLoggingNeeded(
                DialerImpression.Type.UPGRADE_TO_VIDEO_CALL_BUTTON_SHOWN_FOR_LIGHTBRINGER);
    }

    @Override
    public void setDeviceOrientation(int rotation) {
    }

    @Override
    public void onDuoStateChanged() {
        listener.onVideoTechStateChanged();
    }

    @Override
    public app.diol.dialer.logging.VideoTech.Type getVideoTechType() {
        return app.diol.dialer.logging.VideoTech.Type.LIGHTBRINGER_VIDEO_TECH;
    }
}
