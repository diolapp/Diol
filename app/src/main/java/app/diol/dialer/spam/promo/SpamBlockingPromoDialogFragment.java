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

package app.diol.dialer.spam.promo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import app.diol.R;

/**
 * Dialog for spam blocking on-boarding promotion.
 */
public class SpamBlockingPromoDialogFragment extends DialogFragment {

    public static final String SPAM_BLOCKING_PROMO_DIALOG_TAG = "SpamBlockingPromoDialog";

    /**
     * Called when dialog positive button is pressed.
     */
    protected OnEnableListener positiveListener;

    /**
     * Called when the dialog is dismissed.
     */
    @Nullable
    protected DialogInterface.OnDismissListener dismissListener;

    public static DialogFragment newInstance(
            OnEnableListener positiveListener,
            @Nullable DialogInterface.OnDismissListener dismissListener) {
        SpamBlockingPromoDialogFragment fragment = new SpamBlockingPromoDialogFragment();
        fragment.positiveListener = positiveListener;
        fragment.dismissListener = dismissListener;
        return fragment;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dismissListener != null) {
            dismissListener.onDismiss(dialog);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onPause() {
        // The dialog is dismissed onPause, i.e. rotation.
        dismiss();
        dismissListener = null;
        positiveListener = null;
        super.onPause();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        // Return the newly created dialog
        return new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle(R.string.spam_blocking_promo_title)
                .setMessage(R.string.spam_blocking_promo_text)
                .setNegativeButton(
                        R.string.spam_blocking_promo_action_dismiss, (dialog, which) -> dismiss())
                .setPositiveButton(
                        R.string.spam_blocking_promo_action_filter_spam,
                        (dialog, which) -> {
                            dismiss();
                            positiveListener.onClick();
                        })
                .create();
    }

    /**
     * Positive listener for spam blocking promotion dialog.
     */
    public interface OnEnableListener {
        /**
         * Called when user clicks on positive button in the spam blocking promo dialog.
         */
        void onClick();
    }
}
