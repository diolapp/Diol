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

package app.diol.dialer.calllog.ui.menu;

import android.content.Context;

import app.diol.dialer.calllog.model.CoalescedRow;
import app.diol.dialer.calllogutils.CallLogEntryText;
import app.diol.dialer.calllogutils.PhotoInfoBuilder;
import app.diol.dialer.historyitemactions.HistoryItemBottomSheetHeaderInfo;

/**
 * Configures the top row in the bottom sheet.
 */
final class BottomSheetHeader {

    static HistoryItemBottomSheetHeaderInfo fromRow(Context context, CoalescedRow row) {
        return HistoryItemBottomSheetHeaderInfo.newBuilder()
                .setNumber(row.getNumber())
                .setPhotoInfo(PhotoInfoBuilder.fromCoalescedRow(context, row))
                .setPrimaryText(CallLogEntryText.buildPrimaryText(context, row).toString())
                .setSecondaryText(
                        CallLogEntryText.buildSecondaryTextForBottomSheet(context, row).toString())
                .build();
    }
}
