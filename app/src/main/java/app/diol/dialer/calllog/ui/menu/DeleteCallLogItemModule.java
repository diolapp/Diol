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

import android.Manifest.permission;
import android.content.Context;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import app.diol.R;
import app.diol.dialer.CoalescedIds;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.common.database.Selection;
import app.diol.dialer.historyitemactions.HistoryItemActionModule;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;

/**
 * {@link HistoryItemActionModule} for deleting a call log item in the new call log.
 */
final class DeleteCallLogItemModule implements HistoryItemActionModule {
    private static final String TAG = DeleteCallLogItemModule.class.getName();

    private final Context context;
    private final CoalescedIds coalescedIds;

    DeleteCallLogItemModule(Context context, CoalescedIds coalescedIds) {
        this.context = context;
        this.coalescedIds = coalescedIds;
    }

    @Override
    public int getStringId() {
        return R.string.delete;
    }

    @Override
    public int getDrawableId() {
        return R.drawable.quantum_ic_delete_vd_theme_24;
    }

    @Override
    public boolean onClick() {
        DialerExecutorComponent.get(context)
                .dialerExecutorFactory()
                .createNonUiTaskBuilder(new CallLogItemDeletionWorker(context))
                .build()
                .executeSerial(coalescedIds);

        Logger.get(context).logImpression(DialerImpression.Type.USER_DELETED_CALL_LOG_ITEM);
        return true;
    }

    /**
     * A {@link Worker} that deletes a call log item.
     *
     * <p>It takes as input the IDs of all call log records that are coalesced into the item to be
     * deleted.
     */
    private static class CallLogItemDeletionWorker implements Worker<CoalescedIds, Void> {
        private final WeakReference<Context> contextWeakReference;

        CallLogItemDeletionWorker(Context context) {
            contextWeakReference = new WeakReference<>(context);
        }

        private static List<String> getCallLogIdsAsStrings(CoalescedIds coalescedIds) {
            Assert.checkArgument(coalescedIds.getCoalescedIdCount() > 0);

            List<String> idStrings = new ArrayList<>(coalescedIds.getCoalescedIdCount());

            for (long callLogId : coalescedIds.getCoalescedIdList()) {
                idStrings.add(String.valueOf(callLogId));
            }

            return idStrings;
        }

        @Nullable
        @Override
        @RequiresPermission(value = permission.WRITE_CALL_LOG)
        public Void doInBackground(CoalescedIds coalescedIds) throws Throwable {
            Context context = contextWeakReference.get();
            if (context == null) {
                LogUtil.e(TAG, "Unable to delete an call log item due to null context.");
                return null;
            }

            Selection selection =
                    Selection.builder()
                            .and(Selection.column(CallLog.Calls._ID).in(getCallLogIdsAsStrings(coalescedIds)))
                            .build();
            int numRowsDeleted =
                    context
                            .getContentResolver()
                            .delete(Calls.CONTENT_URI, selection.getSelection(), selection.getSelectionArgs());

            if (numRowsDeleted != coalescedIds.getCoalescedIdCount()) {
                LogUtil.e(
                        TAG,
                        "Deleting call log item is unsuccessful. %d of %d rows are deleted.",
                        numRowsDeleted,
                        coalescedIds.getCoalescedIdCount());
            }

            return null;
        }
    }
}
