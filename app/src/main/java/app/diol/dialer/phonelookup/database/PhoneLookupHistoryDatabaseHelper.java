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

package app.diol.dialer.phonelookup.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.phonelookup.database.contract.PhoneLookupHistoryContract.PhoneLookupHistory;

/**
 * {@link SQLiteOpenHelper} for the PhoneLookupHistory database.
 */
@Singleton
public class PhoneLookupHistoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String FILENAME = "phone_lookup_history.db";
    // TODO(zachh): LAST_MODIFIED is no longer read and can be deleted.
    private static final String CREATE_TABLE_SQL =
            "create table if not exists "
                    + PhoneLookupHistory.TABLE
                    + " ("
                    + (PhoneLookupHistory.NORMALIZED_NUMBER + " text primary key not null, ")
                    + (PhoneLookupHistory.PHONE_LOOKUP_INFO + " blob not null, ")
                    + (PhoneLookupHistory.LAST_MODIFIED + " long not null")
                    + ");";
    private static final String CREATE_INDEX_ON_LAST_MODIFIED_SQL =
            "create index last_modified_index on "
                    + PhoneLookupHistory.TABLE
                    + " ("
                    + PhoneLookupHistory.LAST_MODIFIED
                    + ");";
    private final Context appContext;
    private final ListeningExecutorService backgroundExecutor;

    @Inject
    PhoneLookupHistoryDatabaseHelper(
            @ApplicationContext Context appContext,
            @BackgroundExecutor ListeningExecutorService backgroundExecutor) {
        super(appContext, FILENAME, null, 1);

        this.appContext = appContext;
        this.backgroundExecutor = backgroundExecutor;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.enterBlock("PhoneLookupHistoryDatabaseHelper.onCreate");
        long startTime = SystemClock.uptimeMillis();
        db.execSQL(CREATE_TABLE_SQL);
        db.execSQL(CREATE_INDEX_ON_LAST_MODIFIED_SQL);
        // TODO(zachh): Consider logging impression.
        LogUtil.i(
                "PhoneLookupHistoryDatabaseHelper.onCreate",
                "took: %dms",
                SystemClock.uptimeMillis() - startTime);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Closes the database and deletes it.
     */
    public ListenableFuture<Void> delete() {
        return backgroundExecutor.submit(
                () -> {
                    close();
                    appContext.deleteDatabase(FILENAME);
                    return null;
                });
    }
}
