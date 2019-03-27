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
import android.support.annotation.NonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javax.inject.Inject;

import app.diol.dialer.activecalls.ActiveCallsComponent;
import app.diol.dialer.commandline.Arguments;
import app.diol.dialer.commandline.Command;
import app.diol.dialer.inject.ApplicationContext;

/**
 * Manipulates {@link app.diol.dialer.activecalls.ActiveCalls}
 */
public class ActiveCallsCommand implements Command {

    private final Context appContext;

    @Inject
    ActiveCallsCommand(@ApplicationContext Context appContext) {
        this.appContext = appContext;
    }

    @NonNull
    @Override
    public String getShortDescription() {
        return "manipulate active calls";
    }

    @NonNull
    @Override
    public String getUsage() {
        return "activecalls list";
    }

    @Override
    public ListenableFuture<String> run(Arguments args) throws IllegalCommandLineArgumentException {
        if (args.getPositionals().isEmpty()) {
            return Futures.immediateFuture(getUsage());
        }

        String command = args.getPositionals().get(0);

        switch (command) {
            case "list":
                return Futures.immediateFuture(
                        ActiveCallsComponent.get(appContext).activeCalls().getActiveCalls().toString());
            default:
                throw new IllegalCommandLineArgumentException("unknown command " + command);
        }
    }
}
