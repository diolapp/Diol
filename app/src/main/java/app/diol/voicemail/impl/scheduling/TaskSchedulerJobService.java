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

package app.diol.voicemail.impl.scheduling;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;

import java.util.ArrayList;
import java.util.List;

import app.diol.dialer.constants.ScheduledJobIds;
import app.diol.dialer.strictmode.StrictModeUtils;
import app.diol.voicemail.impl.Assert;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.scheduling.Tasks.TaskCreationException;

/**
 * A {@link JobService} that will trigger the background execution of {@link TaskExecutor}.
 */
@TargetApi(VERSION_CODES.O)
public class TaskSchedulerJobService extends JobService implements TaskExecutor.Job {

    private static final String TAG = "TaskSchedulerJobService";

    private static final String EXTRA_TASK_EXTRAS_ARRAY = "extra_task_extras_array";

    private static final String EXTRA_JOB_ID = "extra_job_id";

    private static final String EXPECTED_JOB_ID =
            "app.diol.voicemail.impl.scheduling.TaskSchedulerJobService.EXPECTED_JOB_ID";

    private static final String NEXT_JOB_ID =
            "app.diol.voicemail.impl.scheduling.TaskSchedulerJobService.NEXT_JOB_ID";

    private JobParameters jobParameters;

    /**
     * Schedule a job to run the {@code pendingTasks}. If a job is already scheduled it will be
     * appended to the back of the queue and the job will be rescheduled. A job may only be scheduled
     * when the {@link TaskExecutor} is not running ({@link TaskExecutor#getRunningInstance()}
     * returning {@code null})
     *
     * @param delayMillis delay before running the job. Must be 0 if{@code isNewJob} is true.
     * @param isNewJob    a new job will be forced to run immediately.
     */
    @MainThread
    public static void scheduleJob(
            Context context, List<Bundle> pendingTasks, long delayMillis, boolean isNewJob) {
        Assert.isMainThread();
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        JobInfo pendingJob = jobScheduler.getPendingJob(ScheduledJobIds.VVM_TASK_SCHEDULER_JOB);
        VvmLog.i(TAG, "scheduling job with " + pendingTasks.size() + " tasks");
        if (pendingJob != null) {
            if (isNewJob) {
                List<Bundle> existingTasks =
                        getBundleList(
                                pendingJob.getTransientExtras().getParcelableArray(EXTRA_TASK_EXTRAS_ARRAY));
                VvmLog.i(TAG, "merging job with " + existingTasks.size() + " existing tasks");
                TaskQueue queue = new TaskQueue();
                queue.fromBundles(context, existingTasks);
                for (Bundle pendingTask : pendingTasks) {
                    try {
                        queue.add(Tasks.createTask(context, pendingTask));
                    } catch (TaskCreationException e) {
                        VvmLog.e(TAG, "cannot create task", e);
                    }
                }
                pendingTasks = queue.toBundles();
            }
            VvmLog.i(TAG, "canceling existing job.");
            jobScheduler.cancel(ScheduledJobIds.VVM_TASK_SCHEDULER_JOB);
        }
        Bundle extras = new Bundle();
        int jobId = createJobId(context);
        extras.putInt(EXTRA_JOB_ID, jobId);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(EXPECTED_JOB_ID, jobId)
                .apply();

        extras.putParcelableArray(
                EXTRA_TASK_EXTRAS_ARRAY, pendingTasks.toArray(new Bundle[pendingTasks.size()]));
        JobInfo.Builder builder =
                new JobInfo.Builder(
                        ScheduledJobIds.VVM_TASK_SCHEDULER_JOB,
                        new ComponentName(context, TaskSchedulerJobService.class))
                        .setTransientExtras(extras)
                        .setMinimumLatency(delayMillis)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        if (isNewJob) {
            Assert.isTrue(delayMillis == 0);
            builder.setOverrideDeadline(0);
            VvmLog.i(TAG, "running job instantly.");
        }
        jobScheduler.schedule(builder.build());
        VvmLog.i(TAG, "job " + jobId + " scheduled");
    }

    private static List<Bundle> getBundleList(Parcelable[] parcelables) {
        List<Bundle> result = new ArrayList<>(parcelables.length);
        for (Parcelable parcelable : parcelables) {
            result.add((Bundle) parcelable);
        }
        return result;
    }

    private static int createJobId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int jobId = sharedPreferences.getInt(NEXT_JOB_ID, 0);
        sharedPreferences.edit().putInt(NEXT_JOB_ID, jobId + 1).apply();
        return jobId;
    }

    @Override
    @MainThread
    public boolean onStartJob(JobParameters params) {
        int jobId = params.getTransientExtras().getInt(EXTRA_JOB_ID);
        int expectedJobId =
                StrictModeUtils.bypass(
                        () -> PreferenceManager.getDefaultSharedPreferences(this).getInt(EXPECTED_JOB_ID, 0));
        if (jobId != expectedJobId) {
            VvmLog.e(
                    TAG, "Job " + jobId + " is not the last scheduled job " + expectedJobId + ", ignoring");
            return false; // nothing more to do. Job not running in background.
        }
        VvmLog.i(TAG, "starting " + jobId);
        jobParameters = params;
        TaskExecutor.createRunningInstance(this);
        TaskExecutor.getRunningInstance()
                .onStartJob(
                        this,
                        getBundleList(
                                jobParameters.getTransientExtras().getParcelableArray(EXTRA_TASK_EXTRAS_ARRAY)));
        return true /* job still running in background */;
    }

    @Override
    @MainThread
    public boolean onStopJob(JobParameters params) {
        TaskExecutor.getRunningInstance().onStopJob();
        jobParameters = null;
        return false /* don't reschedule. TaskExecutor service will post a new job */;
    }

    /**
     * The system will hold a wakelock when {@link #onStartJob(JobParameters)} is called to ensure the
     * device will not sleep when the job is still running. Finish the job so the system will release
     * the wakelock
     */
    @Override
    public void finishAsync() {
        VvmLog.i(TAG, "finishing job");
        jobFinished(jobParameters, false);
        jobParameters = null;
    }

    @MainThread
    @Override
    public boolean isFinished() {
        Assert.isMainThread();
        return getSystemService(JobScheduler.class)
                .getPendingJob(ScheduledJobIds.VVM_TASK_SCHEDULER_JOB)
                == null;
    }
}
