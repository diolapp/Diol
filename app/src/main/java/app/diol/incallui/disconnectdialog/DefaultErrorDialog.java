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

package app.diol.incallui.disconnectdialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.telecom.DisconnectCause;
import android.text.TextUtils;
import android.util.Pair;

import app.diol.incallui.call.DialerCall;

/**
 * Default error dialog shown to user after disconnect.
 */
public class DefaultErrorDialog implements DisconnectDialog {

    @Override
    public boolean shouldShow(DisconnectCause disconnectCause) {
        return !TextUtils.isEmpty(disconnectCause.getDescription())
                && (disconnectCause.getCode() == DisconnectCause.ERROR
                || disconnectCause.getCode() == DisconnectCause.RESTRICTED);
    }

    @Override
    public Pair<Dialog, CharSequence> createDialog(@NonNull Context context, DialerCall call) {
        DisconnectCause disconnectCause = call.getDisconnectCause();
        CharSequence message = disconnectCause.getDescription();

        Dialog dialog =
                new AlertDialog.Builder(context)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.cancel, null)
                        .create();
        return new Pair<>(dialog, message);
    }
}
