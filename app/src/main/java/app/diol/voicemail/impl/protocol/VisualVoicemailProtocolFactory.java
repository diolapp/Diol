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

package app.diol.voicemail.impl.protocol;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import app.diol.voicemail.VisualVoicemailTypeExtensions;
import app.diol.voicemail.impl.VvmLog;

public class VisualVoicemailProtocolFactory {

    private static final String TAG = "VvmProtocolFactory";

    @Nullable
    public static VisualVoicemailProtocol create(Resources resources, String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case TelephonyManager.VVM_TYPE_OMTP:
                return new OmtpProtocol();
            case TelephonyManager.VVM_TYPE_CVVM:
                return new CvvmProtocol();
            case VisualVoicemailTypeExtensions.VVM_TYPE_VVM3:
                return new Vvm3Protocol();
            default:
                VvmLog.e(TAG, "Unexpected visual voicemail type: " + type);
        }
        return null;
    }
}
