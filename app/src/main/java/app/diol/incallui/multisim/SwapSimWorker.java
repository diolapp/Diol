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

package app.diol.incallui.multisim;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.ThreadUtil;
import app.diol.dialer.preferredsim.suggestion.SimSuggestionComponent;
import app.diol.dialer.util.PermissionsUtil;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.DialerCallListener;
import app.diol.incallui.incalluilock.InCallUiLock;

/**
 * Hangs up the current call and redial the call using the {@code otherAccount} instead. the in call
 * ui will be prevented from closing until the process has finished.
 */
public class SwapSimWorker implements Worker<Void, Void>, DialerCallListener, CallList.Listener {

    // Timeout waiting for the call to hangup or redial.
    private static final int DEFAULT_TIMEOUT_MILLIS = 5_000;

    private final Context context;
    private final DialerCall call;
    private final CallList callList;
    private final InCallUiLock inCallUiLock;

    private final CountDownLatch disconnectLatch = new CountDownLatch(1);
    private final CountDownLatch dialingLatch = new CountDownLatch(1);

    private final PhoneAccountHandle otherAccount;
    private final String number;

    private final int timeoutMillis;

    private CountDownLatch latchForTest;

    @MainThread
    public SwapSimWorker(
            Context context,
            DialerCall call,
            CallList callList,
            PhoneAccountHandle otherAccount,
            InCallUiLock lock) {
        this(context, call, callList, otherAccount, lock, DEFAULT_TIMEOUT_MILLIS);
    }

    @VisibleForTesting
    SwapSimWorker(
            Context context,
            DialerCall call,
            CallList callList,
            PhoneAccountHandle otherAccount,
            InCallUiLock lock,
            int timeoutMillis) {
        Assert.isMainThread();
        this.context = context;
        this.call = call;
        this.callList = callList;
        this.otherAccount = otherAccount;
        inCallUiLock = lock;
        this.timeoutMillis = timeoutMillis;
        number = call.getNumber();
        call.addListener(this);
        call.disconnect();
    }

    @WorkerThread
    @Nullable
    @Override
    @SuppressWarnings("MissingPermission")
    public Void doInBackground(Void unused) {
        try {
            SimSuggestionComponent.get(context)
                    .getSuggestionProvider()
                    .reportIncorrectSuggestion(context, number, otherAccount);

            if (!PermissionsUtil.hasPhonePermissions(context)) {
                LogUtil.e("SwapSimWorker.doInBackground", "missing phone permission");
                return null;
            }
            if (!disconnectLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                LogUtil.e("SwapSimWorker.doInBackground", "timeout waiting for call to disconnect");
                return null;
            }
            LogUtil.i("SwapSimWorker.doInBackground", "call disconnected, redialing");
            TelecomManager telecomManager = context.getSystemService(TelecomManager.class);
            Bundle extras = new Bundle();
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, otherAccount);
            callList.addListener(this);
            telecomManager.placeCall(Uri.fromParts(PhoneAccount.SCHEME_TEL, number, null), extras);
            if (latchForTest != null) {
                latchForTest.countDown();
            }
            if (!dialingLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                LogUtil.e("SwapSimWorker.doInBackground", "timeout waiting for call to dial");
            }
            return null;
        } catch (InterruptedException e) {
            LogUtil.e("SwapSimWorker.doInBackground", "interrupted", e);
            Thread.currentThread().interrupt();
            return null;
        } finally {
            ThreadUtil.postOnUiThread(
                    () -> {
                        call.removeListener(this);
                        callList.removeListener(this);
                        inCallUiLock.release();
                    });
        }
    }

    @MainThread
    @Override
    public void onDialerCallDisconnect() {
        disconnectLatch.countDown();
    }

    @Override
    public void onCallListChange(CallList callList) {
        if (callList.getOutgoingCall() != null) {
            dialingLatch.countDown();
        }
    }

    @VisibleForTesting
    void setLatchForTest(CountDownLatch latch) {
        latchForTest = latch;
    }

    @Override
    public void onDialerCallUpdate() {
    }

    @Override
    public void onDialerCallChildNumberChange() {
    }

    @Override
    public void onDialerCallLastForwardedNumberChange() {
    }

    @Override
    public void onDialerCallUpgradeToVideo() {
    }

    @Override
    public void onDialerCallSessionModificationStateChange() {
    }

    @Override
    public void onWiFiToLteHandover() {
    }

    @Override
    public void onHandoverToWifiFailure() {
    }

    @Override
    public void onInternationalCallOnWifi() {
    }

    @Override
    public void onEnrichedCallSessionUpdate() {
    }

    @Override
    public void onIncomingCall(DialerCall call) {
    }

    @Override
    public void onUpgradeToVideo(DialerCall call) {
    }

    @Override
    public void onSessionModificationStateChange(DialerCall call) {
    }

    @Override
    public void onDisconnect(DialerCall call) {
    }

    @Override
    public void onWiFiToLteHandover(DialerCall call) {
    }

    @Override
    public void onHandoverToWifiFailed(DialerCall call) {
    }

    @Override
    public void onInternationalCallOnWifi(@NonNull DialerCall call) {
    }
}
