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
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.telecom.PhoneAccountHandle;

import java.util.ArrayList;
import java.util.List;

import app.diol.dialer.proguard.UsedByReflection;
import app.diol.voicemail.impl.Assert;
import app.diol.voicemail.impl.NeededForTesting;

/**
 * Provides common utilities for task implementations, such as execution time
 * and managing {@link Policy}
 */
@UsedByReflection(value = "Tasks.java")
public abstract class BaseTask implements Task {

    @VisibleForTesting
    public static final String EXTRA_PHONE_ACCOUNT_HANDLE = "extra_phone_account_handle";

    private static final String EXTRA_EXECUTION_TIME = "extra_execution_time";
    private static Clock clock = new Clock();
    @NonNull
    private final List<Policy> policies = new ArrayList<>();
    private Bundle extras;
    private Context context;
    private int id;
    private PhoneAccountHandle phoneAccountHandle;
    private boolean hasStarted;
    private volatile boolean hasFailed;
    private long executionTime;

    protected BaseTask(int id) {
        this.id = id;
        executionTime = getTimeMillis();
    }

    /**
     * Creates an intent that can be used to be broadcast to the
     * {@link TaskReceiver}. Derived class should build their intent upon this.
     */
    public static Intent createIntent(Context context, Class<? extends BaseTask> task,
                                      PhoneAccountHandle phoneAccountHandle) {
        Intent intent = Tasks.createIntent(context, task);
        intent.putExtra(EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
        return intent;
    }

    /**
     * Used to replace the clock with an deterministic clock
     */
    @NeededForTesting
    static void setClockForTesting(Clock clock) {
        BaseTask.clock = clock;
    }

    @MainThread
    public boolean hasStarted() {
        Assert.isMainThread();
        return hasStarted;
    }

    @MainThread
    public boolean hasFailed() {
        Assert.isMainThread();
        return hasFailed;
    }

    public Context getContext() {
        return context;
    }

    public PhoneAccountHandle getPhoneAccountHandle() {
        return phoneAccountHandle;
    }

    /**
     * Should be call in the constructor or
     * {@link Policy#onCreate(BaseTask, Bundle)} will be missed.
     */
    @MainThread
    public BaseTask addPolicy(Policy policy) {
        Assert.isMainThread();
        policies.add(policy);
        return this;
    }

    /**
     * Indicate the task has failed. {@link Policy#onFail()} will be triggered once
     * the execution ends. This mechanism is used by policies for actions such as
     * determining whether to schedule a retry. Must be call inside
     * {@link #onExecuteInBackgroundThread()}
     */
    @WorkerThread
    public void fail() {
        Assert.isNotMainThread();
        hasFailed = true;
    }

    /**
     * @param timeMillis the time since epoch, in milliseconds.
     */
    @MainThread
    public void setExecutionTime(long timeMillis) {
        Assert.isMainThread();
        executionTime = timeMillis;
    }

    public long getTimeMillis() {
        return clock.getTimeMillis();
    }

    /**
     * Creates an intent that can be used to restart the current task. Derived class
     * should build their intent upon this.
     */
    public Intent createRestartIntent() {
        return createIntent(getContext(), this.getClass(), phoneAccountHandle);
    }

    @Override
    public TaskId getId() {
        return new TaskId(id, phoneAccountHandle);
    }

    /**
     * Modify the task ID to prevent arbitrary task from executing. Can only be
     * called before {@link #onCreate(Context, Bundle)} returns.
     */
    @MainThread
    public void setId(int id) {
        Assert.isMainThread();
        this.id = id;
    }

    @Override
    public Bundle toBundle() {
        extras.putLong(EXTRA_EXECUTION_TIME, executionTime);
        return extras;
    }

    @Override
    @CallSuper
    public void onCreate(Context context, Bundle extras) {
        this.context = context;
        this.extras = extras;
        phoneAccountHandle = extras.getParcelable(EXTRA_PHONE_ACCOUNT_HANDLE);
        for (Policy policy : policies) {
            policy.onCreate(this, extras);
        }
    }

    @Override
    @CallSuper
    public void onRestore(Bundle extras) {
        if (this.extras.containsKey(EXTRA_EXECUTION_TIME)) {
            executionTime = extras.getLong(EXTRA_EXECUTION_TIME);
        }
    }

    @Override
    public long getReadyInMilliSeconds() {
        return executionTime - getTimeMillis();
    }

    @Override
    @CallSuper
    public void onBeforeExecute() {
        for (Policy policy : policies) {
            policy.onBeforeExecute();
        }
        hasStarted = true;
    }

    @Override
    @CallSuper
    public void onCompleted() {
        if (hasFailed) {
            for (Policy policy : policies) {
                policy.onFail();
            }
        }

        for (Policy policy : policies) {
            policy.onCompleted();
        }
    }

    @Override
    public void onDuplicatedTaskAdded(Task task) {
        for (Policy policy : policies) {
            policy.onDuplicatedTaskAdded();
        }
    }

    @NeededForTesting
    static class Clock {

        public long getTimeMillis() {
            return SystemClock.elapsedRealtime();
        }
    }
}
