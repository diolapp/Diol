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

package app.diol.voicemail.impl.sms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.telecom.PhoneAccountHandle;
import android.telephony.SmsManager;
import android.telephony.VisualVoicemailSms;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.diol.voicemail.impl.Assert;
import app.diol.voicemail.impl.OmtpConstants;
import app.diol.voicemail.impl.OmtpService;
import app.diol.voicemail.impl.OmtpVvmCarrierConfigHelper;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.protocol.VisualVoicemailProtocol;

/**
 * Intercepts a incoming STATUS SMS with a blocking call.
 */
@TargetApi(VERSION_CODES.O)
public class StatusSmsFetcher extends BroadcastReceiver implements Closeable {

    private static final String TAG = "VvmStatusSmsFetcher";

    private static final long STATUS_SMS_TIMEOUT_MILLIS = 60_000;

    private static final String ACTION_REQUEST_SENT_INTENT = "com.android.voicemailomtp.sms.REQUEST_SENT";
    private static final int ACTION_REQUEST_SENT_REQUEST_CODE = 0;
    private final Context context;
    private final PhoneAccountHandle phoneAccountHandle;
    private CompletableFuture<Bundle> future = new CompletableFuture<>();

    public StatusSmsFetcher(Context context, PhoneAccountHandle phoneAccountHandle) {
        this.context = context;
        this.phoneAccountHandle = phoneAccountHandle;
        IntentFilter filter = new IntentFilter(ACTION_REQUEST_SENT_INTENT);
        filter.addAction(OmtpService.ACTION_SMS_RECEIVED);
        context.registerReceiver(this, filter);
    }

    private static String sentSmsResultToString(int resultCode) {
        switch (resultCode) {
            case AppCompatActivity.RESULT_OK:
                return "OK";
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "RESULT_ERROR_GENERIC_FAILURE";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "RESULT_ERROR_GENERIC_FAILURE";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "RESULT_ERROR_GENERIC_FAILURE";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "RESULT_ERROR_GENERIC_FAILURE";
            default:
                return "UNKNOWN CODE: " + resultCode;
        }
    }

    @Override
    public void close() throws IOException {
        context.unregisterReceiver(this);
    }

    @WorkerThread
    @Nullable
    public Bundle get() throws InterruptedException, ExecutionException, TimeoutException, CancellationException {
        Assert.isNotMainThread();
        return future.get(STATUS_SMS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }

    public PendingIntent getSentIntent() {
        Intent intent = new Intent(ACTION_REQUEST_SENT_INTENT);
        intent.setPackage(context.getPackageName());
        // Because the receiver is registered dynamically, implicit intent must be used.
        // There should only be a single status SMS request at a time.
        return PendingIntent.getBroadcast(context, ACTION_REQUEST_SENT_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    @MainThread
    public void onReceive(Context context, Intent intent) {
        Assert.isMainThread();
        if (ACTION_REQUEST_SENT_INTENT.equals(intent.getAction())) {
            int resultCode = getResultCode();

            if (resultCode == Activity.RESULT_OK) {
                VvmLog.d(TAG, "Request SMS successfully sent");
                return;
            }

            VvmLog.e(TAG, "Request SMS send failed: " + sentSmsResultToString(resultCode));
            future.cancel(true);
            return;
        }

        VisualVoicemailSms sms = intent.getExtras().getParcelable(OmtpService.EXTRA_VOICEMAIL_SMS);

        if (!phoneAccountHandle.equals(sms.getPhoneAccountHandle())) {
            return;
        }
        String eventType = sms.getPrefix();

        if (eventType.equals(OmtpConstants.STATUS_SMS_PREFIX)) {
            future.complete(sms.getFields());
            return;
        }

        if (eventType.equals(OmtpConstants.SYNC_SMS_PREFIX)) {
            return;
        }

        VvmLog.i(TAG, "VVM SMS with event " + eventType + " received, attempting to translate to STATUS SMS");
        OmtpVvmCarrierConfigHelper helper = new OmtpVvmCarrierConfigHelper(context, phoneAccountHandle);
        VisualVoicemailProtocol protocol = helper.getProtocol();
        if (protocol == null) {
            return;
        }
        Bundle translatedBundle = protocol.translateStatusSmsBundle(helper, eventType, sms.getFields());

        if (translatedBundle != null) {
            VvmLog.i(TAG, "Translated to STATUS SMS");
            future.complete(translatedBundle);
        }
    }
}
