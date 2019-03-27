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

import app.diol.dialer.calldetails.CallDetailsEntryViewHolder.CallDetailsEntryListener;
import app.diol.dialer.calldetails.CallDetailsFooterViewHolder.DeleteCallDetailsListener;
import app.diol.dialer.calldetails.CallDetailsFooterViewHolder.ReportCallIdListener;
import app.diol.dialer.calldetails.CallDetailsHeaderViewHolder.CallDetailsHeaderListener;
import app.diol.dialer.common.Assert;
import app.diol.dialer.dialercontact.DialerContact;
import app.diol.dialer.protos.ProtoParsers;

/**
 * Displays the details of a specific call log entry.
 *
 * <p>This activity is for the old call log.
 *
 * <p>See {@link CallDetailsAdapterCommon} for logic shared between this activity and the one for
 * the new call log.
 */
public final class OldCallDetailsActivity extends CallDetailsActivityCommon {
    public static final String EXTRA_CALL_DETAILS_ENTRIES = "call_details_entries";
    public static final String EXTRA_CONTACT = "contact";

    /**
     * Contains info to be shown in the header.
     */
    private DialerContact contact;

    public static boolean isLaunchIntent(Intent intent) {
        return intent.getComponent() != null
                && OldCallDetailsActivity.class.getName().equals(intent.getComponent().getClassName());
    }

    /**
     * Returns an {@link Intent} to launch this activity.
     */
    public static Intent newInstance(
            Context context,
            CallDetailsEntries details,
            DialerContact contact,
            boolean canReportCallerId,
            boolean canSupportAssistedDialing) {
        Intent intent = new Intent(context, OldCallDetailsActivity.class);
        ProtoParsers.put(intent, EXTRA_CONTACT, Assert.isNotNull(contact));
        ProtoParsers.put(intent, EXTRA_CALL_DETAILS_ENTRIES, Assert.isNotNull(details));
        intent.putExtra(EXTRA_CAN_REPORT_CALLER_ID, canReportCallerId);
        intent.putExtra(EXTRA_CAN_SUPPORT_ASSISTED_DIALING, canSupportAssistedDialing);
        return intent;
    }

    @Override
    protected void handleIntent(Intent intent) {
        Assert.checkArgument(intent.hasExtra(EXTRA_CONTACT));
        Assert.checkArgument(intent.hasExtra(EXTRA_CALL_DETAILS_ENTRIES));
        Assert.checkArgument(intent.hasExtra(EXTRA_CAN_REPORT_CALLER_ID));
        Assert.checkArgument(intent.hasExtra(EXTRA_CAN_SUPPORT_ASSISTED_DIALING));

        contact = ProtoParsers.getTrusted(intent, EXTRA_CONTACT, DialerContact.getDefaultInstance());
        setCallDetailsEntries(
                ProtoParsers.getTrusted(
                        intent, EXTRA_CALL_DETAILS_ENTRIES, CallDetailsEntries.getDefaultInstance()));
        loadRttTranscriptAvailability();
    }

    @Override
    protected CallDetailsAdapterCommon createAdapter(
            CallDetailsEntryListener callDetailsEntryListener,
            CallDetailsHeaderListener callDetailsHeaderListener,
            ReportCallIdListener reportCallIdListener,
            DeleteCallDetailsListener deleteCallDetailsListener) {
        return new OldCallDetailsAdapter(
                /* context = */ this,
                contact,
                getCallDetailsEntries(),
                callDetailsEntryListener,
                callDetailsHeaderListener,
                reportCallIdListener,
                deleteCallDetailsListener);
    }

    @Override
    protected String getNumber() {
        return contact.getNumber();
    }
}
