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

package app.diol.dialer.app.calllog;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.provider.VoicemailContract;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.constants.ScheduledJobIds;

/**
 * Monitors voicemail provider changes to update active notifications.
 */
public class VoicemailNotificationJobService extends JobService {

    private static JobInfo jobInfo;

    /**
     * Start monitoring the provider. The provider should be monitored whenever a visual voicemail
     * notification is visible.
     */
    public static void scheduleJob(Context context) {
        context.getSystemService(JobScheduler.class).schedule(getJobInfo(context));
        LogUtil.i("VoicemailNotificationJobService.scheduleJob", "job scheduled");
    }

    /**
     * Stop monitoring the provider. The provider should not be monitored when visual voicemail
     * notification is cleared.
     */
    public static void cancelJob(Context context) {
        context.getSystemService(JobScheduler.class).cancel(ScheduledJobIds.VVM_NOTIFICATION_JOB);
        LogUtil.i("VoicemailNotificationJobService.scheduleJob", "job canceled");
    }

    private static JobInfo getJobInfo(Context context) {
        if (jobInfo == null) {
            jobInfo =
                    new JobInfo.Builder(
                            ScheduledJobIds.VVM_NOTIFICATION_JOB,
                            new ComponentName(context, VoicemailNotificationJobService.class))
                            .addTriggerContentUri(
                                    new JobInfo.TriggerContentUri(
                                            VoicemailContract.Voicemails.CONTENT_URI,
                                            JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
                            .setTriggerContentMaxDelay(0)
                            .build();
        }

        return jobInfo;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        LogUtil.i("VoicemailNotificationJobService.onStartJob", "updating notification");
        VisualVoicemailUpdateTask.scheduleTask(
                this,
                () -> {
                    jobFinished(params, false);
                });
        return true; // Running in background
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
