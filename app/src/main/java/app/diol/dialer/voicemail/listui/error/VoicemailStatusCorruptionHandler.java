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

package app.diol.dialer.voicemail.listui.error;

import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.VoicemailContract.Status;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.compat.telephony.TelephonyManagerCompat;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;

/**
 * This class will detect the corruption in the voicemail status and log it so we can track how many
 * users are affected.
 */
public class VoicemailStatusCorruptionHandler {

    private static final String CONFIG_VVM_STATUS_FIX_DISABLED = "vvm_status_fix_disabled";

    public static void maybeFixVoicemailStatus(Context context, Cursor statusCursor, Source source) {

        if (ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getBoolean(CONFIG_VVM_STATUS_FIX_DISABLED, false)) {
            return;
        }

        if (VERSION.SDK_INT != VERSION_CODES.N_MR1) {
            // This issue is specific to N MR1, it is fixed in future SDK.
            return;
        }

        if (statusCursor.getCount() == 0) {
            return;
        }

        statusCursor.moveToFirst();
        VoicemailStatus status = new VoicemailStatus(context, statusCursor);
        PhoneAccountHandle phoneAccountHandle =
                new PhoneAccountHandle(
                        ComponentName.unflattenFromString(status.phoneAccountComponentName),
                        status.phoneAccountId);

        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class);

        boolean visualVoicemailEnabled =
                TelephonyManagerCompat.isVisualVoicemailEnabled(telephonyManager, phoneAccountHandle);
        LogUtil.i(
                "VoicemailStatusCorruptionHandler.maybeFixVoicemailStatus",
                "Source="
                        + source
                        + ", CONFIGURATION_STATE="
                        + status.configurationState
                        + ", visualVoicemailEnabled="
                        + visualVoicemailEnabled);

        // If visual voicemail is enabled, the CONFIGURATION_STATE should be either OK, PIN_NOT_SET,
        // or other failure code. CONFIGURATION_STATE_NOT_CONFIGURED means that the client has been
        // shut down improperly (a bug). The client should be reset or the VVM tab will be
        // missing.
        if (Status.CONFIGURATION_STATE_NOT_CONFIGURED == status.configurationState
                && visualVoicemailEnabled) {
            LogUtil.e(
                    "VoicemailStatusCorruptionHandler.maybeFixVoicemailStatus",
                    "VVM3 voicemail status corrupted");

            switch (source) {
                case Activity:
                    Logger.get(context)
                            .logImpression(
                                    DialerImpression.Type
                                            .VOICEMAIL_CONFIGURATION_STATE_CORRUPTION_DETECTED_FROM_ACTIVITY);
                    break;
                case Notification:
                    Logger.get(context)
                            .logImpression(
                                    DialerImpression.Type
                                            .VOICEMAIL_CONFIGURATION_STATE_CORRUPTION_DETECTED_FROM_NOTIFICATION);
                    break;
                default:
                    Assert.fail("this should never happen");
                    break;
            }
            // At this point we could attempt to work around the issue by disabling and re-enabling
            // voicemail. Unfortunately this work around is buggy so we'll do nothing for now.
        }
    }

    /**
     * Where the check is made so logging can be done.
     */
    public enum Source {
        Activity,
        Notification
    }
}
