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

package app.diol.incallui.calllocation;

import android.content.Context;

import app.diol.dialer.inject.HasRootComponent;
import dagger.Subcomponent;

/**
 * Subcomponent that can be used to access the call location implementation.
 */
@Subcomponent
public abstract class CallLocationComponent {

    public static CallLocationComponent get(Context context) {
        return ((HasComponent) ((HasRootComponent) context.getApplicationContext()).component())
                .callLocationComponent();
    }

    public abstract CallLocation getCallLocation();

    /**
     * Used to refer to the root application component.
     */
    public interface HasComponent {
        CallLocationComponent callLocationComponent();
    }
}
