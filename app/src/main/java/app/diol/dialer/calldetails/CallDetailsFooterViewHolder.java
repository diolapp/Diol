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

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import app.diol.R;
import app.diol.dialer.clipboard.ClipboardUtils;
import app.diol.dialer.common.Assert;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.logging.UiAction;
import app.diol.dialer.performancereport.PerformanceReport;
import app.diol.dialer.util.CallUtil;
import app.diol.dialer.util.DialerUtils;

/**
 * ViewHolder for the footer in {@link OldCallDetailsActivity} or {@link CallDetailsActivity}.
 */
final class CallDetailsFooterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final ReportCallIdListener reportCallIdListener;
    private final DeleteCallDetailsListener deleteCallDetailsListener;
    private final View copy;
    private final View edit;
    private final View reportCallerId;
    private final View delete;

    private String number;

    CallDetailsFooterViewHolder(
            View view,
            ReportCallIdListener reportCallIdListener,
            DeleteCallDetailsListener deleteCallDetailsListener) {
        super(view);
        this.reportCallIdListener = reportCallIdListener;
        this.deleteCallDetailsListener = deleteCallDetailsListener;
        copy = view.findViewById(R.id.call_detail_action_copy);
        edit = view.findViewById(R.id.call_detail_action_edit_before_call);
        reportCallerId = view.findViewById(R.id.call_detail_action_report_caller_id);
        delete = view.findViewById(R.id.call_detail_action_delete);
        copy.setOnClickListener(this);
        edit.setOnClickListener(this);
        reportCallerId.setOnClickListener(this);
        delete.setOnClickListener(this);
    }

    public void setPhoneNumber(String number) {
        this.number = number;
        if (TextUtils.isEmpty(number)) {
            copy.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        } else if (reportCallIdListener.canReportCallerId(number)) {
            reportCallerId.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        Context context = view.getContext();
        if (view == copy) {
            PerformanceReport.recordClick(UiAction.Type.COPY_NUMBER_IN_CALL_DETAIL);

            Logger.get(context).logImpression(DialerImpression.Type.CALL_DETAILS_COPY_NUMBER);
            ClipboardUtils.copyText(context, null, number, true);
        } else if (view == edit) {
            PerformanceReport.recordClick(UiAction.Type.EDIT_NUMBER_BEFORE_CALL_IN_CALL_DETAIL);
            // Dialpad will be filled with this number, but we don't want to record it as user action
            PerformanceReport.setIgnoreActionOnce(UiAction.Type.TEXT_CHANGE_WITH_INPUT);

            Logger.get(context).logImpression(DialerImpression.Type.CALL_DETAILS_EDIT_BEFORE_CALL);
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, CallUtil.getCallUri(number));
            DialerUtils.startActivityWithErrorToast(context, dialIntent);
        } else if (view == reportCallerId) {
            reportCallIdListener.reportCallId(number);
        } else if (view == delete) {
            deleteCallDetailsListener.delete();
        } else {
            throw Assert.createUnsupportedOperationFailException(
                    "View on click not implemented: " + view);
        }
    }

    /**
     * Listener for reporting caller id
     */
    interface ReportCallIdListener {

        /**
         * Tell listener that the user requested to report caller id info as inaccurate.
         */
        void reportCallId(String number);

        /**
         * returns true if the number can be reported as inaccurate.
         */
        boolean canReportCallerId(String number);
    }

    /**
     * Listener for deleting call details
     */
    interface DeleteCallDetailsListener {

        /**
         * Delete call details
         */
        void delete();
    }
}
