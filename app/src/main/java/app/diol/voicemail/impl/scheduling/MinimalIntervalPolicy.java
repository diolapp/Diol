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

import android.content.Intent;
import android.os.Bundle;

import app.diol.voicemail.impl.scheduling.Task.TaskId;

/**
 * If a task with this policy succeeds, a {@link BlockerTask} with the same
 * {@link TaskId} of the task will be queued immediately, preventing the same
 * task from running for a certain amount of time.
 */
public class MinimalIntervalPolicy implements Policy {

    BaseTask task;
    TaskId id;
    int blockForMillis;

    public MinimalIntervalPolicy(int blockForMillis) {
        this.blockForMillis = blockForMillis;
    }

    @Override
    public void onCreate(BaseTask task, Bundle extras) {
        this.task = task;
        id = this.task.getId();
    }

    @Override
    public void onBeforeExecute() {
    }

    @Override
    public void onCompleted() {
        if (!task.hasFailed()) {
            Intent intent = BaseTask.createIntent(task.getContext(), BlockerTask.class, id.phoneAccountHandle);
            intent.putExtra(BlockerTask.EXTRA_TASK_ID, id.id);
            intent.putExtra(BlockerTask.EXTRA_BLOCK_FOR_MILLIS, blockForMillis);
            task.getContext().sendBroadcast(intent);
        }
    }

    @Override
    public void onFail() {
    }

    @Override
    public void onDuplicatedTaskAdded() {
    }
}
