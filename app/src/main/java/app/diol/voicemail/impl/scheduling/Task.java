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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.telecom.PhoneAccountHandle;

import java.util.Objects;

/**
 * A task for {@link TaskExecutor} to execute. Since the task is sent through a
 * bundle to the scheduler, The task must be constructable with the bundle.
 * Specifically, It must have a constructor with zero arguments, and have all
 * relevant data packed inside the bundle. Use
 * {@link Tasks#createIntent(Context, Class)} to create a intent that will
 * construct the Task.
 *
 * <p>
 * Only {@link #onExecuteInBackgroundThread()} is run on the worker thread.
 */
public interface Task {
    /**
     * TaskId to indicate it has not be set. If a task does not provide a default
     * TaskId it should be set before {@link Task#onCreate(Context, Bundle)} returns
     */
    int TASK_INVALID = -1;

    /**
     * TaskId to indicate it should always be queued regardless of duplicates.
     * {@link Task#onDuplicatedTaskAdded(Task)} will never be called on tasks with
     * this TaskId.
     */
    int TASK_ALLOW_DUPLICATES = -2;

    int TASK_UPLOAD = 1;
    int TASK_SYNC = 2;
    int TASK_ACTIVATION = 3;
    int TASK_STATUS_CHECK = 4;

    TaskId getId();

    /**
     * Serializes the task into a bundle, which will be stored in a
     * {@link android.app.job.JobInfo} and used to reconstruct the task even if the
     * app is terminated. The task will be initialized with
     * {@link #onCreate(Context, Bundle)}.
     */
    Bundle toBundle();

    /**
     * A task object is created through reflection, calling the default constructor.
     * The actual initialization is done in this method. If the task is not a new
     * instance, but being restored from a bundle, {@link #onRestore(Bundle)} will
     * be called afterwards.
     */
    @MainThread
    void onCreate(Context context, Bundle extras);

    /**
     * Called after {@link #onCreate(Context, Bundle)} if the task is being restored
     * from a Bundle instead creating a new instance. For example, if the task is
     * stored in {@link TaskSchedulerJobService} during a long sleep, this will be
     * called when the job is ran again and the tasks are being restored from the
     * saved state.
     */
    @MainThread
    void onRestore(Bundle extras);

    /**
     * @return number of milliSeconds the scheduler should wait before running this
     * task. A value less than
     * {@link TaskExecutor#READY_TOLERANCE_MILLISECONDS} will be considered
     * ready. If no tasks are ready, the scheduler will sleep for this
     * amount of time before doing another check (it will still wake if a
     * new task is added). The first task in the queue that is ready will be
     * executed.
     */
    @MainThread
    long getReadyInMilliSeconds();

    /**
     * Called on the main thread when the scheduler is about to send the task into
     * the worker thread, calling {@link #onExecuteInBackgroundThread()}
     */
    @MainThread
    void onBeforeExecute();

    /**
     * The actual payload of the task, executed on the worker thread.
     */
    @WorkerThread
    void onExecuteInBackgroundThread();

    /**
     * Called on the main thread when {@link #onExecuteInBackgroundThread()} has
     * finished or thrown an uncaught exception. The task is already removed from
     * the queue at this point, and a same task can be queued again.
     */
    @MainThread
    void onCompleted();

    /**
     * Another task with the same TaskId has been added. Necessary data can be
     * retrieved from the other task, and after this returns the task will be
     * discarded.
     */
    @MainThread
    void onDuplicatedTaskAdded(Task task);

    /**
     * Used to differentiate between types of tasks. If a task with the same TaskId
     * is already in the queue the new task will be rejected.
     */
    class TaskId {

        /**
         * Indicates the operation type of the task.
         */
        public final int id;
        /**
         * Same operation for a different phoneAccountHandle is allowed.
         * phoneAccountHandle is used to differentiate phone accounts in multi-SIM
         * scenario. For example, each SIM can queue a sync task for their own.
         */
        public final PhoneAccountHandle phoneAccountHandle;

        public TaskId(int id, PhoneAccountHandle phoneAccountHandle) {
            this.id = id;
            this.phoneAccountHandle = phoneAccountHandle;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof TaskId)) {
                return false;
            }
            TaskId other = (TaskId) object;
            return id == other.id && phoneAccountHandle.equals(other.phoneAccountHandle);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, phoneAccountHandle);
        }
    }
}
