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
package app.diol.voicemail.impl.fetch;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Network;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.provider.VoicemailContract;
import android.provider.VoicemailContract.Voicemails;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.BuildCompat;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.imap.ImapHelper;
import app.diol.voicemail.impl.imap.ImapHelper.InitializingException;
import app.diol.voicemail.impl.sync.VvmAccountManager;
import app.diol.voicemail.impl.sync.VvmNetworkRequestCallback;

/**
 * handles {@link VoicemailContract#ACTION_FETCH_VOICEMAIL}
 */
@TargetApi(VERSION_CODES.O)
public class FetchVoicemailReceiver extends BroadcastReceiver {

    public static final int SOURCE_DATA = 0;
    public static final int PHONE_ACCOUNT_ID = 1;
    public static final int PHONE_ACCOUNT_COMPONENT_NAME = 2;
    static final String[] PROJECTION = new String[]{Voicemails.SOURCE_DATA, // 0
            Voicemails.PHONE_ACCOUNT_ID, // 1
            Voicemails.PHONE_ACCOUNT_COMPONENT_NAME, // 2
    };
    private static final String TAG = "FetchVoicemailReceiver";
    // Number of retries
    private static final int NETWORK_RETRY_COUNT = 3;

    private ContentResolver contentResolver;
    private Uri uri;
    private VvmNetworkRequestCallback networkCallback;
    private Context context;
    private String uid;
    private PhoneAccountHandle phoneAccount;
    private int retryCount = NETWORK_RETRY_COUNT;

    /**
     * In ag/930496 the format of PhoneAccountHandle has changed between Marshmallow
     * and Nougat. This method attempts to search the account from the old database
     * in registered sources using the old format. There's a chance of M phone
     * account collisions on multi-SIM devices, but visual voicemail is not
     * supported on M multi-SIM.
     */
    @Nullable
    private static PhoneAccountHandle getAccountFromMarshmallowAccount(Context context, PhoneAccountHandle oldAccount) {
        if (!BuildCompat.isAtLeastN()) {
            return null;
        }
        for (PhoneAccountHandle handle : context.getSystemService(TelecomManager.class).getCallCapablePhoneAccounts()) {
            if (getIccSerialNumberFromFullIccSerialNumber(handle.getId()).equals(oldAccount.getId())) {
                return handle;
            }
        }
        return null;
    }

    /**
     * getIccSerialNumber() is used for ID before N, and getFullIccSerialNumber()
     * after. getIccSerialNumber() stops at the first hex char.
     */
    @NonNull
    private static String getIccSerialNumberFromFullIccSerialNumber(@NonNull String id) {
        for (int i = 0; i < id.length(); i++) {
            if (!Character.isDigit(id.charAt(i))) {
                return id.substring(0, i);
            }
        }
        return id;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!VoicemailComponent.get(context).getVoicemailClient().isVoicemailModuleEnabled()) {
            return;
        }
        if (VoicemailContract.ACTION_FETCH_VOICEMAIL.equals(intent.getAction())) {
            VvmLog.i(TAG, "ACTION_FETCH_VOICEMAIL received");
            this.context = context;
            contentResolver = context.getContentResolver();
            uri = intent.getData();

            if (uri == null) {
                VvmLog.w(TAG, VoicemailContract.ACTION_FETCH_VOICEMAIL + " intent sent with no data");
                return;
            }

            if (!context.getPackageName().equals(uri.getQueryParameter(VoicemailContract.PARAM_KEY_SOURCE_PACKAGE))) {
                // Ignore if the fetch request is for a voicemail not from this package.
                VvmLog.e(TAG, "ACTION_FETCH_VOICEMAIL from foreign pacakge " + context.getPackageName());
                return;
            }

            Cursor cursor = contentResolver.query(uri, PROJECTION, null, null, null);
            if (cursor == null) {
                VvmLog.i(TAG, "ACTION_FETCH_VOICEMAIL query returned null");
                return;
            }
            try {
                if (cursor.moveToFirst()) {
                    uid = cursor.getString(SOURCE_DATA);
                    String accountId = cursor.getString(PHONE_ACCOUNT_ID);
                    if (TextUtils.isEmpty(accountId)) {
                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        accountId = telephonyManager.getSimSerialNumber();

                        if (TextUtils.isEmpty(accountId)) {
                            VvmLog.e(TAG, "Account null and no default sim found.");
                            return;
                        }
                    }

                    phoneAccount = new PhoneAccountHandle(
                            ComponentName.unflattenFromString(cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME)),
                            cursor.getString(PHONE_ACCOUNT_ID));
                    TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class)
                            .createForPhoneAccountHandle(phoneAccount);
                    if (telephonyManager == null) {
                        // can happen when trying to fetch voicemails from a SIM that is no longer on
                        // the
                        // device
                        VvmLog.e(TAG, "account no longer valid, cannot retrieve message");
                        return;
                    }
                    if (!VvmAccountManager.isAccountActivated(context, phoneAccount)) {
                        phoneAccount = getAccountFromMarshmallowAccount(context, phoneAccount);
                        if (phoneAccount == null) {
                            VvmLog.w(TAG, "Account not registered - cannot retrieve message.");
                            return;
                        }
                        VvmLog.i(TAG, "Fetching voicemail with Marshmallow PhoneAccountHandle");
                    }
                    VvmLog.i(TAG, "Requesting network to fetch voicemail");
                    networkCallback = new fetchVoicemailNetworkRequestCallback(context, phoneAccount);
                    networkCallback.requestNetwork();
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void fetchVoicemail(final Network network, final VoicemailStatus.Editor status) {
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (retryCount > 0) {
                        VvmLog.i(TAG, "fetching voicemail, retry count=" + retryCount);
                        try (ImapHelper imapHelper = new ImapHelper(context, phoneAccount, network, status)) {
                            boolean success = imapHelper
                                    .fetchVoicemailPayload(new VoicemailFetchedCallback(context, uri, phoneAccount), uid);
                            if (!success && retryCount > 0) {
                                VvmLog.i(TAG, "fetch voicemail failed, retrying");
                                retryCount--;
                            } else {
                                return;
                            }
                        } catch (InitializingException e) {
                            VvmLog.w(TAG, "Can't retrieve Imap credentials ", e);
                            return;
                        }
                    }
                } finally {
                    if (networkCallback != null) {
                        networkCallback.releaseNetwork();
                    }
                }
            }
        });
    }

    private class fetchVoicemailNetworkRequestCallback extends VvmNetworkRequestCallback {

        public fetchVoicemailNetworkRequestCallback(Context context, PhoneAccountHandle phoneAccount) {
            super(context, phoneAccount, VoicemailStatus.edit(context, phoneAccount));
        }

        @Override
        public void onAvailable(final Network network) {
            super.onAvailable(network);
            fetchVoicemail(network, getVoicemailStatusEditor());
        }
    }
}
