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
package app.diol.voicemail.impl.mail;

import android.content.Context;

import java.io.File;

/**
 * TempDirectory caches the directory used for caching file. It is set up during
 * application initialization.
 */
public class TempDirectory {
    private static File tempDirectory = null;

    public static File getTempDirectory() {
        if (tempDirectory == null) {
            throw new RuntimeException(
                    "TempDirectory not set.  " + "If in a unit test, call Email.setTempDirectory(context) in setUp().");
        }
        return tempDirectory;
    }

    public static void setTempDirectory(Context context) {
        tempDirectory = context.getCacheDir();
    }
}