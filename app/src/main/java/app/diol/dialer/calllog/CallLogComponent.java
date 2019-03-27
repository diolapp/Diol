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

package app.diol.dialer.calllog;

import android.content.Context;

import app.diol.dialer.calllog.notifier.RefreshAnnotatedCallLogNotifier;
import app.diol.dialer.inject.HasRootComponent;
import dagger.Subcomponent;

/**
 * Dagger component for the call log package.
 */
@Subcomponent
public abstract class CallLogComponent {

    public static CallLogComponent get(Context context) {
        return ((HasComponent) ((HasRootComponent) context.getApplicationContext()).component())
                .callLogComponent();
    }

    public abstract CallLogFramework callLogFramework();

    public abstract RefreshAnnotatedCallLogNotifier getRefreshAnnotatedCallLogNotifier();

    public abstract RefreshAnnotatedCallLogWorker getRefreshAnnotatedCallLogWorker();

    public abstract ClearMissedCalls getClearMissedCalls();

    /**
     * Used to refer to the root application component.
     */
    public interface HasComponent {
        CallLogComponent callLogComponent();
    }
}
