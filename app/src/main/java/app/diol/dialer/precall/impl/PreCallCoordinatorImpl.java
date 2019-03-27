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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.common.concurrent.SupportUiListener;
import app.diol.dialer.duo.DuoComponent;
import app.diol.dialer.function.Consumer;
import app.diol.dialer.logging.DialerImpression.Type;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.precall.PreCallAction;
import app.diol.dialer.precall.PreCallComponent;
import app.diol.dialer.precall.PreCallCoordinator;
import app.diol.dialer.telecom.TelecomUtil;

/**
 * Implements {@link PreCallCoordinator}. Listens to the life cycle of {@link PreCallActivity} to
 * save/restore states.
 */
public class PreCallCoordinatorImpl implements PreCallCoordinator {

    private static final String SAVED_STATE_CURRENT_ACTION = "current_action";

    @NonNull
    private final AppCompatActivity activity;

    private CallIntentBuilder builder;
    private ImmutableList<PreCallAction> actions;
    private int currentActionIndex = 0;
    private PreCallAction currentAction;
    private PendingAction pendingAction;
    private boolean aborted = false;

    private SupportUiListener<Object> uiListener;

    PreCallCoordinatorImpl(@NonNull AppCompatActivity activity) {
        this.activity = Assert.isNotNull(activity);
    }

    void onCreate(Intent intent, @Nullable Bundle savedInstanceState) {
        LogUtil.enterBlock("PreCallCoordinatorImpl.onCreate");
        if (savedInstanceState != null) {
            currentActionIndex = savedInstanceState.getInt(SAVED_STATE_CURRENT_ACTION);
            builder = Assert.isNotNull(savedInstanceState.getParcelable(EXTRA_CALL_INTENT_BUILDER));
        } else {
            builder = Assert.isNotNull(intent.getParcelableExtra(EXTRA_CALL_INTENT_BUILDER));
        }
        uiListener =
                DialerExecutorComponent.get(activity)
                        .createUiListener(activity.getSupportFragmentManager(), "PreCallCoordinatorImpl.uiListener");
    }

    void onRestoreInstanceState(Bundle savedInstanceState) {
        currentActionIndex = savedInstanceState.getInt(SAVED_STATE_CURRENT_ACTION);
        builder = savedInstanceState.getParcelable(EXTRA_CALL_INTENT_BUILDER);
    }

    void onResume() {
        actions = PreCallComponent.get(activity).createActions();
        runNextAction();
    }

    void onPause() {
        if (currentAction != null) {
            currentAction.onDiscard();
        }
        currentAction = null;
        pendingAction = null;
    }

    void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_STATE_CURRENT_ACTION, currentActionIndex);
        outState.putParcelable(EXTRA_CALL_INTENT_BUILDER, builder);
    }

    private void runNextAction() {
        LogUtil.enterBlock("PreCallCoordinatorImpl.runNextAction");
        Assert.checkArgument(currentAction == null);
        if (currentActionIndex >= actions.size()) {
            placeCall();
            activity.finish();
            return;
        }
        LogUtil.i("PreCallCoordinatorImpl.runNextAction", "running " + actions.get(currentActionIndex));
        currentAction = actions.get(currentActionIndex);
        actions.get(currentActionIndex).runWithUi(this);
        if (pendingAction == null) {
            onActionFinished();
        }
    }

    private void onActionFinished() {
        LogUtil.enterBlock("PreCallCoordinatorImpl.onActionFinished");
        Assert.isNotNull(currentAction);
        currentAction = null;
        currentActionIndex++;
        if (!aborted) {
            runNextAction();
        } else {
            activity.finish();
        }
    }

    @Override
    public void abortCall() {
        Assert.checkState(currentAction != null);
        aborted = true;
        Logger.get(getActivity()).logImpression(Type.PRECALL_CANCELED);
    }

    @NonNull
    @Override
    public CallIntentBuilder getBuilder() {
        return builder;
    }

    @NonNull
    @Override
    public AppCompatActivity getActivity() {
        return activity;
    }

    @Override
    @NonNull
    public PendingAction startPendingAction() {
        Assert.isMainThread();
        Assert.isNotNull(currentAction);
        Assert.checkArgument(pendingAction == null);
        pendingAction = new PendingActionImpl();
        return pendingAction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <OutputT> void listen(
            ListenableFuture<OutputT> future,
            Consumer<OutputT> successListener,
            Consumer<Throwable> failureListener) {

        uiListener.listen(
                activity,
                Futures.transform(future, (output) -> (Object) output, MoreExecutors.directExecutor()),
                output -> successListener.accept((OutputT) output),
                failureListener::accept);
    }

    private void placeCall() {
        if (builder.isDuoCall()) {
            Optional<Intent> intent =
                    DuoComponent.get(activity)
                            .getDuo()
                            .getCallIntent(builder.getUri().getSchemeSpecificPart());
            if (intent.isPresent()) {
                activity.startActivityForResult(intent.get(), 0);
                return;
            } else {
                LogUtil.e("PreCallCoordinatorImpl.placeCall", "duo.getCallIntent() returned absent");
            }
        }
        TelecomUtil.placeCall(activity, builder.build());
    }

    private class PendingActionImpl implements PendingAction {

        @Override
        public void finish() {
            Assert.checkArgument(pendingAction == this);
            pendingAction = null;
            onActionFinished();
        }
    }
}
