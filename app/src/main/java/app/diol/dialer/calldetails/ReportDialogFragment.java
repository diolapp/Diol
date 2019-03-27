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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import app.diol.R;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.SuccessListener;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.phonenumbercache.CachedNumberLookupService;
import app.diol.dialer.phonenumbercache.CachedNumberLookupService.CachedContactInfo;
import app.diol.dialer.phonenumbercache.PhoneNumberCache;
import app.diol.dialer.theme.base.ThemeComponent;

/**
 * Dialog for reporting an inaccurate caller id information.
 */
public class ReportDialogFragment extends DialogFragment {

    private static final String KEY_NUMBER = "number";
    private TextView name;
    private TextView numberView;

    private CachedNumberLookupService cachedNumberLookupService;
    private CachedNumberLookupService.CachedContactInfo info;
    private String number;

    public static ReportDialogFragment newInstance(String number) {
        ReportDialogFragment fragment = new ReportDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NUMBER, number);
        fragment.setArguments(bundle);
        return fragment;
    }

    private static void onShow(Context context, AlertDialog dialog) {
        int buttonTextColor = ThemeComponent.get(context).theme().getColorPrimary();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(buttonTextColor);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(buttonTextColor);
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        number = getArguments().getString(KEY_NUMBER);
        cachedNumberLookupService = PhoneNumberCache.get(getContext()).getCachedNumberLookupService();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.caller_id_report_dialog, null, false);
        name = view.findViewById(R.id.name);
        numberView = view.findViewById(R.id.number);

        lookupContactInfo(number);

        AlertDialog reportDialog =
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.report_caller_id_dialog_title)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> positiveClick(dialog))
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .setView(view)
                        .create();

        reportDialog.setOnShowListener(dialog -> onShow(getContext(), reportDialog));
        return reportDialog;
    }

    private void positiveClick(DialogInterface dialog) {
        startReportCallerIdWorker();
        dialog.dismiss();
    }

    private void lookupContactInfo(String number) {
        Worker<String, CachedContactInfo> worker =
                number1 -> cachedNumberLookupService.lookupCachedContactFromNumber(getContext(), number1);
        SuccessListener<CachedContactInfo> successListener = this::setCachedContactInfo;
        DialerExecutorComponent.get(getContext())
                .dialerExecutorFactory()
                .createUiTaskBuilder(getFragmentManager(), "lookup_contact_info", worker)
                .onSuccess(successListener)
                .build()
                .executeParallel(number);
    }

    private void setCachedContactInfo(CachedContactInfo info) {
        this.info = info;
        if (info != null) {
            name.setText(info.getContactInfo().name);
            numberView.setText(info.getContactInfo().number);
        } else {
            numberView.setText(number);
            name.setVisibility(View.GONE);
        }
    }

    private void startReportCallerIdWorker() {
        Worker<Context, Pair<Context, Boolean>> worker = this::reportCallerId;
        SuccessListener<Pair<Context, Boolean>> successListener = this::onReportCallerId;
        DialerExecutorComponent.get(getContext())
                .dialerExecutorFactory()
                .createUiTaskBuilder(getFragmentManager(), "report_caller_id", worker)
                .onSuccess(successListener)
                .build()
                .executeParallel(getActivity());
    }

    private Pair<Context, Boolean> reportCallerId(Context context) {
        if (cachedNumberLookupService.reportAsInvalid(context, info)) {
            info.getContactInfo().isBadData = true;
            cachedNumberLookupService.addContact(context, info);
            LogUtil.d("ReportUploadTask.doInBackground", "Contact reported.");
            return new Pair<>(context, true);
        } else {
            return new Pair<>(context, false);
        }
    }

    private void onReportCallerId(Pair<Context, Boolean> output) {
        Context context = output.first;
        boolean wasReport = output.second;
        if (wasReport) {
            Logger.get(context).logImpression(DialerImpression.Type.CALLER_ID_REPORTED);
            Toast.makeText(context, R.string.report_caller_id_toast, Toast.LENGTH_SHORT).show();
        } else {
            Logger.get(context).logImpression(DialerImpression.Type.CALLER_ID_REPORT_FAILED);
            Toast.makeText(context, R.string.report_caller_id_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            // Prevent dialog from dismissing on rotate.
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
