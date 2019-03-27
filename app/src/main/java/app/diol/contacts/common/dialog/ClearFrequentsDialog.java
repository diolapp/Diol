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

package app.diol.contacts.common.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;

import app.diol.R;
import app.diol.dialer.util.PermissionsUtil;

/**
 * Dialog that clears the frequently contacted list after confirming with the user.
 */
public class ClearFrequentsDialog extends DialogFragment {

    /**
     * Preferred way to show this dialog
     */
    public static void show(FragmentManager fragmentManager) {
        ClearFrequentsDialog dialog = new ClearFrequentsDialog();
        dialog.show(fragmentManager, "clearFrequents");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity().getApplicationContext();
        final ContentResolver resolver = getActivity().getContentResolver();
        final OnClickListener okListener =
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!PermissionsUtil.hasContactsReadPermissions(context)) {
                            return;
                        }

                        final ProgressDialog progressDialog =
                                ProgressDialog.show(
                                        getContext(),
                                        getString(R.string.clearFrequentsProgress_title),
                                        null,
                                        true,
                                        true);

                        final AsyncTask<Void, Void, Void> task =
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        resolver.delete(
                                                ContactsContract.DataUsageFeedback.DELETE_USAGE_URI, null, null);
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void result) {
                                        progressDialog.dismiss();
                                    }
                                };
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                };
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.clearFrequentsConfirmation_title)
                .setMessage(R.string.clearFrequentsConfirmation)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, okListener)
                .setCancelable(true)
                .create();
    }
}
