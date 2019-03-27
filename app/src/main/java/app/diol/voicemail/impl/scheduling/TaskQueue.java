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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import app.diol.voicemail.impl.Assert;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.scheduling.Task.TaskId;
import app.diol.voicemail.impl.scheduling.Tasks.TaskCreationException;

/**
 * A queue that manages priority and duplication of {@link Task}. A task is
 * identified by a {@link TaskId}, which consists of an integer representing the
 * operation the task, and a {@link android.telecom.PhoneAccountHandle}
 * representing which SIM it is operated on.
 */
class TaskQueue implements Iterable<Task> {

    private final Queue<Task> queue = new ArrayDeque<>();

    public List<Bundle> toBundles() {
        List<Bundle> result = new ArrayList<>(queue.size());
        for (Task task : queue) {
            result.add(Tasks.toBundle(task));
        }
        return result;
    }

    public void fromBundles(Context context, List<Bundle> pendingTasks) {
        Assert.isTrue(queue.isEmpty());
        for (Bundle pendingTask : pendingTasks) {
            try {
                Task task = Tasks.createTask(context, pendingTask);
                task.onRestore(pendingTask);
                add(task);
            } catch (TaskCreationException e) {
                VvmLog.e("TaskQueue.fromBundles", "cannot create task", e);
            }
        }
    }

    /**
     * Add a new task to the queue. A new task with a TaskId collision will be
     * discarded, and {@link Task#onDuplicatedTaskAdded(Task)} will be called on the
     * existing task.
     *
     * @return {@code true} if the task is added, or {@code false} if the task is
     * discarded due to collision.
     */
    public boolean add(Task task) {
        if (task.getId().id == Task.TASK_INVALID) {
            throw new AssertionError("Task id was not set to a valid value before adding.");
        }
        if (task.getId().id != Task.TASK_ALLOW_DUPLICATES) {
            Task oldTask = getTask(task.getId());
            if (oldTask != null) {
                oldTask.onDuplicatedTaskAdded(task);
                VvmLog.i("TaskQueue.add", "duplicated task added");
                return false;
            }
        }
        queue.add(task);
        return true;
    }

    public void remove(Task task) {
        queue.remove(task);
    }

    public Task getTask(TaskId id) {
        Assert.isMainThread();
        for (Task task : queue) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    /**
     * The next task is the first task with {@link Task#getReadyInMilliSeconds()}
     * return a value less then {@code readyToleranceMillis}, in insertion order. If
     * no task matches this criteria, the minimal value of
     * {@link Task#getReadyInMilliSeconds()} is returned instead. If there are no
     * tasks at all, the minimalWaitTimeMillis will also be null.
     */
    @NonNull
    NextTask getNextTask(long readyToleranceMillis) {
        Long minimalWaitTime = null;
        for (Task task : queue) {
            long waitTime = task.getReadyInMilliSeconds();
            if (waitTime < readyToleranceMillis) {
                return new NextTask(task, 0L);
            } else {
                if (minimalWaitTime == null || waitTime < minimalWaitTime) {
                    minimalWaitTime = waitTime;
                }
            }
        }
        return new NextTask(null, minimalWaitTime);
    }

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Iterator<Task> iterator() {
        return queue.iterator();
    }

    /**
     * Packed return value of {@link #getNextTask(long)}. If a runnable task is
     * found {@link #minimalWaitTimeMillis} will be {@code null}. If no tasks is
     * runnable {@link #task} will be {@code null}, and
     * {@link #minimalWaitTimeMillis} will contain the time to wait. If there are no
     * tasks at all both will be {@code null}.
     */
    static final class NextTask {
        @Nullable
        final Task task;
        @Nullable
        final Long minimalWaitTimeMillis;

        NextTask(@Nullable Task task, @Nullable Long minimalWaitTimeMillis) {
            this.task = task;
            this.minimalWaitTimeMillis = minimalWaitTimeMillis;
        }
    }
}
