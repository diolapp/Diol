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

package app.diol.dialer.preferredsim.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.preferredsim.PreferredSimFallbackContract.PreferredSim;

/**
 * Database helper class for preferred SIM.
 */
public class PreferredSimDatabaseHelper extends SQLiteOpenHelper {

    static final String TABLE = "preferred_sim";

    private static final String CREATE_TABLE_SQL =
            "create table if not exists "
                    + TABLE
                    + " ("
                    + (PreferredSim.DATA_ID + " integer primary key, ")
                    + (PreferredSim.PREFERRED_PHONE_ACCOUNT_COMPONENT_NAME + " text, ")
                    + (PreferredSim.PREFERRED_PHONE_ACCOUNT_ID + " text")
                    + ");";

    PreferredSimDatabaseHelper(Context appContext) {
        super(appContext, "preferred_sim.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.enterBlock("PreferredSimDatabaseHelper.onCreate");
        long startTime = System.currentTimeMillis();
        db.execSQL(CREATE_TABLE_SQL);
        LogUtil.i(
                "PreferredSimDatabaseHelper.onCreate",
                "took: %dms",
                System.currentTimeMillis() - startTime);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
