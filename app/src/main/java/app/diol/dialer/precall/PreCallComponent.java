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

package app.diol.dialer.precall;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import app.diol.dialer.inject.HasRootComponent;
import dagger.Subcomponent;

/**
 * Daggaer component for {@link PreCall}
 */
@Subcomponent
public abstract class PreCallComponent {
    public static PreCallComponent get(Context context) {
        return ((HasComponent) ((HasRootComponent) context.getApplicationContext()).component())
                .preCallActionsComponent();
    }

    public abstract PreCall getPreCall();

    /**
     * @return a list of {@link PreCallAction} in execution order for the {@link PreCallCoordinator}
     * to run.
     */
    @NonNull
    public abstract ImmutableList<PreCallAction> createActions();

    /**
     * Used to refer to the root application component.
     */
    public interface HasComponent {
        PreCallComponent preCallActionsComponent();
    }
}
