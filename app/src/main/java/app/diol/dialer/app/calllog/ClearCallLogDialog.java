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

package app.diol.dialer.app.calllog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.concurrent.DialerExecutor;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.enrichedcall.EnrichedCallComponent;
import app.diol.dialer.phonenumbercache.CachedNumberLookupService;
import app.diol.dialer.phonenumbercache.PhoneNumberCache;

/**
 * Dialog that clears the call log after confirming with the user
 */
public class ClearCallLogDialog extends DialogFragment {

    private DialerExecutor<Void> clearCallLogTask;
    private ProgressDialog progressDialog;

    /**
     * Preferred way to show this dialog
     */
    public static void show(FragmentManager fragmentManager) {
        ClearCallLogDialog dialog = new ClearCallLogDialog();
        dialog.show(fragmentManager, "deleteCallLog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clearCallLogTask =
                DialerExecutorComponent.get(getContext())
                        .dialerExecutorFactory()
                        .createUiTaskBuilder(
                                getFragmentManager(),
                                "clearCallLogTask",
                                new ClearCallLogWorker(getActivity().getApplicationContext()))
                        .onSuccess(this::onSuccess)
                        .build();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        OnClickListener okListener =
                (dialog, which) -> {
                    progressDialog =
                            ProgressDialog.show(
                                    getActivity(), getString(R.string.clearCallLogProgress_title), "", true, false);
                    progressDialog.setOwnerActivity(getActivity());
                    CallLogNotificationsService.cancelAllMissedCalls(getContext());

                    // TODO: Once we have the API, we should configure this ProgressDialog
                    // to only show up after a certain time (e.g. 150ms)
                    progressDialog.show();

                    clearCallLogTask.executeSerial(null);
                };
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.clearCallLogConfirmation_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.clearCallLogConfirmation)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, okListener)
                .setCancelable(true)
                .create();
    }

    private void onSuccess(Void unused) {
        Assert.isNotNull(progressDialog);
        Activity activity = progressDialog.getOwnerActivity();

        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }

        maybeShowEnrichedCallSnackbar(activity);

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void maybeShowEnrichedCallSnackbar(Activity activity) {
        if (EnrichedCallComponent.get(activity).getEnrichedCallManager().hasStoredData()) {
            Snackbar.make(
                    activity.findViewById(R.id.calllog_frame),
                    activity.getString(R.string.multiple_ec_data_deleted),
                    5_000)
                    .show();
        }
    }

    private static class ClearCallLogWorker implements Worker<Void, Void> {
        private final Context appContext;

        private ClearCallLogWorker(Context appContext) {
            this.appContext = appContext;
        }

        @Nullable
        @Override
        public Void doInBackground(@Nullable Void unused) throws Throwable {
            appContext.getContentResolver().delete(Calls.CONTENT_URI, null, null);
            CachedNumberLookupService cachedNumberLookupService =
                    PhoneNumberCache.get(appContext).getCachedNumberLookupService();
            if (cachedNumberLookupService != null) {
                cachedNumberLookupService.clearAllCacheEntries(appContext);
            }
            return null;
        }
    }
}
