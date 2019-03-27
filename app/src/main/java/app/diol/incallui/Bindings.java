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

package app.diol.incallui;

import android.content.Context;

import java.util.Objects;

import app.diol.incallui.bindings.InCallUiBindings;
import app.diol.incallui.bindings.InCallUiBindingsFactory;
import app.diol.incallui.bindings.InCallUiBindingsStub;

/**
 * Accessor for the in call UI bindings.
 */
public class Bindings {

    private static InCallUiBindings instance;

    private Bindings() {
    }

    public static InCallUiBindings get(Context context) {
        Objects.requireNonNull(context);
        if (instance != null) {
            return instance;
        }

        Context application = context.getApplicationContext();
        if (application instanceof InCallUiBindingsFactory) {
            instance = ((InCallUiBindingsFactory) application).newInCallUiBindings();
        }

        if (instance == null) {
            instance = new InCallUiBindingsStub();
        }
        return instance;
    }

    public static void setForTesting(InCallUiBindings testInstance) {
        instance = testInstance;
    }
}
