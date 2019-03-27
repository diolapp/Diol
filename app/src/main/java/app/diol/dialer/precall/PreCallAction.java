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

import android.content.Context;
import android.support.annotation.MainThread;

import app.diol.dialer.callintent.CallIntentBuilder;

/**
 * An action to perform before the call is made. The action should inspect and modify the {@link
 * CallIntentBuilder} to generate full information for the call. For example, showing a dialog to
 * select the phone account on a multi-SIM device, ask if RTT should be enabled, or rewrite the
 * number for roaming calls.
 *
 * <p>UI actions are discarded when the hosting activity is paused. A new instance of the action
 * will be created once the activity is resumed again.
 */
public interface PreCallAction {

    /**
     * Whether the action requires an activity to operate. This method is called on all actions before
     * {@link #runWithUi(PreCallCoordinator)} is called. If {@link true} is returned, {@link
     * #runWithUi(PreCallCoordinator)} will be guaranteed to be called on the execution phase.
     * Otherwise {@link #runWithoutUi(Context, CallIntentBuilder)} may be called instead and the
     * action will not be able to show UI, perform async task, or abort the call. This method should
     * not make any state changes.
     */
    @MainThread
    boolean requiresUi(Context context, CallIntentBuilder builder);

    /**
     * Called when all actions returned {@code false} for {@link #requiresUi(Context,
     * CallIntentBuilder)}.
     */
    void runWithoutUi(Context context, CallIntentBuilder builder);

    /**
     * Runs the action. Should block on the main thread until the action is finished. If the action is
     * not instantaneous, {@link PreCallCoordinator#startPendingAction()} should be called to release
     * the thread and continue later.
     */
    @MainThread
    void runWithUi(PreCallCoordinator coordinator);

    /**
     * Called when the UI is being paused when a {@link PreCallCoordinator.PendingAction} is started,
     * and the action is going to be discarded. If the action is showing a dialog the dialog should be
     * dismissed. The action should not retain state, a new instance of the action will be re-run when
     * the UI is resumed.
     */
    @MainThread
    void onDiscard();
}
