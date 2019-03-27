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

package app.diol.incallui.speakeasy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Optional;

import app.diol.incallui.call.DialerCall;

/**
 * Provides operations necessary to SpeakEasy.
 */
public interface SpeakEasyCallManager {

    /**
     * Returns the Fragment used to display data.
     *
     * <p>An absent optional indicates the feature is unavailable.
     */
    Optional<Fragment> getSpeakEasyFragment(@NonNull DialerCall call);

    /**
     * Indicates a call has been removed.
     *
     * @param call The call which has been removed.
     */
    void onCallRemoved(@NonNull DialerCall call);

    /**
     * Indicates there is a new incoming call that is about to be answered.
     *
     * @param call The call which is about to become active.
     */
    ListenableFuture<Void> onNewIncomingCall(@NonNull DialerCall call);

    /**
     * Indicates the feature is available.
     *
     * @param context The application context.
     */
    boolean isAvailable(@NonNull Context context);

    /**
     * Optional: Performs work necessary to happen-before callers use other methods on this interface.
     *
     * @apiNote Use of this API is completely optional, and callers are NOT required to invoke this
     * method prior to using other methods on the interface.
     * @implSpec Other members of this interface always promise to do any required initialization work
     * at the time they are invoked. This method will always be idempotent.
     */
    default void performManualInitialization() {
    }

    /**
     * Returns the config provider flag associated with the feature.
     */
    @NonNull
    String getConfigProviderFlag();
}
