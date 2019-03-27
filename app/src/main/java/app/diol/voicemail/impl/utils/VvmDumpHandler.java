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

package app.diol.voicemail.impl.utils;

import android.content.Context;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import app.diol.voicemail.impl.OmtpVvmCarrierConfigHelper;
import app.diol.voicemail.impl.VvmLog;

public class VvmDumpHandler {

    public static void dump(Context context, FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter indentedWriter = new IndentingPrintWriter(writer, "  ");
        indentedWriter.println("******* OmtpVvm *******");
        indentedWriter.println("======= Configs =======");
        indentedWriter.increaseIndent();
        for (PhoneAccountHandle handle : context.getSystemService(TelecomManager.class).getCallCapablePhoneAccounts()) {
            OmtpVvmCarrierConfigHelper config = new OmtpVvmCarrierConfigHelper(context, handle);
            indentedWriter.println(config.toString());
        }
        indentedWriter.decreaseIndent();
        indentedWriter.println("======== Logs =========");
        VvmLog.dump(fd, indentedWriter, args);
    }
}
