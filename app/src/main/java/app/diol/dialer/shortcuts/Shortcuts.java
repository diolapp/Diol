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

package app.diol.dialer.shortcuts;

import android.content.Context;
import android.support.annotation.NonNull;

import app.diol.dialer.configprovider.ConfigProviderComponent;

/**
 * Checks if dynamic shortcuts should be enabled.
 */
public class Shortcuts {

    /**
     * Key for boolean config value which determines whether or not to enable dynamic shortcuts.
     */
    private static final String DYNAMIC_SHORTCUTS_ENABLED = "dynamic_shortcuts_enabled";

    private Shortcuts() {
    }

    static boolean areDynamicShortcutsEnabled(@NonNull Context context) {
        return ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getBoolean(DYNAMIC_SHORTCUTS_ENABLED, true);
    }
}
