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

package app.diol.dialer.common.cp2;

import android.provider.ContactsContract.Directory;

/**
 * Utilities for {@link Directory}.
 */
public class DirectoryUtils {

    /**
     * Returns true if the given ID belongs to an invisible directory.
     */
    public static boolean isInvisibleDirectoryId(long directoryId) {
        return directoryId == Directory.LOCAL_INVISIBLE
                || directoryId == Directory.ENTERPRISE_LOCAL_INVISIBLE;
    }

    /**
     * Returns true if the given ID belongs to a local enterprise directory.
     */
    public static boolean isLocalEnterpriseDirectoryId(long directoryId) {
        return Directory.isEnterpriseDirectoryId(directoryId)
                && !Directory.isRemoteDirectoryId(directoryId);
    }
}
