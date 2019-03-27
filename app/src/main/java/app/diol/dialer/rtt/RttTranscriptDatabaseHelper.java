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

package app.diol.dialer.rtt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.rtt.RttTranscriptContract.RttTranscriptColumn;

/**
 * Database helper class for RTT transcript.
 */
final class RttTranscriptDatabaseHelper extends SQLiteOpenHelper {

    static final String TABLE = "rtt_transcript";

    private static final String CREATE_TABLE_SQL =
            "create table if not exists "
                    + TABLE
                    + " ("
                    + (RttTranscriptColumn.TRANSCRIPT_ID + " integer primary key, ")
                    + (RttTranscriptColumn.TRANSCRIPT_DATA + " blob not null")
                    + ");";

    RttTranscriptDatabaseHelper(Context context) {
        super(context, "rtt_transcript.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.enterBlock("RttTranscriptDatabaseHelper.onCreate");
        long startTime = SystemClock.elapsedRealtime();
        db.execSQL(CREATE_TABLE_SQL);
        LogUtil.i(
                "RttTranscriptDatabaseHelper.onCreate",
                "took: %dms",
                SystemClock.elapsedRealtime() - startTime);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
