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

package app.diol.voicemail.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import app.diol.voicemail.impl.settings.VisualVoicemailSettingsUtil;

/**
 * When a new package is installed, check if it matches any of the vvm carrier
 * apps of the currently enabled dialer VVM sources. The dialer VVM client will
 * be disabled upon carrier VVM app installation, unless it was explicitly
 * enabled by the user.
 *
 * <p>
 * The ACTION_PACKAGE_ADDED broadcast can no longer be received. (see
 * https://developer.android.com/preview/features/background.html#broadcasts)
 * New apps are scanned when a VVM SMS is received instead, as it can be a
 * result of the carrier VVM app trying to run activation.
 */
@TargetApi(VERSION_CODES.O)
public final class VvmPackageInstallHandler {

    /**
     * Iterates through all phone account and disable VVM on a account if
     * {@code packageName} is listed as a carrier VVM package.
     */
    public static void handlePackageInstalled(Context context) {
        // This get called every time an app is installed and will be noisy. Don't log
        // until the app
        // is identified as a carrier VVM app.
        for (PhoneAccountHandle phoneAccount : context.getSystemService(TelecomManager.class)
                .getCallCapablePhoneAccounts()) {
            OmtpVvmCarrierConfigHelper carrierConfigHelper = new OmtpVvmCarrierConfigHelper(context, phoneAccount);
            if (!carrierConfigHelper.isValid()) {
                continue;
            }
            if (!carrierConfigHelper.isCarrierAppInstalled()) {
                continue;
            }

            // Force deactivate the client.
            VvmLog.i("VvmPackageInstallHandler.handlePackageInstalled",
                    "Carrier VVM package installed, disabling system VVM client");
            VisualVoicemailSettingsUtil.setEnabled(context, phoneAccount, false);
        }
    }
}
