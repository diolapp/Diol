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
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.diol.R;
import app.diol.dialer.calldetails.CallDetailsEntries.CallDetailsEntry;
import app.diol.dialer.calldetails.CallDetailsEntryViewHolder.CallDetailsEntryListener;
import app.diol.dialer.calldetails.CallDetailsFooterViewHolder.DeleteCallDetailsListener;
import app.diol.dialer.calldetails.CallDetailsFooterViewHolder.ReportCallIdListener;
import app.diol.dialer.calldetails.CallDetailsHeaderViewHolder.CallDetailsHeaderListener;
import app.diol.dialer.calllogutils.CallTypeHelper;
import app.diol.dialer.calllogutils.CallbackActionHelper;
import app.diol.dialer.calllogutils.CallbackActionHelper.CallbackAction;
import app.diol.dialer.common.Assert;
import app.diol.dialer.duo.DuoComponent;
import app.diol.dialer.glidephotomanager.PhotoInfo;

/**
 * Contains common logic shared between {@link OldCallDetailsAdapter} and {@link
 * CallDetailsAdapter}.
 */
abstract class CallDetailsAdapterCommon extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_VIEW_TYPE = 1;
    private static final int CALL_ENTRY_VIEW_TYPE = 2;
    private static final int FOOTER_VIEW_TYPE = 3;

    private final CallDetailsEntryListener callDetailsEntryListener;
    private final CallDetailsHeaderListener callDetailsHeaderListener;
    private final ReportCallIdListener reportCallIdListener;
    private final DeleteCallDetailsListener deleteCallDetailsListener;
    private final CallTypeHelper callTypeHelper;

    private CallDetailsEntries callDetailsEntries;

    CallDetailsAdapterCommon(
            Context context,
            CallDetailsEntries callDetailsEntries,
            CallDetailsEntryListener callDetailsEntryListener,
            CallDetailsHeaderListener callDetailsHeaderListener,
            ReportCallIdListener reportCallIdListener,
            DeleteCallDetailsListener deleteCallDetailsListener) {
        this.callDetailsEntries = callDetailsEntries;
        this.callDetailsEntryListener = callDetailsEntryListener;
        this.callDetailsHeaderListener = callDetailsHeaderListener;
        this.reportCallIdListener = reportCallIdListener;
        this.deleteCallDetailsListener = deleteCallDetailsListener;
        this.callTypeHelper =
                new CallTypeHelper(context.getResources(), DuoComponent.get(context).getDuo());
    }

    protected abstract void bindCallDetailsHeaderViewHolder(
            CallDetailsHeaderViewHolder viewHolder, int position);

    protected abstract CallDetailsHeaderViewHolder createCallDetailsHeaderViewHolder(
            View container, CallDetailsHeaderListener callDetailsHeaderListener);

    /**
     * Returns the phone number of the call details.
     */
    protected abstract String getNumber();

    /**
     * Returns the primary text shown on call details toolbar, usually contact name or number.
     */
    protected abstract String getPrimaryText();

    /**
     * Returns {@link PhotoInfo} of the contact.
     */
    protected abstract PhotoInfo getPhotoInfo();

    @Override
    @CallSuper
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case HEADER_VIEW_TYPE:
                return createCallDetailsHeaderViewHolder(
                        inflater.inflate(R.layout.contact_container, parent, false), callDetailsHeaderListener);
            case CALL_ENTRY_VIEW_TYPE:
                return new CallDetailsEntryViewHolder(
                        inflater.inflate(R.layout.call_details_entry, parent, false), callDetailsEntryListener);
            case FOOTER_VIEW_TYPE:
                return new CallDetailsFooterViewHolder(
                        inflater.inflate(R.layout.call_details_footer, parent, false),
                        reportCallIdListener,
                        deleteCallDetailsListener);
            default:
                throw Assert.createIllegalStateFailException(
                        "No ViewHolder available for viewType: " + viewType);
        }
    }

    @Override
    @CallSuper
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) { // Header
            bindCallDetailsHeaderViewHolder((CallDetailsHeaderViewHolder) holder, position);
        } else if (position == getItemCount() - 1) {
            ((CallDetailsFooterViewHolder) holder).setPhoneNumber(getNumber());
        } else {
            CallDetailsEntryViewHolder viewHolder = (CallDetailsEntryViewHolder) holder;
            CallDetailsEntry entry = callDetailsEntries.getEntries(position - 1);
            viewHolder.setCallDetails(
                    getNumber(),
                    getPrimaryText(),
                    getPhotoInfo(),
                    entry,
                    callTypeHelper,
                    !entry.getHistoryResultsList().isEmpty() && position != getItemCount() - 2);
        }
    }

    @Override
    @CallSuper
    public int getItemViewType(int position) {
        if (position == 0) { // Header
            return HEADER_VIEW_TYPE;
        } else if (position == getItemCount() - 1) {
            return FOOTER_VIEW_TYPE;
        } else {
            return CALL_ENTRY_VIEW_TYPE;
        }
    }

    @Override
    @CallSuper
    public int getItemCount() {
        return callDetailsEntries.getEntriesCount() == 0
                ? 0
                : callDetailsEntries.getEntriesCount() + 2; // plus header and footer
    }

    final CallDetailsEntries getCallDetailsEntries() {
        return callDetailsEntries;
    }

    @MainThread
    final void updateCallDetailsEntries(CallDetailsEntries entries) {
        Assert.isMainThread();
        callDetailsEntries = entries;
        notifyDataSetChanged();
    }

    final @CallbackAction
    int getCallbackAction() {
        Assert.checkState(!callDetailsEntries.getEntriesList().isEmpty());

        CallDetailsEntry entry = callDetailsEntries.getEntries(0);
        return CallbackActionHelper.getCallbackAction(
                getNumber(), entry.getFeatures(), entry.getIsDuoCall());
    }
}
