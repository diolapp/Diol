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

package app.diol.dialer.calllog.database;

import dagger.Module;
import dagger.Provides;

/**
 * Binds database dependencies.
 */
@Module
public class CallLogDatabaseModule {

    @Provides
    @AnnotatedCallLogMaxRows
    static int provideMaxRows() {
        /*
         * We sometimes run queries where we potentially pass every ID into a where clause using the
         * (?,?,?,...) syntax. The maximum number of host parameters is 999, so that's the maximum size
         * this table can be. See https://www.sqlite.org/limits.html for more details.
         */
        return 999;
    }
}
