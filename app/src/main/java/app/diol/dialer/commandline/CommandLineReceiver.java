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

package app.diol.dialer.commandline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import app.diol.dialer.buildtype.BuildType;
import app.diol.dialer.buildtype.BuildType.Type;
import app.diol.dialer.commandline.Command.IllegalCommandLineArgumentException;
import app.diol.dialer.common.LogUtil;

/**
 * Receives broadcasts to the component from adb shell. Must be on bugfood or have debug logging
 * enabled.
 */
public class CommandLineReceiver extends BroadcastReceiver {

    public static final String COMMAND = "command";
    public static final String ARGS = "args";
    public static final String TAG = "tag";

    @Override
    public void onReceive(Context context, Intent intent) {
        String outputTag = intent.getStringExtra(TAG);
        if (outputTag == null) {
            LogUtil.e("CommandLineReceiver", "missing tag");
            return;
        }
        if (!LogUtil.isDebugEnabled() && BuildType.get() != Type.BUGFOOD) {
            LogUtil.i(outputTag, "DISABLED");
            return;
        }
        Command command =
                CommandLineComponent.get(context)
                        .commandSupplier()
                        .get()
                        .get(intent.getStringExtra(COMMAND));
        try {
            if (command == null) {
                LogUtil.i(outputTag, "unknown command " + intent.getStringExtra(COMMAND));
                return;
            }

            Arguments args = Arguments.parse(intent.getStringArrayExtra(ARGS));

            if (args.getBoolean("help", false)) {
                LogUtil.i(outputTag, "usage:\n" + command.getUsage());
                return;
            }
            Futures.addCallback(
                    command.run(args),
                    new FutureCallback<String>() {
                        @Override
                        public void onSuccess(String response) {
                            if (TextUtils.isEmpty(response)) {
                                LogUtil.i(outputTag, "EMPTY");
                            } else {
                                LogUtil.i(outputTag, response);
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            if (throwable instanceof IllegalCommandLineArgumentException) {
                                LogUtil.e(outputTag, throwable.getMessage() + "\n\nusage:\n" + command.getUsage());
                            }
                            LogUtil.e(outputTag, "error running command future", throwable);
                        }
                    },
                    MoreExecutors.directExecutor());
        } catch (IllegalCommandLineArgumentException e) {
            LogUtil.e(outputTag, e.getMessage() + "\n\nusage:\n" + command.getUsage());
        } catch (Throwable throwable) {
            LogUtil.e(outputTag, "error running command", throwable);
        }
    }
}
