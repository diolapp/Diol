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

import android.support.annotation.NonNull;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Handles a Command from {@link CommandLineReceiver}.
 */
public interface Command {

    /**
     * Describe the command when "help" is listing available commands.
     */
    @NonNull
    String getShortDescription();

    /**
     * Call when 'command --help' is called or when {@link IllegalCommandLineArgumentException} is
     * thrown to inform the user how should the command be used.
     */
    @NonNull
    String getUsage();

    ListenableFuture<String> run(Arguments args) throws IllegalCommandLineArgumentException;

    /**
     * Thrown when {@code args} in {@link #run(Arguments)} does not match the expected format. The
     * commandline will print {@code message} and {@link #getUsage()}.
     */
    class IllegalCommandLineArgumentException extends Exception {
        public IllegalCommandLineArgumentException(String message) {
            super(message);
        }
    }
}
