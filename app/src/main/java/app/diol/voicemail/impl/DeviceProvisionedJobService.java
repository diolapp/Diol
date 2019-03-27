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
import android.app.job.JobInfo.TriggerContentUri;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.constants.ScheduledJobIds;

/**
 * JobService triggered when the setup wizard is completed, and rerun all
 * {@link ActivationTask} scheduled during the setup.
 */
@TargetApi(VERSION_CODES.O)
public class DeviceProvisionedJobService extends JobService {

    @VisibleForTesting
    static final String EXTRA_PHONE_ACCOUNT_HANDLE = "EXTRA_PHONE_ACCOUNT_HANDLE";

    /**
     * Queue the phone account to be reactivated after the setup wizard has
     * completed.
     */
    public static void activateAfterProvisioned(Context context, PhoneAccountHandle phoneAccountHandle) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
        context.getSystemService(JobScheduler.class).enqueue(createJobInfo(context), new JobWorkItem(intent));
    }

    private static JobInfo createJobInfo(Context context) {
        return new JobInfo.Builder(ScheduledJobIds.VVM_DEVICE_PROVISIONED_JOB,
                new ComponentName(context, DeviceProvisionedJobService.class))
                .addTriggerContentUri(new TriggerContentUri(Global.getUriFor(Global.DEVICE_PROVISIONED), 0))
                // VVM activation must be run as soon as possible to avoid voicemail loss
                .setTriggerContentMaxDelay(0).build();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        if (!isDeviceProvisioned()) {
            VvmLog.i("DeviceProvisionedJobService.onStartJob", "device not provisioned, rescheduling");
            getSystemService(JobScheduler.class).schedule(createJobInfo(this));
            return false; // job not running in background
        }
        VvmLog.i("DeviceProvisionedJobService.onStartJob", "device provisioned");
        for (JobWorkItem item = params.dequeueWork(); item != null; item = params.dequeueWork()) {
            PhoneAccountHandle phoneAccountHandle = item.getIntent().getParcelableExtra(EXTRA_PHONE_ACCOUNT_HANDLE);
            VvmLog.i("DeviceProvisionedJobService.onStartJob", "restarting activation for " + phoneAccountHandle);
            ActivationTask.start(this, phoneAccountHandle, null);
        }
        return false; // job not running in background
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true; // reschedule job
    }

    private boolean isDeviceProvisioned() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) == 1;
    }
}
