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
package app.diol.voicemail.impl.sync;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;

import app.diol.dialer.common.Assert;
import app.diol.voicemail.impl.OmtpEvents;
import app.diol.voicemail.impl.OmtpVvmCarrierConfigHelper;
import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.VvmLog;

/**
 * Base class for network request call backs for visual voicemail syncing with
 * the Imap server. This handles retries and network requests.
 */
@TargetApi(VERSION_CODES.O)
public abstract class VvmNetworkRequestCallback extends ConnectivityManager.NetworkCallback {

    public static final String NETWORK_REQUEST_FAILED_TIMEOUT = "timeout";
    public static final String NETWORK_REQUEST_FAILED_LOST = "lost";
    private static final String TAG = "VvmNetworkRequest";
    // Timeout used to call ConnectivityManager.requestNetwork
    private static final int NETWORK_REQUEST_TIMEOUT_MILLIS = 60 * 1000;
    private final OmtpVvmCarrierConfigHelper carrierConfigHelper;
    private final VoicemailStatus.Editor status;
    protected Context context;
    protected PhoneAccountHandle phoneAccount;
    protected NetworkRequest networkRequest;
    private ConnectivityManager connectivityManager;
    private boolean requestSent = false;
    private boolean resultReceived = false;
    private boolean released = false;

    public VvmNetworkRequestCallback(Context context, PhoneAccountHandle phoneAccount, VoicemailStatus.Editor status) {
        this.context = context;
        this.phoneAccount = phoneAccount;
        this.status = status;
        carrierConfigHelper = new OmtpVvmCarrierConfigHelper(context, this.phoneAccount);
        networkRequest = createNetworkRequest();
    }

    public VvmNetworkRequestCallback(OmtpVvmCarrierConfigHelper config, PhoneAccountHandle phoneAccount,
                                     VoicemailStatus.Editor status) {
        context = config.getContext();
        this.phoneAccount = phoneAccount;
        this.status = status;
        carrierConfigHelper = config;
        networkRequest = createNetworkRequest();
    }

    public VoicemailStatus.Editor getVoicemailStatusEditor() {
        return status;
    }

    /**
     * @return NetworkRequest for a proper transport type. Use only cellular network
     * if the carrier requires it. Otherwise use whatever available.
     */
    private NetworkRequest createNetworkRequest() {

        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class)
                .createForPhoneAccountHandle(phoneAccount);
        // At this point mPhoneAccount should always be valid and telephonyManager will
        // never be null
        Assert.isNotNull(telephonyManager);
        if (carrierConfigHelper.isCellularDataRequired()) {
            VvmLog.d(TAG, "Transport type: CELLULAR");
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .setNetworkSpecifier(telephonyManager.getNetworkSpecifier());
        } else {
            VvmLog.d(TAG, "Transport type: ANY");
        }
        return builder.build();
    }

    public NetworkRequest getNetworkRequest() {
        return networkRequest;
    }

    @Override
    @CallSuper
    public void onLost(Network network) {
        VvmLog.i(TAG, "onLost");
        resultReceived = true;
        onFailed(NETWORK_REQUEST_FAILED_LOST);
    }

    @Override
    @CallSuper
    public void onAvailable(Network network) {
        super.onAvailable(network);
        resultReceived = true;
    }

    @CallSuper
    public void onUnavailable() {
        VvmLog.i(TAG, "onUnavailable");
        resultReceived = true;
        onFailed(NETWORK_REQUEST_FAILED_TIMEOUT);
    }

    public void requestNetwork() {
        if (requestSent == true) {
            VvmLog.e(TAG, "requestNetwork() called twice");
            return;
        }
        requestSent = true;
        getConnectivityManager().requestNetwork(getNetworkRequest(), this);
        /**
         * Somehow requestNetwork() with timeout doesn't work, and it's a hidden method.
         * Implement our own timeout mechanism instead.
         */
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (resultReceived == false) {
                    onFailed(NETWORK_REQUEST_FAILED_TIMEOUT);
                }
            }
        }, NETWORK_REQUEST_TIMEOUT_MILLIS);
    }

    public void releaseNetwork() {
        VvmLog.i(TAG, "releaseNetwork");
        if (!released) {
            getConnectivityManager().unregisterNetworkCallback(this);
            released = true;
        } else {
            VvmLog.w(TAG, "already released");
        }
    }

    public ConnectivityManager getConnectivityManager() {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return connectivityManager;
    }

    @CallSuper
    public void onFailed(String reason) {
        VvmLog.i(TAG, "onFailed: " + reason);
        if (carrierConfigHelper.isCellularDataRequired()) {
            carrierConfigHelper.handleEvent(status, OmtpEvents.DATA_NO_CONNECTION_CELLULAR_REQUIRED);
        } else {
            carrierConfigHelper.handleEvent(status, OmtpEvents.DATA_NO_CONNECTION);
        }
        releaseNetwork();
    }
}
