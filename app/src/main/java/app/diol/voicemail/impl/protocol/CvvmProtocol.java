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
import app.diol.voicemail.impl.sms.OmtpCvvmMessageSender;
import app.diol.voicemail.impl.sms.OmtpMessageSender;

/**
 * A flavor of OMTP protocol with a different mobile originated (MO) format
 *
 * <p>
 * Used by carriers such as T-Mobile
 */
public class CvvmProtocol extends VisualVoicemailProtocol {

    private static String IMAP_CHANGE_TUI_PWD_FORMAT = "CHANGE_TUI_PWD PWD=%1$s OLD_PWD=%2$s";
    private static String IMAP_CHANGE_VM_LANG_FORMAT = "CHANGE_VM_LANG Lang=%1$s";
    private static String IMAP_CLOSE_NUT = "CLOSE_NUT";

    @Override
    public OmtpMessageSender createMessageSender(Context context, PhoneAccountHandle phoneAccountHandle,
                                                 short applicationPort, String destinationNumber) {
        return new OmtpCvvmMessageSender(context, phoneAccountHandle, applicationPort, destinationNumber);
    }

    @Override
    public String getCommand(String command) {
        if (command == OmtpConstants.IMAP_CHANGE_TUI_PWD_FORMAT) {
            return IMAP_CHANGE_TUI_PWD_FORMAT;
        }
        if (command == OmtpConstants.IMAP_CLOSE_NUT) {
            return IMAP_CLOSE_NUT;
        }
        if (command == OmtpConstants.IMAP_CHANGE_VM_LANG_FORMAT) {
            return IMAP_CHANGE_VM_LANG_FORMAT;
        }
        return super.getCommand(command);
    }
}
