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
package app.diol.voicemail.impl.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

public class Vvm3MessageSender extends OmtpMessageSender {

    /**
     * Creates a new instance of Vvm3MessageSender.
     *
     * @param applicationPort If set to a value > 0 then a binary sms is sent to
     *                        this port number. Otherwise, a standard text SMS is
     *                        sent.
     */
    public Vvm3MessageSender(Context context, PhoneAccountHandle phoneAccountHandle, short applicationPort,
                             String destinationNumber) {
        super(context, phoneAccountHandle, applicationPort, destinationNumber);
    }

    @Override
    public void requestVvmActivation(@Nullable PendingIntent sentIntent) {
        // Activation not supported for VVM3, send a status request instead.
        requestVvmStatus(sentIntent);
    }

    @Override
    public void requestVvmDeactivation(@Nullable PendingIntent sentIntent) {
        // Deactivation not supported for VVM3, do nothing
    }

    @Override
    public void requestVvmStatus(@Nullable PendingIntent sentIntent) {
        // Status message:
        // STATUS
        StringBuilder sb = new StringBuilder().append("STATUS");
        sendSms(sb.toString(), sentIntent);
    }
}
