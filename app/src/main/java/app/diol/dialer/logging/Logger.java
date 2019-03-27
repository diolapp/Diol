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

package app.diol.dialer.logging;

import android.content.Context;

import java.util.Objects;

/**
 * Single entry point for all logging/analytics-related work for all user interactions.
 */
public class Logger {

    private static LoggingBindings loggingBindings;

    private Logger() {
    }

    public static LoggingBindings get(Context context) {
        Objects.requireNonNull(context);
        if (loggingBindings != null) {
            return loggingBindings;
        }

        Context application = context.getApplicationContext();
        if (application instanceof LoggingBindingsFactory) {
            loggingBindings = ((LoggingBindingsFactory) application).newLoggingBindings();
        }

        if (loggingBindings == null) {
            loggingBindings = new LoggingBindingsStub();
        }
        return loggingBindings;
    }

    public static void setForTesting(LoggingBindings loggingBindings) {
        Logger.loggingBindings = loggingBindings;
    }
}
