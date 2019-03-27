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

package app.diol.dialer.app;

import android.content.Context;

import java.util.Objects;

import app.diol.dialer.app.legacybindings.DialerLegacyBindings;
import app.diol.dialer.app.legacybindings.DialerLegacyBindingsFactory;
import app.diol.dialer.app.legacybindings.DialerLegacyBindingsStub;

/**
 * Accessor for the in call UI bindings.
 */
public class Bindings {

    private static DialerLegacyBindings legacyInstance;

    private Bindings() {
    }

    public static DialerLegacyBindings getLegacy(Context context) {
        Objects.requireNonNull(context);
        if (legacyInstance != null) {
            return legacyInstance;
        }

        Context application = context.getApplicationContext();
        if (application instanceof DialerLegacyBindingsFactory) {
            legacyInstance = ((DialerLegacyBindingsFactory) application).newDialerLegacyBindings();
        }

        if (legacyInstance == null) {
            legacyInstance = new DialerLegacyBindingsStub();
        }
        return legacyInstance;
    }

}
