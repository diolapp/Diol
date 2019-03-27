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
package app.diol.voicemail.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.proguard.UsedByReflection;
import app.diol.voicemail.impl.scheduling.BaseTask;
import app.diol.voicemail.impl.sms.StatusMessage;
import app.diol.voicemail.impl.sms.StatusSmsFetcher;
import app.diol.voicemail.impl.sync.VvmAccountManager;
import app.diol.voicemail.impl.utils.LoggerUtils;

/**
 * Task to verify the account status is still correct. This task is only for book keeping so any
 * error is ignored and will not retry. If the provision status sent by the carrier is "ready" the
 * access credentials will be updated (although it is not expected to change without the carrier
 * actively sending out an STATUS SMS which will be handled by {@link
 * app.diol.voicemail.impl.sms.OmtpMessageReceiver}). If the provisioning status is not ready an
 * {@link ActivationTask} will be launched to attempt to correct it.
 */
@TargetApi(VERSION_CODES.O)
@UsedByReflection(value = "Tasks.java")
public class StatusCheckTask extends BaseTask {

    public StatusCheckTask() {
        super(TASK_STATUS_CHECK);
    }

    public static void start(Context context, PhoneAccountHandle phoneAccountHandle) {
        Intent intent = BaseTask.createIntent(context, StatusCheckTask.class, phoneAccountHandle);
        context.sendBroadcast(intent);
    }

    @Override
    public void onExecuteInBackgroundThread() {
        TelephonyManager telephonyManager =
                getContext()
                        .getSystemService(TelephonyManager.class)
                        .createForPhoneAccountHandle(getPhoneAccountHandle());

        if (telephonyManager == null) {
            VvmLog.w(
                    "StatusCheckTask.onExecuteInBackgroundThread",
                    getPhoneAccountHandle() + " no longer valid");
            return;
        }
        if (telephonyManager.getServiceState().getState() != ServiceState.STATE_IN_SERVICE) {
            VvmLog.i(
                    "StatusCheckTask.onExecuteInBackgroundThread",
                    getPhoneAccountHandle() + " not in service");
            return;
        }
        OmtpVvmCarrierConfigHelper config =
                new OmtpVvmCarrierConfigHelper(getContext(), getPhoneAccountHandle());
        if (!config.isValid()) {
            VvmLog.e(
                    "StatusCheckTask.onExecuteInBackgroundThread",
                    "config no longer valid for " + getPhoneAccountHandle());
            VvmAccountManager.removeAccount(getContext(), getPhoneAccountHandle());
            return;
        }

        Bundle data;
        try (StatusSmsFetcher fetcher = new StatusSmsFetcher(getContext(), getPhoneAccountHandle())) {
            config.getProtocol().requestStatus(config, fetcher.getSentIntent());
            // Both the fetcher and OmtpMessageReceiver will be triggered, but
            // OmtpMessageReceiver will just route the SMS back to ActivationTask, which will be
            // rejected because the task is still running.
            data = fetcher.get();
        } catch (TimeoutException e) {
            VvmLog.e("StatusCheckTask.onExecuteInBackgroundThread", "timeout requesting status");
            return;
        } catch (CancellationException e) {
            VvmLog.e("StatusCheckTask.onExecuteInBackgroundThread", "Unable to send status request SMS");
            return;
        } catch (InterruptedException | ExecutionException | IOException e) {
            VvmLog.e("StatusCheckTask.onExecuteInBackgroundThread", "can't get future STATUS SMS", e);
            return;
        }

        StatusMessage message = new StatusMessage(data);
        VvmLog.i(
                "StatusCheckTask.onExecuteInBackgroundThread",
                "STATUS SMS received: st="
                        + message.getProvisioningStatus()
                        + ", rc="
                        + message.getReturnCode());
        if (message.getProvisioningStatus().equals(OmtpConstants.SUBSCRIBER_READY)) {
            VvmLog.i(
                    "StatusCheckTask.onExecuteInBackgroundThread",
                    "subscriber ready, no activation required");
            LoggerUtils.logImpressionOnMainThread(
                    getContext(), DialerImpression.Type.VVM_STATUS_CHECK_READY);
            VvmAccountManager.addAccount(getContext(), getPhoneAccountHandle(), message);
        } else {
            VvmLog.i(
                    "StatusCheckTask.onExecuteInBackgroundThread",
                    "subscriber not ready, attempting reactivation");
            VvmAccountManager.removeAccount(getContext(), getPhoneAccountHandle());
            LoggerUtils.logImpressionOnMainThread(
                    getContext(), DialerImpression.Type.VVM_STATUS_CHECK_REACTIVATION);
            ActivationTask.start(getContext(), getPhoneAccountHandle(), data);
        }
    }
}
