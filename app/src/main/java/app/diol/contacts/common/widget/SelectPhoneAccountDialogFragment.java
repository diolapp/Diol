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

package app.diol.contacts.common.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.common.base.Optional;

import app.diol.R;
import app.diol.contacts.common.compat.PhoneAccountCompat;
import app.diol.dialer.location.GeoUtil;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;
import app.diol.dialer.protos.ProtoParsers;
import app.diol.dialer.telecom.TelecomUtil;

/**
 * Dialog that allows the user to select a phone accounts for a given action. Optionally provides
 * the choice to set the phone account as default.
 */
public class SelectPhoneAccountDialogFragment extends DialogFragment {

    @VisibleForTesting
    public static final String ARG_OPTIONS = "options";

    private static final String ARG_IS_DEFAULT_CHECKED = "is_default_checked";

    private SelectPhoneAccountDialogOptions options =
            SelectPhoneAccountDialogOptions.getDefaultInstance();
    private SelectPhoneAccountListener listener;

    private boolean isDefaultChecked;
    private boolean isSelected;

    /**
     * Create new fragment instance.
     */
    public static SelectPhoneAccountDialogFragment newInstance(
            SelectPhoneAccountDialogOptions options, SelectPhoneAccountListener listener) {
        SelectPhoneAccountDialogFragment fragment = new SelectPhoneAccountDialogFragment();
        fragment.setListener(listener);
        Bundle arguments = new Bundle();
        ProtoParsers.put(arguments, ARG_OPTIONS, options);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @VisibleForTesting
    public SelectPhoneAccountListener getListener() {
        return listener;
    }

    public void setListener(SelectPhoneAccountListener listener) {
        this.listener = listener;
    }

    @VisibleForTesting
    public boolean canSetDefault() {
        return options.getCanSetDefault();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_IS_DEFAULT_CHECKED, isDefaultChecked);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        options =
                ProtoParsers.getTrusted(
                        getArguments(), ARG_OPTIONS, SelectPhoneAccountDialogOptions.getDefaultInstance());
        if (savedInstanceState != null) {
            isDefaultChecked = savedInstanceState.getBoolean(ARG_IS_DEFAULT_CHECKED);
        }
        isSelected = false;

        final DialogInterface.OnClickListener selectionListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSelected = true;
                        PhoneAccountHandle selectedAccountHandle =
                                SelectPhoneAccountDialogOptionsUtil.getPhoneAccountHandle(
                                        options.getEntriesList().get(which));
                        Bundle result = new Bundle();
                        result.putParcelable(
                                SelectPhoneAccountListener.EXTRA_SELECTED_ACCOUNT_HANDLE, selectedAccountHandle);
                        result.putBoolean(SelectPhoneAccountListener.EXTRA_SET_DEFAULT, isDefaultChecked);
                        result.putString(SelectPhoneAccountListener.EXTRA_CALL_ID, getCallId());
                        if (listener != null) {
                            listener.onReceiveResult(SelectPhoneAccountListener.RESULT_SELECTED, result);
                        }
                    }
                };

        final CompoundButton.OnCheckedChangeListener checkListener =
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton check, boolean isChecked) {
                        isDefaultChecked = isChecked;
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ListAdapter selectAccountListAdapter =
                new SelectAccountListAdapter(
                        builder.getContext(), R.layout.select_account_list_item, options);

        AlertDialog dialog =
                builder
                        .setTitle(
                                options.hasTitle() ? options.getTitle() : R.string.select_account_dialog_title)
                        .setAdapter(selectAccountListAdapter, selectionListener)
                        .create();

        if (options.getCanSetDefault()) {
            // Generate custom checkbox view, lint suppressed since no appropriate parent (is dialog)
            @SuppressLint("InflateParams")
            LinearLayout checkboxLayout =
                    (LinearLayout)
                            LayoutInflater.from(builder.getContext())
                                    .inflate(R.layout.default_account_checkbox, null);

            CheckBox checkBox = checkboxLayout.findViewById(R.id.default_account_checkbox_view);
            checkBox.setOnCheckedChangeListener(checkListener);
            checkBox.setChecked(isDefaultChecked);

            TextView textView = checkboxLayout.findViewById(R.id.default_account_checkbox_text);
            int setDefaultResId =
                    options.hasSetDefaultLabel()
                            ? options.getSetDefaultLabel()
                            : R.string.set_default_account;
            textView.setText(setDefaultResId);
            textView.setOnClickListener((view) -> checkBox.performClick());
            checkboxLayout.setOnClickListener((view) -> checkBox.performClick());
            checkboxLayout.setContentDescription(getString(setDefaultResId));
            dialog.getListView().addFooterView(checkboxLayout);
        }

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (!isSelected && listener != null) {
            Bundle result = new Bundle();
            result.putString(SelectPhoneAccountListener.EXTRA_CALL_ID, getCallId());
            listener.onReceiveResult(SelectPhoneAccountListener.RESULT_DISMISSED, result);
        }
        super.onCancel(dialog);
    }

    @Nullable
    private String getCallId() {
        return options.getCallId();
    }

    public static class SelectPhoneAccountListener extends ResultReceiver {

        static final int RESULT_SELECTED = 1;
        static final int RESULT_DISMISSED = 2;

        static final String EXTRA_SELECTED_ACCOUNT_HANDLE = "extra_selected_account_handle";
        static final String EXTRA_SET_DEFAULT = "extra_set_default";
        static final String EXTRA_CALL_ID = "extra_call_id";

        protected SelectPhoneAccountListener() {
            super(new Handler());
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == RESULT_SELECTED) {
                onPhoneAccountSelected(
                        resultData.getParcelable(EXTRA_SELECTED_ACCOUNT_HANDLE),
                        resultData.getBoolean(EXTRA_SET_DEFAULT),
                        resultData.getString(EXTRA_CALL_ID));
            } else if (resultCode == RESULT_DISMISSED) {
                onDialogDismissed(resultData.getString(EXTRA_CALL_ID));
            }
        }

        public void onPhoneAccountSelected(
                PhoneAccountHandle selectedAccountHandle, boolean setDefault, @Nullable String callId) {
        }

        public void onDialogDismissed(@Nullable String callId) {
        }
    }

    static class SelectAccountListAdapter
            extends ArrayAdapter<SelectPhoneAccountDialogOptions.Entry> {

        private final SelectPhoneAccountDialogOptions options;
        private int mResId;

        SelectAccountListAdapter(
                Context context, int resource, SelectPhoneAccountDialogOptions options) {
            super(context, resource, options.getEntriesList());
            this.options = options;
            mResId = resource;
        }

        private static String getCountryIso(
                Context context, @NonNull PhoneAccountHandle phoneAccountHandle) {
            Optional<SubscriptionInfo> info =
                    TelecomUtil.getSubscriptionInfo(context, phoneAccountHandle);
            if (!info.isPresent()) {
                return GeoUtil.getCurrentCountryIso(context);
            }
            return info.get().getCountryIso().toUpperCase();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return options.getEntries(position).getEnabled();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView;
            final ViewHolder holder;

            if (convertView == null) {
                // Cache views for faster scrolling
                rowView = inflater.inflate(mResId, null);
                holder = new ViewHolder();
                holder.labelTextView = (TextView) rowView.findViewById(R.id.label);
                holder.numberTextView = (TextView) rowView.findViewById(R.id.number);
                holder.hintTextView = rowView.findViewById(R.id.hint);
                holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) rowView.getTag();
            }

            SelectPhoneAccountDialogOptions.Entry entry = getItem(position);
            PhoneAccountHandle accountHandle =
                    SelectPhoneAccountDialogOptionsUtil.getPhoneAccountHandle(entry);
            PhoneAccount account =
                    getContext().getSystemService(TelecomManager.class).getPhoneAccount(accountHandle);
            if (account == null) {
                return rowView;
            }
            holder.labelTextView.setText(account.getLabel());
            if (account.getAddress() == null
                    || TextUtils.isEmpty(account.getAddress().getSchemeSpecificPart())) {
                holder.numberTextView.setVisibility(View.GONE);
            } else {
                holder.numberTextView.setVisibility(View.VISIBLE);
                holder.numberTextView.setText(
                        PhoneNumberHelper.formatNumberForDisplay(
                                getContext(),
                                account.getAddress().getSchemeSpecificPart(),
                                getCountryIso(getContext(), accountHandle)));
            }
            holder.imageView.setImageDrawable(
                    PhoneAccountCompat.createIconDrawable(account, getContext()));

            if (TextUtils.isEmpty(entry.getHint())) {
                holder.hintTextView.setVisibility(View.GONE);
            } else {
                holder.hintTextView.setVisibility(View.VISIBLE);
                holder.hintTextView.setText(entry.getHint());
            }
            holder.labelTextView.setEnabled(entry.getEnabled());
            holder.numberTextView.setEnabled(entry.getEnabled());
            holder.hintTextView.setEnabled(entry.getEnabled());
            holder.imageView.setImageAlpha(entry.getEnabled() ? 255 : 97 /* 38%*/);
            return rowView;
        }

        static final class ViewHolder {

            TextView labelTextView;
            TextView numberTextView;
            TextView hintTextView;
            ImageView imageView;
        }
    }
}
