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
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.precall.PreCall;
import app.diol.dialer.precall.PreCallAction;
import app.diol.dialer.precall.PreCallCoordinator;

/**
 * Implementation of {@link PreCall}
 */
public class PreCallImpl implements PreCall {

    private final ImmutableList<PreCallAction> actions;

    @Inject
    PreCallImpl(ImmutableList<PreCallAction> actions) {
        this.actions = actions;
    }

    @NonNull
    @Override
    public Intent buildIntent(Context context, CallIntentBuilder builder) {
        Logger.get(context).logImpression(DialerImpression.Type.PRECALL_INITIATED);
        if (!requiresUi(context, builder)) {
            LogUtil.i("PreCallImpl.buildIntent", "No UI requested, running pre-call directly");
            for (PreCallAction action : actions) {
                action.runWithoutUi(context, builder);
            }
            return builder.build();
        }
        LogUtil.i("PreCallImpl.buildIntent", "building intent to start activity");
        Intent intent = new Intent(context, PreCallActivity.class);
        intent.putExtra(PreCallCoordinator.EXTRA_CALL_INTENT_BUILDER, builder);
        return intent;
    }

    private boolean requiresUi(Context context, CallIntentBuilder builder) {
        for (PreCallAction action : actions) {
            if (action.requiresUi(context, builder)) {
                LogUtil.i("PreCallImpl.requiresUi", action + " requested UI");
                return true;
            }
        }
        return false;
    }
}
