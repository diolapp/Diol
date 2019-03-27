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
import android.os.BadParcelableException;
import android.os.Bundle;
import android.support.annotation.NonNull;

import app.diol.voicemail.impl.VvmLog;

/**
 * Common operations on {@link Task}
 */
final class Tasks {

    static final String EXTRA_CLASS_NAME = "extra_class_name";

    private Tasks() {
    }

    /**
     * Create a task from a bundle. The bundle is created either with
     * {@link #toBundle(Task)} or {@link #createIntent(Context, Class)} from the
     * target {@link Task}
     */
    @NonNull
    public static Task createTask(Context context, Bundle extras) throws TaskCreationException {
        // The extra contains custom parcelables which cannot be unmarshalled by the
        // framework class
        // loader.
        extras.setClassLoader(context.getClassLoader());
        String className;
        try {
            className = extras.getString(EXTRA_CLASS_NAME);
        } catch (BadParcelableException e) {
            // BadParcelableException:Parcelable protocol requires that the class implements
            // Parcelable
            // This happens when the task is submitted before an update, and can no longer
            // be unparceled.
            throw new TaskCreationException(e);
        }
        VvmLog.i("Task.createTask", "create task:" + className);
        if (className == null) {
            throw new IllegalArgumentException("EXTRA_CLASS_NAME expected");
        }
        try {
            Task task = (Task) Class.forName(className).getDeclaredConstructor().newInstance();
            task.onCreate(context, extras);
            return task;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Serializes necessary states to a bundle that can be used to restore the task
     * with {@link #createTask(Context, Bundle)}
     */
    public static Bundle toBundle(Task task) {
        Bundle result = task.toBundle();
        result.putString(EXTRA_CLASS_NAME, task.getClass().getName());
        return result;
    }

    /**
     * Create an intent that when called with {@link Context#startService(Intent)},
     * will queue the <code>task</code>. Implementations of {@link Task} should use
     * the result of this and fill in necessary information.
     */
    public static Intent createIntent(Context context, Class<? extends Task> task) {
        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setPackage(context.getPackageName());
        intent.putExtra(EXTRA_CLASS_NAME, task.getName());
        return intent;
    }

    /**
     * The task cannot be created.
     */
    static final class TaskCreationException extends Exception {
        TaskCreationException(Throwable throwable) {
            super(throwable);
        }
    }
}
