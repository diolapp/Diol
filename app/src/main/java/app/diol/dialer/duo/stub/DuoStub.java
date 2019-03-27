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

package app.diol.dialer.duo.stub;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.telecom.Call;
import android.telecom.PhoneAccountHandle;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import javax.inject.Inject;

import app.diol.dialer.common.Assert;
import app.diol.dialer.duo.Duo;
import app.diol.dialer.duo.DuoListener;

public class DuoStub implements Duo {

    @Inject
    public DuoStub() {
    }

    @Override
    public boolean isEnabled(@NonNull Context context) {
        return false;
    }

    @Override
    public boolean isInstalled(@NonNull Context context) {
        return false;
    }

    @Override
    public boolean isActivated(@NonNull Context context) {
        return false;
    }

    @MainThread
    @Override
    public boolean isReachable(@NonNull Context context, @Nullable String number) {
        Assert.isMainThread();
        Assert.isNotNull(context);
        return false;
    }

    @MainThread
    @Override
    public Optional<Boolean> supportsUpgrade(
            @NonNull Context context,
            @Nullable String number,
            @Nullable PhoneAccountHandle phoneAccountHandle) {
        Assert.isMainThread();
        Assert.isNotNull(context);
        return Optional.of(false);
    }

    @Override
    public ListenableFuture<ImmutableMap<String, ReachabilityData>> updateReachability(
            @NonNull Context context, @NonNull List<String> numbers) {
        Assert.isMainThread();
        Assert.isNotNull(context);
        Assert.isNotNull(numbers);
        return Futures.immediateFuture(ImmutableMap.of());
    }

    @Override
    public void reloadReachability(@NonNull Context context) {
    }

    @Override
    public Optional<PhoneAccountHandle> getPhoneAccountHandle() {
        return Optional.absent();
    }

    @Override
    public boolean isDuoAccount(PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @Override
    public boolean isDuoAccount(String componentName) {
        return false;
    }

    @MainThread
    @Override
    public Optional<Intent> getCallIntent(@NonNull String number) {
        Assert.isMainThread();
        Assert.isNotNull(number);
        return Optional.absent();
    }

    @Override
    public Optional<Intent> getActivateIntent() {
        return Optional.absent();
    }

    @Override
    public Optional<Intent> getInviteIntent(String number) {
        return Optional.absent();
    }

    @Override
    public Optional<Intent> getInstallDuoIntent() {
        return null;
    }

    @MainThread
    @Override
    public void requestUpgrade(@NonNull Context context, Call call) {
        Assert.isMainThread();
        Assert.isNotNull(call);
    }

    @MainThread
    @Override
    public void registerListener(DuoListener listener) {
        Assert.isMainThread();
        Assert.isNotNull(listener);
    }

    @MainThread
    @Override
    public void unregisterListener(DuoListener listener) {
        Assert.isMainThread();
        Assert.isNotNull(listener);
    }

    @StringRes
    @Override
    public int getOutgoingCallTypeText() {
        return -1;
    }

    @StringRes
    @Override
    public int getIncomingCallTypeText() {
        return -1;
    }

    @DrawableRes
    @Override
    public int getLogo() {
        return -1;
    }
}
