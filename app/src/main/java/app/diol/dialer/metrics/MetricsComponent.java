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

package app.diol.dialer.metrics;

import android.content.Context;

import app.diol.dialer.inject.HasRootComponent;
import app.diol.dialer.inject.IncludeInDialerRoot;
import dagger.Subcomponent;

/**
 * Component for metrics.
 */
@Subcomponent
public abstract class MetricsComponent {

    public static MetricsComponent get(Context context) {
        return ((MetricsComponent.HasComponent)
                ((HasRootComponent) context.getApplicationContext()).component())
                .metricsComponent();
    }

    public abstract Metrics metrics();

    public abstract Metrics.Initializer metricsInitializer();

    public abstract FutureTimer futureTimer();

    /**
     * Used to refer to the root application component.
     */
    @IncludeInDialerRoot
    public interface HasComponent {
        MetricsComponent metricsComponent();
    }
}
