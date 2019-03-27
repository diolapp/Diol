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

package app.diol.dialer.precall.impl;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.MainThread;
import android.telecom.PhoneAccount;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.precall.PreCallAction;
import app.diol.dialer.precall.PreCallCoordinator;

/**
 * Fix common malformed number before it is dialed. Rewrite the number to the first handler that can
 * handle it
 */
public class MalformedNumberRectifier implements PreCallAction {

    private final ImmutableList<MalformedNumberHandler> handlers;

    MalformedNumberRectifier(ImmutableList<MalformedNumberHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean requiresUi(Context context, CallIntentBuilder builder) {
        return false;
    }

    @Override
    public void runWithoutUi(Context context, CallIntentBuilder builder) {
        if (!PhoneAccount.SCHEME_TEL.equals(builder.getUri().getScheme())) {
            return;
        }
        String number = builder.getUri().getSchemeSpecificPart();

        for (MalformedNumberHandler handler : handlers) {
            Optional<String> result = handler.handle(context, number);
            if (result.isPresent()) {
                builder.setUri(Uri.fromParts(PhoneAccount.SCHEME_TEL, result.get(), null));
                return;
            }
        }
    }

    @Override
    public void runWithUi(PreCallCoordinator coordinator) {
        runWithoutUi(coordinator.getActivity(), coordinator.getBuilder());
    }

    @Override
    public void onDiscard() {
    }

    /**
     * Handler for individual rules.
     */
    public interface MalformedNumberHandler {

        /**
         * @return the number to be corrected to.
         */
        @MainThread
        Optional<String> handle(Context context, String number);
    }
}
