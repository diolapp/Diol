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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.NonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;

import javax.inject.Inject;

import app.diol.dialer.commandline.Arguments;
import app.diol.dialer.commandline.Command;
import app.diol.dialer.inject.ApplicationContext;

/**
 * Print the version name and code.
 */
public class Version implements Command {

    private final Context appContext;

    @Inject
    Version(@ApplicationContext Context context) {
        this.appContext = context;
    }

    @NonNull
    @Override
    public String getShortDescription() {
        return "Print dialer version";
    }

    @NonNull
    @Override
    public String getUsage() {
        return "version";
    }

    @Override
    public ListenableFuture<String> run(Arguments args) throws IllegalCommandLineArgumentException {
        try {
            PackageInfo info =
                    appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            return Futures.immediateFuture(
                    String.format(Locale.US, "%s(%d)", info.versionName, info.versionCode));
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
