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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import app.diol.R;
import app.diol.incallui.call.TelecomAdapter;

/**
 * Pop up an alert dialog with OK and Cancel buttons to allow user to Accept or Reject the WAIT
 * inserted as part of the Dial string.
 */
public class PostCharDialogFragment extends DialogFragment {

    private static final String STATE_CALL_ID = "CALL_ID";
    private static final String STATE_POST_CHARS = "POST_CHARS";

    private String callId;
    private String postDialStr;

    public PostCharDialogFragment() {
    }

    public PostCharDialogFragment(String callId, String postDialStr) {
        this.callId = callId;
        this.postDialStr = postDialStr;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if (postDialStr == null && savedInstanceState != null) {
            callId = savedInstanceState.getString(STATE_CALL_ID);
            postDialStr = savedInstanceState.getString(STATE_POST_CHARS);
        }

        final StringBuilder buf = new StringBuilder();
        buf.append(getResources().getText(R.string.wait_prompt_str));
        buf.append(postDialStr);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(buf.toString());

        builder.setPositiveButton(
                R.string.pause_prompt_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        TelecomAdapter.getInstance().postDialContinue(callId, true);
                    }
                });
        builder.setNegativeButton(
                R.string.pause_prompt_no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        final AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        TelecomAdapter.getInstance().postDialContinue(callId, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_CALL_ID, callId);
        outState.putString(STATE_POST_CHARS, postDialStr);
    }
}
