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

package app.diol.dialer.calldetails;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import app.diol.dialer.CoalescedIds;
import app.diol.dialer.calldetails.CallDetailsEntryViewHolder.CallDetailsEntryListener;
import app.diol.dialer.calldetails.CallDetailsFooterViewHolder.DeleteCallDetailsListener;
import app.diol.dialer.calldetails.CallDetailsFooterViewHolder.ReportCallIdListener;
import app.diol.dialer.calldetails.CallDetailsHeaderViewHolder.CallDetailsHeaderListener;
import app.diol.dialer.calllog.database.contract.AnnotatedCallLogContract.AnnotatedCallLog;
import app.diol.dialer.common.Assert;
import app.diol.dialer.enrichedcall.EnrichedCallComponent;
import app.diol.dialer.protos.ProtoParsers;

/**
 * Displays the details of a specific call log entry.
 *
 * <p>This activity is for the new call log.
 *
 * <p>See {@link CallDetailsAdapterCommon} for logic shared between this activity and the one for
 * the old call log.
 */
public final class CallDetailsActivity extends CallDetailsActivityCommon {
    public static final String EXTRA_COALESCED_CALL_LOG_IDS = "coalesced_call_log_ids";
    public static final String EXTRA_HEADER_INFO = "header_info";

    private static final int CALL_DETAILS_LOADER_ID = 0;

    /**
     * IDs of call log entries, used to retrieve them from the annotated call log.
     */
    private CoalescedIds coalescedCallLogIds;

    /**
     * Info to be shown in the header.
     */
    private CallDetailsHeaderInfo headerInfo;

    /**
     * Returns an {@link Intent} to launch this activity.
     */
    public static Intent newInstance(
            Context context,
            CoalescedIds coalescedAnnotatedCallLogIds,
            CallDetailsHeaderInfo callDetailsHeaderInfo,
            boolean canReportCallerId,
            boolean canSupportAssistedDialing) {
        Intent intent = new Intent(context, CallDetailsActivity.class);
        ProtoParsers.put(
                intent, EXTRA_COALESCED_CALL_LOG_IDS, Assert.isNotNull(coalescedAnnotatedCallLogIds));
        ProtoParsers.put(intent, EXTRA_HEADER_INFO, Assert.isNotNull(callDetailsHeaderInfo));
        intent.putExtra(EXTRA_CAN_REPORT_CALLER_ID, canReportCallerId);
        intent.putExtra(EXTRA_CAN_SUPPORT_ASSISTED_DIALING, canSupportAssistedDialing);
        return intent;
    }

    @Override
    protected void handleIntent(Intent intent) {
        Assert.checkArgument(intent.hasExtra(EXTRA_COALESCED_CALL_LOG_IDS));
        Assert.checkArgument(intent.hasExtra(EXTRA_HEADER_INFO));
        Assert.checkArgument(intent.hasExtra(EXTRA_CAN_REPORT_CALLER_ID));
        Assert.checkArgument(intent.hasExtra(EXTRA_CAN_SUPPORT_ASSISTED_DIALING));

        setCallDetailsEntries(CallDetailsEntries.getDefaultInstance());
        coalescedCallLogIds =
                ProtoParsers.getTrusted(
                        intent, EXTRA_COALESCED_CALL_LOG_IDS, CoalescedIds.getDefaultInstance());
        headerInfo =
                ProtoParsers.getTrusted(
                        intent, EXTRA_HEADER_INFO, CallDetailsHeaderInfo.getDefaultInstance());

        getLoaderManager()
                .initLoader(
                        CALL_DETAILS_LOADER_ID, /* args = */ null, new CallDetailsLoaderCallbacks(this));
    }

    @Override
    protected CallDetailsAdapterCommon createAdapter(
            CallDetailsEntryListener callDetailsEntryListener,
            CallDetailsHeaderListener callDetailsHeaderListener,
            ReportCallIdListener reportCallIdListener,
            DeleteCallDetailsListener deleteCallDetailsListener) {
        return new CallDetailsAdapter(
                this,
                headerInfo,
                getCallDetailsEntries(),
                callDetailsEntryListener,
                callDetailsHeaderListener,
                reportCallIdListener,
                deleteCallDetailsListener);
    }

    @Override
    protected String getNumber() {
        return headerInfo.getDialerPhoneNumber().getNormalizedNumber();
    }

    /**
     * {@link LoaderCallbacks} for {@link CallDetailsCursorLoader}, which loads call detail entries
     * from {@link AnnotatedCallLog}.
     */
    private static final class CallDetailsLoaderCallbacks implements LoaderCallbacks<Cursor> {
        private final CallDetailsActivity activity;

        CallDetailsLoaderCallbacks(CallDetailsActivity callDetailsActivity) {
            this.activity = callDetailsActivity;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CallDetailsCursorLoader(activity, Assert.isNotNull(activity.coalescedCallLogIds));
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            updateCallDetailsEntries(CallDetailsCursorLoader.toCallDetailsEntries(activity, data));
            activity.loadRttTranscriptAvailability();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            updateCallDetailsEntries(CallDetailsEntries.getDefaultInstance());
        }

        private void updateCallDetailsEntries(CallDetailsEntries newEntries) {
            activity.setCallDetailsEntries(newEntries);
            EnrichedCallComponent.get(activity)
                    .getEnrichedCallManager()
                    .requestAllHistoricalData(activity.getNumber(), newEntries);
        }
    }
}
