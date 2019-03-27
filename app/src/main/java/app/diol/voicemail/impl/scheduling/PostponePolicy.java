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

import android.os.Bundle;

import app.diol.voicemail.impl.VvmLog;

/**
 * A task with Postpone policy will not be executed immediately. It will wait
 * for a while and if a duplicated task is queued during the duration, the task
 * will be postponed further. The task will only be executed if no new task was
 * added in postponeMillis. Useful to batch small tasks in quick succession
 * together.
 */
public class PostponePolicy implements Policy {

    private static final String TAG = "PostponePolicy";

    private final int postponeMillis;
    private BaseTask task;

    public PostponePolicy(int postponeMillis) {
        this.postponeMillis = postponeMillis;
    }

    @Override
    public void onCreate(BaseTask task, Bundle extras) {
        this.task = task;
        this.task.setExecutionTime(this.task.getTimeMillis() + postponeMillis);
    }

    @Override
    public void onBeforeExecute() {
        // Do nothing
    }

    @Override
    public void onCompleted() {
        // Do nothing
    }

    @Override
    public void onFail() {
        // Do nothing
    }

    @Override
    public void onDuplicatedTaskAdded() {
        if (task.hasStarted()) {
            return;
        }
        VvmLog.i(TAG, "postponing " + task);
        task.setExecutionTime(task.getTimeMillis() + postponeMillis);
    }
}
