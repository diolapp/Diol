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

package app.diol.dialer.blocking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;

import java.util.Objects;

import app.diol.R;
import app.diol.dialer.blocking.BlockedNumbersMigrator.Listener;

/**
 * Dialog fragment shown to users when they need to migrate to use {@link
 * android.provider.BlockedNumberContract} for blocking.
 */
@Deprecated
public class MigrateBlockedNumbersDialogFragment extends DialogFragment {

    private BlockedNumbersMigrator blockedNumbersMigrator;
    private BlockedNumbersMigrator.Listener migrationListener;

    /**
     * Creates a new MigrateBlockedNumbersDialogFragment.
     *
     * @param blockedNumbersMigrator The {@link BlockedNumbersMigrator} which will be used to migrate
     *                               the numbers.
     * @param migrationListener      The {@link BlockedNumbersMigrator.Listener} to call when the migration
     *                               is complete.
     * @return The new MigrateBlockedNumbersDialogFragment.
     * @throws NullPointerException if blockedNumbersMigrator or migrationListener are {@code null}.
     */
    public static DialogFragment newInstance(
            BlockedNumbersMigrator blockedNumbersMigrator,
            BlockedNumbersMigrator.Listener migrationListener) {
        MigrateBlockedNumbersDialogFragment fragment = new MigrateBlockedNumbersDialogFragment();
        fragment.blockedNumbersMigrator = Objects.requireNonNull(blockedNumbersMigrator);
        fragment.migrationListener = Objects.requireNonNull(migrationListener);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog dialog =
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.migrate_blocked_numbers_dialog_title)
                        .setMessage(R.string.migrate_blocked_numbers_dialog_message)
                        .setPositiveButton(R.string.migrate_blocked_numbers_dialog_allow_button, null)
                        .setNegativeButton(R.string.migrate_blocked_numbers_dialog_cancel_button, null)
                        .create();
        // The Dialog's buttons aren't available until show is called, so an OnShowListener
        // is used to set the positive button callback.
        dialog.setOnShowListener(
                new OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final AlertDialog alertDialog = (AlertDialog) dialog;
                        alertDialog
                                .getButton(AlertDialog.BUTTON_POSITIVE)
                                .setOnClickListener(newPositiveButtonOnClickListener(alertDialog));
                    }
                });
        return dialog;
    }

    /*
     * Creates a new View.OnClickListener to be used as the positive button in this dialog. The
     * OnClickListener will grey out the dialog's positive and negative buttons while the migration
     * is underway, and close the dialog once the migrate is complete.
     */
    private View.OnClickListener newPositiveButtonOnClickListener(final AlertDialog alertDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                blockedNumbersMigrator.migrate(
                        new Listener() {
                            @Override
                            public void onComplete() {
                                alertDialog.dismiss();
                                migrationListener.onComplete();
                            }
                        });
            }
        };
    }

    @Override
    public void onPause() {
        // The dialog is dismissed and state is cleaned up onPause, i.e. rotation.
        dismiss();
        blockedNumbersMigrator = null;
        migrationListener = null;
        super.onPause();
    }
}
