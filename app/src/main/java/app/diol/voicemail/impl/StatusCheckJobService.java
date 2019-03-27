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
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import java.util.concurrent.TimeUnit;

import app.diol.dialer.constants.ScheduledJobIds;
import app.diol.voicemail.impl.sync.VvmAccountManager;

/**
 * A job to perform {@link StatusCheckTask} once per day, performing book
 * keeping to ensure the credentials and status for a activated voicemail
 * account is still correct. A task will be scheduled for each active voicemail
 * account. The status is expected to be always in sync, the check is a failsafe
 * to mimic the previous status check on signal return behavior.
 */
@TargetApi(VERSION_CODES.O)
public class StatusCheckJobService extends JobService {

    public static void schedule(Context context) {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (jobScheduler.getPendingJob(ScheduledJobIds.VVM_STATUS_CHECK_JOB) != null) {
            VvmLog.i("StatusCheckJobService.schedule", "job already scheduled");
            return;
        }

        jobScheduler.schedule(new JobInfo.Builder(ScheduledJobIds.VVM_STATUS_CHECK_JOB,
                new ComponentName(context, StatusCheckJobService.class)).setPeriodic(TimeUnit.DAYS.toMillis(1))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setRequiresCharging(true).build());
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        for (PhoneAccountHandle phoneAccountHandle : getSystemService(TelecomManager.class).getCallCapablePhoneAccounts()) {
            if (VvmAccountManager.isAccountActivated(this, phoneAccountHandle)) {
                StatusCheckTask.start(this, phoneAccountHandle);
            }
        }
        return false; // not running in background
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false; // don't retry
    }
}
