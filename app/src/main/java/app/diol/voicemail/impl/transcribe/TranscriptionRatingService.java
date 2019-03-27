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
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.WorkerThread;
import android.support.v4.app.JobIntentService;

import com.google.internal.communications.voicemailtranscription.v1.SendTranscriptionFeedbackRequest;
import com.google.protobuf.InvalidProtocolBufferException;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.constants.ScheduledJobIds;
import app.diol.voicemail.impl.transcribe.grpc.TranscriptionClientFactory;

/**
 * JobScheduler service for uploading transcription feedback. This service
 * requires a network connection.
 */
public class TranscriptionRatingService extends JobIntentService {
    private static final String FEEDBACK_REQUEST_EXTRA = "feedback_request_extra";

    public TranscriptionRatingService() {
    }

    /**
     * Schedule a task to upload transcription rating feedback
     */
    public static boolean scheduleTask(Context context, SendTranscriptionFeedbackRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LogUtil.enterBlock("TranscriptionRatingService.scheduleTask");
            ComponentName componentName = new ComponentName(context, TranscriptionRatingService.class);
            JobInfo.Builder builder = new JobInfo.Builder(ScheduledJobIds.VVM_TRANSCRIPTION_RATING_JOB, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            JobScheduler scheduler = context.getSystemService(JobScheduler.class);
            return scheduler.enqueue(builder.build(), makeWorkItem(request)) == JobScheduler.RESULT_SUCCESS;
        } else {
            LogUtil.i("TranscriptionRatingService.scheduleTask", "not supported");
            return false;
        }
    }

    private static JobWorkItem makeWorkItem(SendTranscriptionFeedbackRequest request) {
        Intent intent = new Intent();
        intent.putExtra(FEEDBACK_REQUEST_EXTRA, request.toByteArray());
        return new JobWorkItem(intent);
    }

    @Override
    @WorkerThread
    protected void onHandleWork(Intent intent) {
        LogUtil.enterBlock("TranscriptionRatingService.onHandleWork");

        TranscriptionConfigProvider configProvider = new TranscriptionConfigProvider(this);
        TranscriptionClientFactory factory = new TranscriptionClientFactory(this, configProvider);
        try {
            // Send rating to server
            SendTranscriptionFeedbackRequest request = SendTranscriptionFeedbackRequest
                    .parseFrom(intent.getByteArrayExtra(FEEDBACK_REQUEST_EXTRA));
            factory.getClient().sendTranscriptFeedbackRequest(request);
        } catch (InvalidProtocolBufferException e) {
            LogUtil.e("TranscriptionRatingService.onHandleWork", "failed to send feedback", e);
        } finally {
            factory.shutdown();
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.enterBlock("TranscriptionRatingService.onDestroy");
        super.onDestroy();
    }
}
