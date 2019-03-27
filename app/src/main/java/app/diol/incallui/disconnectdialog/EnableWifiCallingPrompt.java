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
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.telecom.DisconnectCause;
import android.util.Pair;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.incallui.call.DialerCall;

/**
 * Prompts the user to enable Wi-Fi calling.
 */
public class EnableWifiCallingPrompt implements DisconnectDialog {
    // This is a hidden constant in android.telecom.DisconnectCause. Telecom sets this as a disconnect
    // reason if it wants us to prompt the user to enable Wi-Fi calling. In Android-O we might
    // consider using a more explicit way to signal this.
    private static final String REASON_WIFI_ON_BUT_WFC_OFF = "REASON_WIFI_ON_BUT_WFC_OFF";
    private static final String ACTION_WIFI_CALLING_SETTINGS =
            "android.settings.WIFI_CALLING_SETTINGS";
    private static final String ANDROID_SETTINGS_PACKAGE = "app.diol.settings";

    private static void openWifiCallingSettings(@NonNull Context context) {
        LogUtil.i("EnableWifiCallingPrompt.openWifiCallingSettings", "opening settings");
        context.startActivity(
                new Intent(ACTION_WIFI_CALLING_SETTINGS).setPackage(ANDROID_SETTINGS_PACKAGE));
    }

    @Override
    public boolean shouldShow(DisconnectCause disconnectCause) {
        String reason = disconnectCause.getReason();
        if (reason != null && reason.startsWith(REASON_WIFI_ON_BUT_WFC_OFF)) {
            LogUtil.i(
                    "EnableWifiCallingPrompt.shouldShowPrompt",
                    "showing prompt for disconnect cause: %s",
                    reason);
            return true;
        }
        return false;
    }

    @Override
    public Pair<Dialog, CharSequence> createDialog(final @NonNull Context context, DialerCall call) {
        Assert.isNotNull(context);
        DisconnectCause cause = call.getDisconnectCause();
        CharSequence message = cause.getDescription();
        Dialog dialog =
                new AlertDialog.Builder(context)
                        .setMessage(message)
                        .setPositiveButton(
                                R.string.incall_enable_wifi_calling_button,
                                (OnClickListener) (dialog1, which) -> openWifiCallingSettings(context))
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
        return new Pair<>(dialog, message);
    }
}
