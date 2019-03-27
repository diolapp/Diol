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
import android.support.v7.app.AppCompatActivity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import javax.inject.Inject;

import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.Ui;
import app.diol.dialer.duo.Duo.ReachabilityData;
import app.diol.dialer.duo.DuoComponent;
import app.diol.dialer.precall.PreCallAction;
import app.diol.dialer.precall.PreCallCoordinator;
import app.diol.dialer.precall.PreCallCoordinator.PendingAction;

/**
 * Checks if a duo call is actually callable, and request an activity for {@link
 * AppCompatActivity#startActivityForResult(Intent, int)}
 */
public class DuoAction implements PreCallAction {

    private final ListeningExecutorService uiExecutor;

    @Inject
    DuoAction(@Ui ListeningExecutorService uiExecutor) {
        this.uiExecutor = uiExecutor;
    }

    @Override
    public boolean requiresUi(Context context, CallIntentBuilder builder) {
        // Duo call must be started with startActivityForResult() which needs a activity.
        return builder.isDuoCall();
    }

    @Override
    public void runWithoutUi(Context context, CallIntentBuilder builder) {
    }

    @Override
    public void runWithUi(PreCallCoordinator coordinator) {
        if (!requiresUi(coordinator.getActivity(), coordinator.getBuilder())) {
            return;
        }
        String number = coordinator.getBuilder().getUri().getSchemeSpecificPart();
        ListenableFuture<ImmutableMap<String, ReachabilityData>> reachabilities =
                DuoComponent.get(coordinator.getActivity())
                        .getDuo()
                        .updateReachability(coordinator.getActivity(), ImmutableList.of(number));
        PendingAction pendingAction = coordinator.startPendingAction();

        Futures.addCallback(
                reachabilities,
                new FutureCallback<ImmutableMap<String, ReachabilityData>>() {
                    @Override
                    public void onSuccess(ImmutableMap<String, ReachabilityData> result) {
                        if (!result.containsKey(number) || !result.get(number).videoCallable()) {
                            LogUtil.w(
                                    "DuoAction.runWithUi",
                                    number + " number no longer duo reachable, falling back to carrier video call");
                            coordinator.getBuilder().setIsDuoCall(false);
                        }
                        pendingAction.finish();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        LogUtil.e("DuoAction.runWithUi", "reachability query failed", throwable);
                        coordinator.getBuilder().setIsDuoCall(false);
                        pendingAction.finish();
                    }
                },
                uiExecutor);
    }

    @Override
    public void onDiscard() {
    }
}
