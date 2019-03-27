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
import android.net.Network;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.telecom.PhoneAccountHandle;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import app.diol.voicemail.impl.OmtpVvmCarrierConfigHelper;
import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.VvmLog;

/**
 * Class to retrieve a {@link Network} synchronously.
 * {@link #getNetwork(OmtpVvmCarrierConfigHelper, PhoneAccountHandle)} will
 * block until a suitable network is retrieved or it has failed.
 */
@TargetApi(VERSION_CODES.O)
public class VvmNetworkRequest {

    private static final String TAG = "VvmNetworkRequest";

    @NonNull
    public static NetworkWrapper getNetwork(OmtpVvmCarrierConfigHelper config, PhoneAccountHandle handle,
                                            VoicemailStatus.Editor status) throws RequestFailedException {
        FutureNetworkRequestCallback callback = new FutureNetworkRequestCallback(config, handle, status);
        callback.requestNetwork();
        try {
            return callback.getFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            callback.releaseNetwork();
            VvmLog.e(TAG, "can't get future network", e);
            throw new RequestFailedException(e);
        }
    }

    /**
     * A wrapper around a Network returned by a {@link VvmNetworkRequestCallback},
     * which should be closed once not needed anymore.
     */
    public static class NetworkWrapper implements Closeable {

        private final Network network;
        private final VvmNetworkRequestCallback callback;

        private NetworkWrapper(Network network, VvmNetworkRequestCallback callback) {
            this.network = network;
            this.callback = callback;
        }

        public Network get() {
            return network;
        }

        @Override
        public void close() {
            callback.releaseNetwork();
        }
    }

    public static class RequestFailedException extends Exception {

        private RequestFailedException(Throwable cause) {
            super(cause);
        }
    }

    private static class FutureNetworkRequestCallback extends VvmNetworkRequestCallback {

        /**
         * {@link CompletableFuture#get()} will block until
         * {@link CompletableFuture# complete(Object) } has been called on the other
         * thread.
         */
        private final CompletableFuture<NetworkWrapper> future = new CompletableFuture<>();

        public FutureNetworkRequestCallback(OmtpVvmCarrierConfigHelper config, PhoneAccountHandle phoneAccount,
                                            VoicemailStatus.Editor status) {
            super(config, phoneAccount, status);
        }

        public Future<NetworkWrapper> getFuture() {
            return future;
        }

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            future.complete(new NetworkWrapper(network, this));
        }

        @Override
        public void onFailed(String reason) {
            super.onFailed(reason);
            future.complete(null);
        }
    }
}
