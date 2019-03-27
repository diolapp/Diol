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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.scheduling.Tasks.TaskCreationException;

/**
 * BroadcastReceiver to queue and run {@link Task} with the
 * {@link android.app.job.JobScheduler}. A task is queued using a explicit
 * broadcast to this receiver. The intent should contain enough information in
 * {@link Intent#getExtras()} to construct the task (see
 * {@link Tasks#createIntent(Context, Class)}). The task will be queued directly
 * in {@link TaskExecutor} if it is already running, or in
 * {@link TaskSchedulerJobService} if not.
 */
@TargetApi(VERSION_CODES.O)
public class TaskReceiver extends BroadcastReceiver {

    private static final String TAG = "VvmTaskReceiver";

    private static final List<Intent> deferredBroadcasts = new ArrayList<>();

    /**
     * When {@link TaskExecutor#isTerminating()} is {@code true}, newly added tasks
     * will be deferred to allow the TaskExecutor to terminate properly. After
     * termination is completed this should be called to add the tasks again.
     */
    public static void resendDeferredBroadcasts(Context context) {
        for (Intent intent : deferredBroadcasts) {
            context.sendBroadcast(intent);
        }
        deferredBroadcasts.clear();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            VvmLog.w(TAG, "null intent received");
            return;
        }
        VvmLog.i(TAG, "task received");
        TaskExecutor taskExecutor = TaskExecutor.getRunningInstance();
        if (taskExecutor != null) {
            VvmLog.i(TAG, "TaskExecutor already running");
            if (taskExecutor.isTerminating()) {
                // The current taskExecutor and cannot do anything and a new job cannot be
                // scheduled. Defer
                // the task until a new job can be scheduled.
                VvmLog.w(TAG, "TaskExecutor is terminating, bouncing task");
                deferredBroadcasts.add(intent);
                return;
            }
            try {
                Task task = Tasks.createTask(context.getApplicationContext(), intent.getExtras());
                taskExecutor.addTask(task);
            } catch (TaskCreationException e) {
                VvmLog.e(TAG, "cannot create task", e);
            }
        } else {
            VvmLog.i(TAG, "scheduling new job");
            List<Bundle> taskList = new ArrayList<>();
            taskList.add(intent.getExtras());
            TaskSchedulerJobService.scheduleJob(context.getApplicationContext(), taskList, 0, true);
        }
    }
}
