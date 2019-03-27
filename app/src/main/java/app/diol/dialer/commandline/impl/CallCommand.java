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

package app.diol.dialer.commandline.impl;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javax.inject.Inject;

import app.diol.dialer.buildtype.BuildType;
import app.diol.dialer.buildtype.BuildType.Type;
import app.diol.dialer.callintent.CallInitiationType;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.commandline.Arguments;
import app.diol.dialer.commandline.Command;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.precall.PreCall;

/**
 * Make calls. Requires bugfood build.
 */
public class CallCommand implements Command {

    private final Context appContext;

    @Inject
    CallCommand(@ApplicationContext Context appContext) {
        this.appContext = appContext;
    }

    @NonNull
    @Override
    public String getShortDescription() {
        return "make a call";
    }

    @NonNull
    @Override
    public String getUsage() {
        return "call [flags --] number\n"
                + "\nuse 'voicemail' to call voicemail"
                + "\n\nflags:"
                + "\n--direct send intent to telecom instead of pre call";
    }

    @Override
    @SuppressWarnings("missingPermission")
    public ListenableFuture<String> run(Arguments args) throws IllegalCommandLineArgumentException {
        if (BuildType.get() != Type.BUGFOOD) {
            throw new SecurityException("Bugfood only command");
        }
        String number = args.expectPositional(0, "number");
        TelecomManager telecomManager = appContext.getSystemService(TelecomManager.class);
        PhoneAccountHandle phoneAccountHandle =
                telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL);
        CallIntentBuilder callIntentBuilder;
        if ("voicemail".equals(number)) {
            callIntentBuilder =
                    CallIntentBuilder.forVoicemail(phoneAccountHandle, CallInitiationType.Type.DIALPAD);
        } else {
            callIntentBuilder = new CallIntentBuilder(number, CallInitiationType.Type.DIALPAD);
        }
        if (args.getBoolean("direct", false)) {
            Intent intent = callIntentBuilder.build();
            appContext
                    .getSystemService(TelecomManager.class)
                    .placeCall(intent.getData(), intent.getExtras());
        } else {
            Intent intent = PreCall.getIntent(appContext, callIntentBuilder);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(intent);
        }
        return Futures.immediateFuture("Calling " + number);
    }
}
