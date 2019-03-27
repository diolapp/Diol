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
package app.diol.voicemail.impl.transcribe;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.VisibleForTesting;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.constants.ScheduledJobIds;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.voicemail.CarrierConfigKeys;
import app.diol.voicemail.VoicemailClient;
import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.transcribe.grpc.TranscriptionClientFactory;

/**
 * Job scheduler callback for launching voicemail transcription tasks. The
 * transcription tasks will run in the background and will typically last for
 * approximately the length of the voicemail audio (since thats how long the
 * backend transcription service takes to do the transcription).
 */
public class TranscriptionService extends JobService {
    @VisibleForTesting
    static final String EXTRA_VOICEMAIL_URI = "extra_voicemail_uri";
    @VisibleForTesting
    static final String EXTRA_ACCOUNT_HANDLE = "extra_account_handle";

    private ExecutorService executorService;
    private JobParameters jobParameters;
    private TranscriptionClientFactory clientFactory;
    private TranscriptionConfigProvider configProvider;
    private TranscriptionTask activeTask;
    private boolean stopped;

    @MainThread
    public TranscriptionService() {
        Assert.isMainThread();
    }

    @VisibleForTesting
    TranscriptionService(ExecutorService executorService, TranscriptionClientFactory clientFactory,
                         TranscriptionConfigProvider configProvider) {
        this.executorService = executorService;
        this.clientFactory = clientFactory;
        this.configProvider = configProvider;
    }

    // Schedule a task to transcribe the indicated voicemail, return true if
    // transcription task was
    // scheduled.
    @MainThread
    public static boolean scheduleNewVoicemailTranscriptionJob(Context context, Uri voicemailUri,
                                                               PhoneAccountHandle account, boolean highPriority) {
        Assert.isMainThread();
        if (!canTranscribeVoicemail(context, account)) {
            return false;
        }

        LogUtil.i("TranscriptionService.scheduleNewVoicemailTranscriptionJob", "scheduling transcription");
        Logger.get(context).logImpression(DialerImpression.Type.VVM_TRANSCRIPTION_VOICEMAIL_RECEIVED);

        ComponentName componentName = new ComponentName(context, TranscriptionService.class);
        JobInfo.Builder builder = new JobInfo.Builder(ScheduledJobIds.VVM_TRANSCRIPTION_JOB, componentName);
        if (highPriority) {
            builder.setMinimumLatency(0).setOverrideDeadline(0).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        } else {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        }
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        JobWorkItem workItem = makeWorkItem(voicemailUri, account);
        return scheduler.enqueue(builder.build(), workItem) == JobScheduler.RESULT_SUCCESS;
    }

    private static boolean canTranscribeVoicemail(Context context, PhoneAccountHandle account) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            LogUtil.i("TranscriptionService.canTranscribeVoicemail", "not supported by sdk");
            return false;
        }
        VoicemailClient client = VoicemailComponent.get(context).getVoicemailClient();
        if (!client.isVoicemailTranscriptionEnabled(context, account)) {
            LogUtil.i("TranscriptionService.canTranscribeVoicemail", "transcription is not enabled");
            return false;
        }
        if (!client.hasAcceptedTos(context, account)) {
            LogUtil.i("TranscriptionService.canTranscribeVoicemail", "hasn't accepted TOS");
            return false;
        }
        if (!Boolean.parseBoolean(client.getCarrierConfigString(context, account,
                CarrierConfigKeys.VVM_CARRIER_ALLOWS_OTT_TRANSCRIPTION_STRING))) {
            LogUtil.i("TranscriptionService.canTranscribeVoicemail", "carrier doesn't allow transcription");
            return false;
        }
        return true;
    }

    // Cancel all transcription tasks
    @MainThread
    public static void cancelTranscriptions(Context context) {
        Assert.isMainThread();
        LogUtil.enterBlock("TranscriptionService.cancelTranscriptions");
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        scheduler.cancel(ScheduledJobIds.VVM_TRANSCRIPTION_JOB);
    }

    static Uri getVoicemailUri(JobWorkItem workItem) {
        return workItem.getIntent().getParcelableExtra(EXTRA_VOICEMAIL_URI);
    }

    static PhoneAccountHandle getPhoneAccountHandle(JobWorkItem workItem) {
        return workItem.getIntent().getParcelableExtra(EXTRA_ACCOUNT_HANDLE);
    }

    private static JobWorkItem makeWorkItem(Uri voicemailUri, PhoneAccountHandle account) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_VOICEMAIL_URI, voicemailUri);
        if (account != null) {
            intent.putExtra(EXTRA_ACCOUNT_HANDLE, account);
        }
        return new JobWorkItem(intent);
    }

    @Override
    @MainThread
    public boolean onStartJob(JobParameters params) {
        Assert.isMainThread();
        LogUtil.enterBlock("TranscriptionService.onStartJob");
        if (!getConfigProvider().isVoicemailTranscriptionAvailable()) {
            LogUtil.i("TranscriptionService.onStartJob", "transcription not available, exiting.");
            return false;
        } else if (TextUtils.isEmpty(getConfigProvider().getServerAddress())) {
            LogUtil.i("TranscriptionService.onStartJob", "transcription server not configured, exiting.");
            return false;
        } else {
            LogUtil.i("TranscriptionService.onStartJob",
                    "transcription server address: " + configProvider.getServerAddress());
            jobParameters = params;
            return checkForWork();
        }
    }

    @Override
    @MainThread
    public boolean onStopJob(JobParameters params) {
        Assert.isMainThread();
        LogUtil.i("TranscriptionService.onStopJob", "params: " + params);
        stopped = true;
        Logger.get(this).logImpression(DialerImpression.Type.VVM_TRANSCRIPTION_JOB_STOPPED);
        if (activeTask != null) {
            LogUtil.i("TranscriptionService.onStopJob", "cancelling active task");
            activeTask.cancel();
            Logger.get(this).logImpression(DialerImpression.Type.VVM_TRANSCRIPTION_TASK_CANCELLED);
        }
        return true;
    }

    @Override
    @MainThread
    public void onDestroy() {
        Assert.isMainThread();
        LogUtil.enterBlock("TranscriptionService.onDestroy");
        cleanup();
    }

    private void cleanup() {
        if (clientFactory != null) {
            clientFactory.shutdown();
            clientFactory = null;
        }
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    @MainThread
    private boolean checkForWork() {
        Assert.isMainThread();
        if (stopped) {
            LogUtil.i("TranscriptionService.checkForWork", "stopped");
            return false;
        }
        JobWorkItem workItem = jobParameters.dequeueWork();
        if (workItem != null) {
            Assert.checkState(activeTask == null);
            activeTask = configProvider.shouldUseSyncApi()
                    ? new TranscriptionTaskSync(this, new Callback(), workItem, getClientFactory(), configProvider)
                    : new TranscriptionTaskAsync(this, new Callback(), workItem, getClientFactory(), configProvider);
            getExecutorService().execute(activeTask);
            return true;
        } else {
            return false;
        }
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            // The common use case is transcribing a single voicemail so just use a single
            // thread executor
            // The reason we're not using DialerExecutor here is because the transcription
            // task can be
            // very long running (ie. multiple minutes).
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }

    private TranscriptionConfigProvider getConfigProvider() {
        if (configProvider == null) {
            configProvider = new TranscriptionConfigProvider(this);
        }
        return configProvider;
    }

    private TranscriptionClientFactory getClientFactory() {
        if (clientFactory == null) {
            clientFactory = new TranscriptionClientFactory(this, getConfigProvider());
        }
        return clientFactory;
    }

    /**
     * Callback used by a task to indicate it has finished processing its work item
     */
    interface JobCallback {
        void onWorkCompleted(JobWorkItem completedWorkItem);
    }

    private class Callback implements JobCallback {
        @Override
        @MainThread
        public void onWorkCompleted(JobWorkItem completedWorkItem) {
            Assert.isMainThread();
            LogUtil.i("TranscriptionService.Callback.onWorkCompleted", completedWorkItem.toString());
            activeTask = null;
            if (stopped) {
                LogUtil.i("TranscriptionService.Callback.onWorkCompleted", "stopped");
            } else {
                jobParameters.completeWork(completedWorkItem);
                checkForWork();
            }
        }
    }
}
