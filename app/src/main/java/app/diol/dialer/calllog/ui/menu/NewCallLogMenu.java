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
import android.provider.CallLog.Calls;
import android.view.View;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import app.diol.dialer.calllog.CallLogComponent;
import app.diol.dialer.calllog.model.CoalescedRow;
import app.diol.dialer.common.concurrent.DefaultFutureCallback;
import app.diol.dialer.historyitemactions.HistoryItemActionBottomSheet;

/**
 * Handles configuration of the bottom sheet menus for call log entries.
 */
public final class NewCallLogMenu {

    /**
     * Creates and returns the OnClickListener which opens the menu for the provided row.
     */
    public static View.OnClickListener createOnClickListener(Context context, CoalescedRow row) {
        return view -> {
            HistoryItemActionBottomSheet.show(
                    context, BottomSheetHeader.fromRow(context, row), Modules.fromRow(context, row));

            // If the user opens the bottom sheet for an unread call, clear the notifications and make the
            // row not bold immediately. To do this, mark all of the calls in group as read.
            if (!row.getIsRead() && row.getCallType() == Calls.MISSED_TYPE) {
                Futures.addCallback(
                        CallLogComponent.get(context)
                                .getClearMissedCalls()
                                .clearBySystemCallLogId(row.getCoalescedIds().getCoalescedIdList()),
                        new DefaultFutureCallback<>(),
                        MoreExecutors.directExecutor());
            }
        };
    }
}
