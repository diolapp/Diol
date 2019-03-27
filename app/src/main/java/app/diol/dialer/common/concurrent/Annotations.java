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

package app.diol.dialer.common.concurrent;

import javax.inject.Qualifier;

/**
 * Annotations for dagger concurrency bindings.
 */
public class Annotations {

    /**
     * Annotation for retrieving the UI thread.
     */
    @Qualifier
    public @interface Ui {
    }

    /**
     * Annotation for retrieving the non-UI thread pool.
     */
    @Qualifier
    public @interface NonUiParallel {
    }

    /**
     * Annotation for retrieving the non-UI serial executor.
     */
    @Qualifier
    public @interface NonUiSerial {
    }

    /**
     * Annotation for retrieving the UI thread pool.
     */
    @Qualifier
    public @interface UiParallel {
    }

    /**
     * Annotation for retrieving the UI serial executor.
     */
    @Qualifier
    public @interface UiSerial {
    }

    /**
     * Annotation for retrieving the lightweight executor.
     */
    @Qualifier
    public @interface LightweightExecutor {
    }

    /**
     * Annotation for retrieving the background executor.
     */
    @Qualifier
    public @interface BackgroundExecutor {
    }
}
