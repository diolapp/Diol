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

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import app.diol.dialer.commandline.Arguments;
import app.diol.dialer.commandline.Command;
import app.diol.dialer.commandline.CommandLineComponent;
import app.diol.dialer.inject.ApplicationContext;

/**
 * List available commands
 */
public class Help implements Command {

    private final Context context;

    @Inject
    Help(@ApplicationContext Context context) {
        this.context = context;
    }

    private static String runOrThrow(Command command) throws IllegalCommandLineArgumentException {
        try {
            return command.run(Arguments.EMPTY).get();
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public String getShortDescription() {
        return "Print this message";
    }

    @NonNull
    @Override
    public String getUsage() {
        return "help";
    }

    @Override
    public ListenableFuture<String> run(Arguments args) throws IllegalCommandLineArgumentException {
        boolean showHidden = args.getFlags().containsKey("showHidden");

        StringBuilder stringBuilder = new StringBuilder();
        ImmutableMap<String, Command> commands =
                CommandLineComponent.get(context).commandSupplier().get();
        stringBuilder
                .append(runOrThrow(commands.get("version")))
                .append("\n")
                .append("\n")
                .append("usage: <command> [args...]\n")
                .append("\n")
                .append("<command>\n");

        for (Entry<String, Command> entry : commands.entrySet()) {
            String description = entry.getValue().getShortDescription();
            if (!showHidden && description.startsWith("@hide ")) {
                continue;
            }
            stringBuilder.append(String.format(Locale.US, "  %20s  %s\n", entry.getKey(), description));
        }

        return Futures.immediateFuture(stringBuilder.toString());
    }
}
