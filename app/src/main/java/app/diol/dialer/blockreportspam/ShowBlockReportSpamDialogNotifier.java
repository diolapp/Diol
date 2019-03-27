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

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.protos.ProtoParsers;

/**
 * Notifies that a dialog for blocking a number and/or marking it as spam/not spam should be shown.
 */
public final class ShowBlockReportSpamDialogNotifier {

    private ShowBlockReportSpamDialogNotifier() {
    }

    /**
     * Notifies that a dialog for blocking a number and optionally report it as spam should be shown.
     */
    public static void notifyShowDialogToBlockNumberAndOptionallyReportSpam(
            Context context, BlockReportSpamDialogInfo blockReportSpamDialogInfo) {
        LogUtil.enterBlock(
                "ShowBlockReportSpamDialogNotifier.notifyShowDialogToBlockNumberAndOptionallyReportSpam");

        Intent intent = new Intent();
        intent.setAction(
                ShowBlockReportSpamDialogReceiver
                        .ACTION_SHOW_DIALOG_TO_BLOCK_NUMBER_AND_OPTIONALLY_REPORT_SPAM);
        ProtoParsers.put(
                intent, ShowBlockReportSpamDialogReceiver.EXTRA_DIALOG_INFO, blockReportSpamDialogInfo);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Notifies that a dialog for blocking a number should be shown.
     */
    public static void notifyShowDialogToBlockNumber(
            Context context, BlockReportSpamDialogInfo blockReportSpamDialogInfo) {
        LogUtil.enterBlock("ShowBlockReportSpamDialogNotifier.notifyShowDialogToBlockNumber");

        Intent intent = new Intent();
        intent.setAction(ShowBlockReportSpamDialogReceiver.ACTION_SHOW_DIALOG_TO_BLOCK_NUMBER);
        ProtoParsers.put(
                intent, ShowBlockReportSpamDialogReceiver.EXTRA_DIALOG_INFO, blockReportSpamDialogInfo);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Notifies that a dialog for reporting a number as not spam should be shown.
     */
    public static void notifyShowDialogToReportNotSpam(
            Context context, BlockReportSpamDialogInfo blockReportSpamDialogInfo) {
        LogUtil.enterBlock("ShowBlockReportSpamDialogNotifier.notifyShowDialogToReportNotSpam");

        Intent intent = new Intent();
        intent.setAction(ShowBlockReportSpamDialogReceiver.ACTION_SHOW_DIALOG_TO_REPORT_NOT_SPAM);
        ProtoParsers.put(
                intent, ShowBlockReportSpamDialogReceiver.EXTRA_DIALOG_INFO, blockReportSpamDialogInfo);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Notifies that a dialog for unblocking a number should be shown.
     */
    public static void notifyShowDialogToUnblockNumber(
            Context context, BlockReportSpamDialogInfo blockReportSpamDialogInfo) {
        LogUtil.enterBlock("ShowBlockReportSpamDialogNotifier.notifyShowDialogToUnblockNumber");

        Intent intent = new Intent();
        intent.setAction(ShowBlockReportSpamDialogReceiver.ACTION_SHOW_DIALOG_TO_UNBLOCK_NUMBER);
        ProtoParsers.put(
                intent, ShowBlockReportSpamDialogReceiver.EXTRA_DIALOG_INFO, blockReportSpamDialogInfo);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
