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

import android.content.Context;
import android.telecom.PhoneAccountHandle;

import app.diol.voicemail.impl.OmtpConstants;
import app.diol.voicemail.impl.sms.OmtpMessageSender;
import app.diol.voicemail.impl.sms.OmtpStandardMessageSender;

public class OmtpProtocol extends VisualVoicemailProtocol {

    @Override
    public OmtpMessageSender createMessageSender(Context context, PhoneAccountHandle phoneAccountHandle,
                                                 short applicationPort, String destinationNumber) {
        return new OmtpStandardMessageSender(context, phoneAccountHandle, applicationPort, destinationNumber,
                OmtpConstants.getClientType(), OmtpConstants.PROTOCOL_VERSION1_1, null /* clientPrefix */);
    }
}
