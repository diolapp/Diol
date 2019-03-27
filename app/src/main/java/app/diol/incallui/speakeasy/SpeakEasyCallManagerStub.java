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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Optional;

import javax.inject.Inject;

import app.diol.incallui.call.DialerCall;

/**
 * Default implementation of SpeakEasyCallManager.
 */
public class SpeakEasyCallManagerStub implements SpeakEasyCallManager {

    @Inject
    public SpeakEasyCallManagerStub() {
    }

    /**
     * Returns an absent optional.
     */
    @Override
    @Nullable
    public Optional<Fragment> getSpeakEasyFragment(DialerCall call) {
        return Optional.empty();
    }

    /**
     * Always inert in the stub.
     */
    @Override
    public void onCallRemoved(DialerCall call) {
    }

    @Override
    public ListenableFuture<Void> onNewIncomingCall(@NonNull DialerCall call) {
        return Futures.immediateFuture(null);
    }

    /**
     * Always returns false.
     */
    @Override
    public boolean isAvailable(@NonNull Context unused) {
        return false;
    }

    /**
     * Always returns a stub string.
     */
    @NonNull
    @Override
    public String getConfigProviderFlag() {
        return "not_yet_implmented";
    }
}
