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

package app.diol.dialer.blockreportspam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import app.diol.R;
import app.diol.dialer.blocking.Blocking;
import app.diol.dialer.blocking.Blocking.BlockingFailedException;
import app.diol.dialer.blockreportspam.BlockReportSpamDialogs.DialogFragmentForBlockingNumber;
import app.diol.dialer.blockreportspam.BlockReportSpamDialogs.DialogFragmentForBlockingNumberAndOptionallyReportingAsSpam;
import app.diol.dialer.blockreportspam.BlockReportSpamDialogs.DialogFragmentForReportingNotSpam;
import app.diol.dialer.blockreportspam.BlockReportSpamDialogs.DialogFragmentForUnblockingNumber;
import app.diol.dialer.blockreportspam.BlockReportSpamDialogs.OnConfirmListener;
import app.diol.dialer.blockreportspam.BlockReportSpamDialogs.OnSpamDialogClickListener;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.DialerImpression.Type;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.protos.ProtoParsers;
import app.diol.dialer.spam.Spam;
import app.diol.dialer.spam.SpamComponent;
import app.diol.dialer.spam.SpamSettings;

/**
 * A {@link BroadcastReceiver} that shows an appropriate dialog upon receiving notifications from
 * {@link ShowBlockReportSpamDialogNotifier}.
 */
public final class ShowBlockReportSpamDialogReceiver extends BroadcastReceiver {

    static final String ACTION_SHOW_DIALOG_TO_BLOCK_NUMBER = "show_dialog_to_block_number";
    static final String ACTION_SHOW_DIALOG_TO_BLOCK_NUMBER_AND_OPTIONALLY_REPORT_SPAM =
            "show_dialog_to_block_number_and_optionally_report_spam";
    static final String ACTION_SHOW_DIALOG_TO_REPORT_NOT_SPAM = "show_dialog_to_report_not_spam";
    static final String ACTION_SHOW_DIALOG_TO_UNBLOCK_NUMBER = "show_dialog_to_unblock_number";
    static final String EXTRA_DIALOG_INFO = "dialog_info";

    /**
     * {@link FragmentManager} needed to show a {@link android.app.DialogFragment}.
     */
    private final FragmentManager fragmentManager;

    public ShowBlockReportSpamDialogReceiver(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    /**
     * Returns an {@link IntentFilter} containing all actions accepted by this broadcast receiver.
     */
    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SHOW_DIALOG_TO_BLOCK_NUMBER_AND_OPTIONALLY_REPORT_SPAM);
        intentFilter.addAction(ACTION_SHOW_DIALOG_TO_BLOCK_NUMBER);
        intentFilter.addAction(ACTION_SHOW_DIALOG_TO_REPORT_NOT_SPAM);
        intentFilter.addAction(ACTION_SHOW_DIALOG_TO_UNBLOCK_NUMBER);
        return intentFilter;
    }

    private static void blockNumber(Context context, BlockReportSpamDialogInfo dialogInfo) {
        Logger.get(context).logImpression(Type.USER_ACTION_BLOCKED_NUMBER);
        Futures.addCallback(
                Blocking.block(
                        context,
                        ImmutableList.of(dialogInfo.getNormalizedNumber()),
                        dialogInfo.getCountryIso()),
                new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Do nothing
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        if (throwable instanceof BlockingFailedException) {
                            Logger.get(context).logImpression(Type.USER_ACTION_BLOCK_NUMBER_FAILED);
                            Toast.makeText(context, R.string.block_number_failed_toast, Toast.LENGTH_LONG).show();
                        } else {
                            throw new RuntimeException(throwable);
                        }
                    }
                },
                DialerExecutorComponent.get(context).uiExecutor());
    }

    private static void unblockNumber(Context context, BlockReportSpamDialogInfo dialogInfo) {
        Logger.get(context).logImpression(Type.USER_ACTION_UNBLOCKED_NUMBER);
        Futures.addCallback(
                Blocking.unblock(
                        context,
                        ImmutableList.of(dialogInfo.getNormalizedNumber()),
                        dialogInfo.getCountryIso()),
                new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Do nothing
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        if (throwable instanceof BlockingFailedException) {
                            Logger.get(context).logImpression(Type.USER_ACTION_UNBLOCK_NUMBER_FAILED);
                            Toast.makeText(context, R.string.unblock_number_failed_toast, Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            throw new RuntimeException(throwable);
                        }
                    }
                },
                DialerExecutorComponent.get(context).uiExecutor());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.enterBlock("ShowBlockReportSpamDialogReceiver.onReceive");

        String action = intent.getAction();

        switch (Assert.isNotNull(action)) {
            case ACTION_SHOW_DIALOG_TO_BLOCK_NUMBER:
                showDialogToBlockNumber(context, intent);
                break;
            case ACTION_SHOW_DIALOG_TO_BLOCK_NUMBER_AND_OPTIONALLY_REPORT_SPAM:
                showDialogToBlockNumberAndOptionallyReportSpam(context, intent);
                break;
            case ACTION_SHOW_DIALOG_TO_REPORT_NOT_SPAM:
                showDialogToReportNotSpam(context, intent);
                break;
            case ACTION_SHOW_DIALOG_TO_UNBLOCK_NUMBER:
                showDialogToUnblockNumber(context, intent);
                break;
            default:
                throw new IllegalStateException("Unsupported action: " + action);
        }
    }

    private void showDialogToBlockNumberAndOptionallyReportSpam(Context context, Intent intent) {
        LogUtil.enterBlock(
                "ShowBlockReportSpamDialogReceiver.showDialogToBlockNumberAndOptionallyReportSpam");

        Assert.checkArgument(intent.hasExtra(EXTRA_DIALOG_INFO));
        BlockReportSpamDialogInfo dialogInfo =
                ProtoParsers.getTrusted(
                        intent, EXTRA_DIALOG_INFO, BlockReportSpamDialogInfo.getDefaultInstance());

        Spam spam = SpamComponent.get(context).spam();
        SpamSettings spamSettings = SpamComponent.get(context).spamSettings();

        // Set up the positive listener for the dialog.
        OnSpamDialogClickListener onSpamDialogClickListener =
                reportSpam -> {
                    LogUtil.i(
                            "ShowBlockReportSpamDialogReceiver.showDialogToBlockNumberAndOptionallyReportSpam",
                            "confirmed");

                    if (reportSpam && spamSettings.isSpamEnabled()) {
                        LogUtil.i(
                                "ShowBlockReportSpamDialogReceiver.showDialogToBlockNumberAndOptionallyReportSpam",
                                "report spam");
                        Logger.get(context)
                                .logImpression(
                                        DialerImpression.Type
                                                .REPORT_CALL_AS_SPAM_VIA_CALL_LOG_BLOCK_REPORT_SPAM_SENT_VIA_BLOCK_NUMBER_DIALOG);
                        spam.reportSpamFromCallHistory(
                                dialogInfo.getNormalizedNumber(),
                                dialogInfo.getCountryIso(),
                                dialogInfo.getCallType(),
                                dialogInfo.getReportingLocation(),
                                dialogInfo.getContactSource());
                    }

                    blockNumber(context, dialogInfo);
                };

        // Create and show the dialog.
        DialogFragmentForBlockingNumberAndOptionallyReportingAsSpam.newInstance(
                dialogInfo.getNormalizedNumber(),
                spamSettings.isDialogReportSpamCheckedByDefault(),
                onSpamDialogClickListener,
                /* dismissListener = */ null)
                .show(fragmentManager, BlockReportSpamDialogs.BLOCK_REPORT_SPAM_DIALOG_TAG);
    }

    private void showDialogToBlockNumber(Context context, Intent intent) {
        LogUtil.enterBlock("ShowBlockReportSpamDialogReceiver.showDialogToBlockNumber");

        Assert.checkArgument(intent.hasExtra(EXTRA_DIALOG_INFO));
        BlockReportSpamDialogInfo dialogInfo =
                ProtoParsers.getTrusted(
                        intent, EXTRA_DIALOG_INFO, BlockReportSpamDialogInfo.getDefaultInstance());

        // Set up the positive listener for the dialog.
        OnConfirmListener onConfirmListener =
                () -> {
                    LogUtil.i("ShowBlockReportSpamDialogReceiver.showDialogToBlockNumber", "block number");
                    blockNumber(context, dialogInfo);
                };

        // Create and show the dialog.
        DialogFragmentForBlockingNumber.newInstance(
                dialogInfo.getNormalizedNumber(), onConfirmListener, /* dismissListener = */ null)
                .show(fragmentManager, BlockReportSpamDialogs.BLOCK_DIALOG_TAG);
    }

    private void showDialogToReportNotSpam(Context context, Intent intent) {
        LogUtil.enterBlock("ShowBlockReportSpamDialogReceiver.showDialogToReportNotSpam");

        Assert.checkArgument(intent.hasExtra(EXTRA_DIALOG_INFO));
        BlockReportSpamDialogInfo dialogInfo =
                ProtoParsers.getTrusted(
                        intent, EXTRA_DIALOG_INFO, BlockReportSpamDialogInfo.getDefaultInstance());

        // Set up the positive listener for the dialog.
        OnConfirmListener onConfirmListener =
                () -> {
                    LogUtil.i("ShowBlockReportSpamDialogReceiver.showDialogToReportNotSpam", "confirmed");

                    if (SpamComponent.get(context).spamSettings().isSpamEnabled()) {
                        Logger.get(context)
                                .logImpression(DialerImpression.Type.DIALOG_ACTION_CONFIRM_NUMBER_NOT_SPAM);
                        SpamComponent.get(context)
                                .spam()
                                .reportNotSpamFromCallHistory(
                                        dialogInfo.getNormalizedNumber(),
                                        dialogInfo.getCountryIso(),
                                        dialogInfo.getCallType(),
                                        dialogInfo.getReportingLocation(),
                                        dialogInfo.getContactSource());
                    }
                };

        // Create & show the dialog.
        DialogFragmentForReportingNotSpam.newInstance(
                dialogInfo.getNormalizedNumber(), onConfirmListener, /* dismissListener = */ null)
                .show(fragmentManager, BlockReportSpamDialogs.NOT_SPAM_DIALOG_TAG);
    }

    private void showDialogToUnblockNumber(Context context, Intent intent) {
        LogUtil.enterBlock("ShowBlockReportSpamDialogReceiver.showDialogToUnblockNumber");

        Assert.checkArgument(intent.hasExtra(EXTRA_DIALOG_INFO));
        BlockReportSpamDialogInfo dialogInfo =
                ProtoParsers.getTrusted(
                        intent, EXTRA_DIALOG_INFO, BlockReportSpamDialogInfo.getDefaultInstance());

        // Set up the positive listener for the dialog.
        OnConfirmListener onConfirmListener =
                () -> {
                    LogUtil.i("ShowBlockReportSpamDialogReceiver.showDialogToUnblockNumber", "confirmed");

                    unblockNumber(context, dialogInfo);
                };

        // Create & show the dialog.
        DialogFragmentForUnblockingNumber.newInstance(
                dialogInfo.getNormalizedNumber(), onConfirmListener, /* dismissListener = */ null)
                .show(fragmentManager, BlockReportSpamDialogs.UNBLOCK_DIALOG_TAG);
    }
}
