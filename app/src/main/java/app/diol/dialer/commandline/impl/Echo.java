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

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javax.inject.Inject;

import app.diol.dialer.commandline.Arguments;
import app.diol.dialer.commandline.Command;

/**
 * Print arguments.
 */
public class Echo implements Command {

    @VisibleForTesting
    @Inject
    public Echo() {
    }

    @NonNull
    @Override
    public String getShortDescription() {
        return "@hide Print all arguments.";
    }

    @NonNull
    @Override
    public String getUsage() {
        return "echo [arguments...]";
    }

    @Override
    public ListenableFuture<String> run(Arguments args) throws IllegalCommandLineArgumentException {
        return Futures.immediateFuture(TextUtils.join(" ", args.getPositionals()));
    }
}
