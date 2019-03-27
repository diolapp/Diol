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

package app.diol.dialer.database;

import android.content.Context;

import java.util.Objects;

/**
 * Accessor for the database bindings.
 */
public class Database {

    private static DatabaseBindings databaseBindings;

    private Database() {
    }

    public static DatabaseBindings get(Context context) {
        Objects.requireNonNull(context);
        if (databaseBindings != null) {
            return databaseBindings;
        }

        Context application = context.getApplicationContext();
        if (application instanceof DatabaseBindingsFactory) {
            databaseBindings = ((DatabaseBindingsFactory) application).newDatabaseBindings();
        }

        if (databaseBindings == null) {
            databaseBindings = new DatabaseBindingsStub();
        }
        return databaseBindings;
    }

    public static void setForTesting(DatabaseBindings databaseBindings) {
        Database.databaseBindings = databaseBindings;
    }
}
