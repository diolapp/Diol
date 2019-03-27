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
import android.telecom.PhoneAccountHandle;

import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.VvmLog;

/**
 * A task with this policy will automatically re-queue itself if
 * {@link BaseTask#fail()} has been called during
 * {@link BaseTask#onExecuteInBackgroundThread()}. A task will be retried at
 * most <code>retryLimit</code> times and with a <code>retryDelayMillis</code>
 * interval in between.
 */
public class RetryPolicy implements Policy {

    private static final String TAG = "RetryPolicy";
    private static final String EXTRA_RETRY_COUNT = "extra_retry_count";

    private final int retryLimit;
    private final int retryDelayMillis;

    private BaseTask task;

    private int retryCount;
    private boolean failed;

    private VoicemailStatus.DeferredEditor voicemailStatusEditor;

    public RetryPolicy(int retryLimit, int retryDelayMillis) {
        this.retryLimit = retryLimit;
        this.retryDelayMillis = retryDelayMillis;
    }

    private boolean hasMoreRetries() {
        return retryCount < retryLimit;
    }

    /**
     * Error status should only be set if retries has exhausted or the task is
     * successful. Status writes to this editor will be deferred until the task has
     * ended, and will only be committed if the task is successful or there are no
     * retries left.
     */
    public VoicemailStatus.Editor getVoicemailStatusEditor() {
        return voicemailStatusEditor;
    }

    @Override
    public void onCreate(BaseTask task, Bundle extras) {
        this.task = task;
        retryCount = extras.getInt(EXTRA_RETRY_COUNT, 0);
        if (retryCount > 0) {
            VvmLog.i(TAG, "retry #" + retryCount + " for " + this.task + " queued, executing in " + retryDelayMillis);
            this.task.setExecutionTime(this.task.getTimeMillis() + retryDelayMillis);
        }
        PhoneAccountHandle phoneAccountHandle = task.getPhoneAccountHandle();
        if (phoneAccountHandle == null) {
            VvmLog.e(TAG, "null phone account for phoneAccountHandle " + task.getPhoneAccountHandle());
            // This should never happen, but continue on if it does. The status write will
            // be
            // discarded.
        }
        voicemailStatusEditor = VoicemailStatus.deferredEdit(task.getContext(), phoneAccountHandle);
    }

    @Override
    public void onBeforeExecute() {
    }

    @Override
    public void onCompleted() {
        if (!failed || !hasMoreRetries()) {
            if (!failed) {
                VvmLog.i(TAG, task + " completed successfully");
            }
            if (!hasMoreRetries()) {
                VvmLog.i(TAG, "Retry limit for " + task + " reached");
            }
            VvmLog.i(TAG, "committing deferred status: " + voicemailStatusEditor.getValues());
            voicemailStatusEditor.deferredApply();
            return;
        }
        VvmLog.i(TAG, "discarding deferred status: " + voicemailStatusEditor.getValues());
        Intent intent = task.createRestartIntent();
        intent.putExtra(EXTRA_RETRY_COUNT, retryCount + 1);

        task.getContext().sendBroadcast(intent);
    }

    @Override
    public void onFail() {
        failed = true;
    }

    @Override
    public void onDuplicatedTaskAdded() {
    }
}
