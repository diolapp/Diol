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

package app.diol.dialer.precall;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;

import com.google.common.util.concurrent.ListenableFuture;

import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.function.Consumer;

/**
 * Runs {@link PreCallAction} one by one to prepare a {@link
 * app.diol.dialer.callintent.CallIntentBuilder} for a call.
 */
public interface PreCallCoordinator {

    @VisibleForTesting
    public String EXTRA_CALL_INTENT_BUILDER = "extra_call_intent_builder";

    @NonNull
    CallIntentBuilder getBuilder();

    /**
     * @return the activity to attach the UI to.
     */
    @NonNull
    AppCompatActivity getActivity();

    /**
     * Called by a {@link PreCallAction} to abort the call. For example, the user has dismissed the
     * dialog and must start over.
     */
    void abortCall();

    /**
     * Called by the current running {@link PreCallAction} to release the main thread and resume
     * pre-call later.
     *
     * @return a {@link PendingAction} which {@link PendingAction#finish()} should be called to resume
     * pre-call. For example the action shows a dialog to the user, startPendingAction() should be
     * called as the action will not be finished immediately. When the dialog is completed, {@code
     * finish()} is then called to continue the next step.
     */
    @MainThread
    @NonNull
    PendingAction startPendingAction();

    <OutputT> void listen(
            ListenableFuture<OutputT> future,
            Consumer<OutputT> successListener,
            Consumer<Throwable> failureListener);

    /**
     * Callback from a {@link PreCallAction} to signal the action started by {@link
     * PreCallCoordinator#startPendingAction()} has finished.
     */
    interface PendingAction {

        @MainThread
        void finish();
    }
}
