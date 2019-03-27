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

package app.diol.incallui.answer.impl;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import app.diol.R;
import app.diol.dialer.common.FragmentUtils;
import app.diol.incallui.incalluilock.InCallUiLock;

/**
 * Shows the dialog for users to enter a custom message when rejecting a call with an SMS message.
 */
public class CreateCustomSmsDialogFragment extends AppCompatDialogFragment {

    private static final String ARG_ENTERED_TEXT = "enteredText";

    private EditText editText;
    private InCallUiLock inCallUiLock;

    public static CreateCustomSmsDialogFragment newInstance() {
        return new CreateCustomSmsDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(builder.getContext(), R.layout.fragment_custom_sms_dialog, null);
        editText = (EditText) view.findViewById(R.id.custom_sms_input);
        if (savedInstanceState != null) {
            editText.setText(savedInstanceState.getCharSequence(ARG_ENTERED_TEXT));
        }

        inCallUiLock =
                FragmentUtils.getParentUnsafe(
                        CreateCustomSmsDialogFragment.this, CreateCustomSmsHolder.class)
                        .acquireInCallUiLock("CreateCustomSmsDialogFragment");
        builder
                .setCancelable(true)
                .setView(view)
                .setPositiveButton(
                        R.string.call_incoming_custom_message_send,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FragmentUtils.getParentUnsafe(
                                        CreateCustomSmsDialogFragment.this, CreateCustomSmsHolder.class)
                                        .customSmsCreated(editText.getText().toString().trim());
                                dismiss();
                            }
                        })
                .setNegativeButton(
                        R.string.call_incoming_custom_message_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dismiss();
                            }
                        })
                .setOnCancelListener(
                        new OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                dismiss();
                            }
                        })
                .setTitle(R.string.call_incoming_respond_via_sms_custom_message);
        final AlertDialog customMessagePopup = builder.create();
        customMessagePopup.setOnShowListener(
                new OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        ((AlertDialog) dialogInterface)
                                .getButton(AlertDialog.BUTTON_POSITIVE)
                                .setEnabled(false);
                    }
                });

        editText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        Button sendButton = customMessagePopup.getButton(DialogInterface.BUTTON_POSITIVE);
                        sendButton.setEnabled(editable != null && editable.toString().trim().length() != 0);
                    }
                });
        customMessagePopup.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        customMessagePopup.getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        return customMessagePopup;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(ARG_ENTERED_TEXT, editText.getText());
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        inCallUiLock.release();
        FragmentUtils.getParentUnsafe(this, CreateCustomSmsHolder.class).customSmsDismissed();
    }

    /**
     * Call back for {@link CreateCustomSmsDialogFragment}
     */
    public interface CreateCustomSmsHolder {

        InCallUiLock acquireInCallUiLock(String tag);

        void customSmsCreated(@NonNull CharSequence text);

        void customSmsDismissed();
    }
}
