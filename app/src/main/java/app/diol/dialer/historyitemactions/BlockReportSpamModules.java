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

package app.diol.dialer.historyitemactions;

import android.content.Context;

import java.util.Optional;

import app.diol.R;
import app.diol.dialer.blockreportspam.BlockReportSpamDialogInfo;
import app.diol.dialer.blockreportspam.ShowBlockReportSpamDialogNotifier;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;

/**
 * Modules for blocking/unblocking a number and/or reporting it as spam/not spam.
 */
final class BlockReportSpamModules {

    private BlockReportSpamModules() {
    }

    static HistoryItemActionModule moduleForMarkingNumberAsNotSpam(
            Context context,
            BlockReportSpamDialogInfo blockReportSpamDialogInfo,
            Optional<DialerImpression.Type> impression) {

        return new HistoryItemActionModule() {
            @Override
            public int getStringId() {
                return R.string.not_spam;
            }

            @Override
            public int getDrawableId() {
                return R.drawable.quantum_ic_report_off_vd_theme_24;
            }

            @Override
            public boolean onClick() {
                ShowBlockReportSpamDialogNotifier.notifyShowDialogToReportNotSpam(
                        context, blockReportSpamDialogInfo);

                impression.ifPresent(Logger.get(context)::logImpression);
                return true; // Close the bottom sheet.
            }
        };
    }

    static HistoryItemActionModule moduleForBlockingNumber(
            Context context,
            BlockReportSpamDialogInfo blockReportSpamDialogInfo,
            Optional<DialerImpression.Type> impression) {

        return new HistoryItemActionModule() {
            @Override
            public int getStringId() {
                return R.string.block_number;
            }

            @Override
            public int getDrawableId() {
                return R.drawable.quantum_ic_block_vd_theme_24;
            }

            @Override
            public boolean onClick() {
                ShowBlockReportSpamDialogNotifier.notifyShowDialogToBlockNumber(
                        context, blockReportSpamDialogInfo);

                impression.ifPresent(Logger.get(context)::logImpression);
                return true; // Close the bottom sheet.
            }
        };
    }

    static HistoryItemActionModule moduleForUnblockingNumber(
            Context context,
            BlockReportSpamDialogInfo blockReportSpamDialogInfo,
            Optional<DialerImpression.Type> impression) {

        return new HistoryItemActionModule() {
            @Override
            public int getStringId() {
                return R.string.unblock_number;
            }

            @Override
            public int getDrawableId() {
                return R.drawable.quantum_ic_unblock_vd_theme_24;
            }

            @Override
            public boolean onClick() {
                ShowBlockReportSpamDialogNotifier.notifyShowDialogToUnblockNumber(
                        context, blockReportSpamDialogInfo);

                impression.ifPresent(Logger.get(context)::logImpression);
                return true; // Close the bottom sheet.
            }
        };
    }

    static HistoryItemActionModule moduleForBlockingNumberAndOptionallyReportingSpam(
            Context context,
            BlockReportSpamDialogInfo blockReportSpamDialogInfo,
            Optional<DialerImpression.Type> impression) {

        return new HistoryItemActionModule() {
            @Override
            public int getStringId() {
                return R.string.block_and_optionally_report_spam;
            }

            @Override
            public int getDrawableId() {
                return R.drawable.quantum_ic_block_vd_theme_24;
            }

            @Override
            public boolean onClick() {
                ShowBlockReportSpamDialogNotifier.notifyShowDialogToBlockNumberAndOptionallyReportSpam(
                        context, blockReportSpamDialogInfo);

                impression.ifPresent(Logger.get(context)::logImpression);
                return true; // Close the bottom sheet.
            }
        };
    }
}
