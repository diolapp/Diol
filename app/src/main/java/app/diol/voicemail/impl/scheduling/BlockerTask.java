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

import app.diol.dialer.proguard.UsedByReflection;
import app.diol.voicemail.impl.VvmLog;

/**
 * Task to block another task of the same ID from being queued for a certain
 * amount of time.
 */
@UsedByReflection(value = "Tasks.java")
public class BlockerTask extends BaseTask {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_BLOCK_FOR_MILLIS = "extra_block_for_millis";
    private static final String TAG = "BlockerTask";

    public BlockerTask() {
        super(TASK_INVALID);
    }

    @Override
    public void onCreate(Context context, Bundle extras) {
        super.onCreate(context, extras);
        setId(extras.getInt(EXTRA_TASK_ID, TASK_INVALID));
        setExecutionTime(getTimeMillis() + extras.getInt(EXTRA_BLOCK_FOR_MILLIS, 0));
    }

    @Override
    public void onExecuteInBackgroundThread() {
        // Do nothing.
    }

    @Override
    public void onDuplicatedTaskAdded(Task task) {
        VvmLog.i(TAG, task + "blocked, " + getReadyInMilliSeconds() + "millis remaining");
    }
}
