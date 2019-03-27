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

package app.diol.dialer.preferredsim.suggestion;

import android.content.Context;
import android.support.annotation.WorkerThread;

import app.diol.dialer.common.Assert;
import app.diol.dialer.inject.HasRootComponent;
import dagger.Subcomponent;

/**
 * Dagger component for {@link SuggestionProvider}
 */
@Subcomponent
public abstract class SimSuggestionComponent {
    @WorkerThread
    public static SimSuggestionComponent get(Context context) {
        Assert.isWorkerThread();
        return ((HasComponent) ((HasRootComponent) context.getApplicationContext()).component())
                .simSuggestionComponent();
    }

    public abstract SuggestionProvider getSuggestionProvider();

    /**
     * Used to refer to the root application component.
     */
    public interface HasComponent {
        SimSuggestionComponent simSuggestionComponent();
    }
}
