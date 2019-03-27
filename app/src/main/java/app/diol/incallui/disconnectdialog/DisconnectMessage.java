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
import android.telecom.DisconnectCause;
import android.util.Pair;

import java.util.Locale;

import app.diol.incallui.call.DialerCall;

/**
 * Wrapper class around @Code{android.telecom.DisconnectCause} to provide more information to user.
 */
public class DisconnectMessage {

    // Disconnect dialog catalog. Default error dialog MUST be last one.
    private static final DisconnectDialog[] DISCONNECT_DIALOGS =
            new DisconnectDialog[]{
                    new EnableWifiCallingPrompt(), new VideoCallNotAvailablePrompt(), new DefaultErrorDialog()
            };

    public final Dialog dialog;
    public final CharSequence toastMessage;
    private final DisconnectCause cause;

    public DisconnectMessage(Context context, DialerCall call) {
        cause = call.getDisconnectCause();

        for (DisconnectDialog disconnectDialog : DISCONNECT_DIALOGS) {
            if (disconnectDialog.shouldShow(cause)) {
                Pair<Dialog, CharSequence> pair = disconnectDialog.createDialog(context, call);
                dialog = pair.first;
                toastMessage = pair.second;
                return;
            }
        }
        dialog = null;
        toastMessage = null;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.ENGLISH,
                "DisconnectMessage {code: %d, description: %s, reason: %s, message: %s}",
                cause.getCode(),
                cause.getDescription(),
                cause.getReason(),
                toastMessage);
    }
}
