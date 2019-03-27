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

package app.diol.incallui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.contacts.ContactsComponent;
import app.diol.incallui.ContactInfoCache.ContactCacheEntry;
import app.diol.incallui.ContactInfoCache.ContactInfoCacheCallback;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;

/**
 * Dialog that shown to user when receiving RTT request mid call.
 */
public class RttRequestDialogFragment extends DialogFragment {

    /**
     * Key in the arguments bundle for call id.
     */
    private static final String ARG_CALL_ID = "call_id";
    private static final String ARG_RTT_REQUEST_ID = "rtt_request_id";
    private TextView detailsTextView;

    /**
     * Returns a new instance of {@link RttRequestDialogFragment} with the given callback.
     *
     * <p>Prefer this method over the default constructor.
     */
    public static RttRequestDialogFragment newInstance(String callId, int rttRequestId) {
        RttRequestDialogFragment fragment = new RttRequestDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CALL_ID, Assert.isNotNull(callId));
        args.putInt(ARG_RTT_REQUEST_ID, rttRequestId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        super.onCreateDialog(bundle);
        LogUtil.enterBlock("RttRequestDialogFragment.onCreateDialog");

        View dialogView = View.inflate(getActivity(), R.layout.frag_rtt_request_dialog, null);
        detailsTextView = dialogView.findViewById(R.id.details);

        ContactInfoCache cache = ContactInfoCache.getInstance(getContext());
        DialerCall dialerCall =
                CallList.getInstance().getCallById(getArguments().getString(ARG_CALL_ID));
        cache.findInfo(dialerCall, false, new ContactLookupCallback(this));

        dialogView
                .findViewById(R.id.rtt_button_decline_request)
                .setOnClickListener(v -> onNegativeButtonClick());
        dialogView
                .findViewById(R.id.rtt_button_accept_request)
                .setOnClickListener(v -> onPositiveButtonClick());

        AlertDialog alertDialog =
                new AlertDialog.Builder(getActivity())
                        .setCancelable(false)
                        .setView(dialogView)
                        .setTitle(R.string.rtt_request_dialog_title)
                        .create();

        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    private void onPositiveButtonClick() {
        LogUtil.enterBlock("RttRequestDialogFragment.onPositiveButtonClick");

        DialerCall call = CallList.getInstance().getCallById(getArguments().getString(ARG_CALL_ID));
        call.respondToRttRequest(true, getArguments().getInt(ARG_RTT_REQUEST_ID));
        dismiss();
    }

    private void onNegativeButtonClick() {
        LogUtil.enterBlock("RttRequestDialogFragment.onNegativeButtonClick");

        DialerCall call = CallList.getInstance().getCallById(getArguments().getString(ARG_CALL_ID));
        call.respondToRttRequest(false, getArguments().getInt(ARG_RTT_REQUEST_ID));
        dismiss();
    }

    private void setNameOrNumber(CharSequence nameOrNumber) {
        detailsTextView.setText(getString(R.string.rtt_request_dialog_details, nameOrNumber));
    }

    private static class ContactLookupCallback implements ContactInfoCacheCallback {
        private final WeakReference<RttRequestDialogFragment> rttRequestDialogFragmentWeakReference;

        private ContactLookupCallback(RttRequestDialogFragment rttRequestDialogFragment) {
            rttRequestDialogFragmentWeakReference = new WeakReference<>(rttRequestDialogFragment);
        }

        @Override
        public void onContactInfoComplete(String callId, ContactCacheEntry entry) {
            RttRequestDialogFragment fragment = rttRequestDialogFragmentWeakReference.get();
            if (fragment != null) {
                fragment.setNameOrNumber(getNameOrNumber(entry, fragment.getContext()));
            }
        }

        private CharSequence getNameOrNumber(ContactCacheEntry entry, Context context) {
            String preferredName =
                    ContactsComponent.get(context)
                            .contactDisplayPreferences()
                            .getDisplayName(entry.namePrimary, entry.nameAlternative);
            if (TextUtils.isEmpty(preferredName)) {
                return TextUtils.isEmpty(entry.number)
                        ? null
                        : PhoneNumberUtils.createTtsSpannable(
                        BidiFormatter.getInstance().unicodeWrap(entry.number, TextDirectionHeuristics.LTR));
            }
            return preferredName;
        }

        @Override
        public void onImageLoadComplete(String callId, ContactCacheEntry entry) {
        }
    }
}
