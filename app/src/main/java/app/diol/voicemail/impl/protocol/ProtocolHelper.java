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

import android.text.TextUtils;

import app.diol.voicemail.impl.OmtpVvmCarrierConfigHelper;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.sms.OmtpMessageSender;

public class ProtocolHelper {

    private static final String TAG = "ProtocolHelper";

    public static OmtpMessageSender getMessageSender(VisualVoicemailProtocol protocol,
                                                     OmtpVvmCarrierConfigHelper config) {

        int applicationPort = config.getApplicationPort();
        String destinationNumber = config.getDestinationNumber();
        if (TextUtils.isEmpty(destinationNumber)) {
            VvmLog.w(TAG, "No destination number for this carrier.");
            return null;
        }

        return protocol.createMessageSender(config.getContext(), config.getPhoneAccountHandle(), (short) applicationPort,
                destinationNumber);
    }
}
