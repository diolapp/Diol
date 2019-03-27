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

package app.diol.dialer.calldetails;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import app.diol.dialer.CoalescedIds;
import app.diol.dialer.calldetails.CallDetailsEntries.CallDetailsEntry;
import app.diol.dialer.calllog.database.contract.AnnotatedCallLogContract.AnnotatedCallLog;
import app.diol.dialer.common.Assert;
import app.diol.dialer.duo.DuoComponent;

/**
 * A {@link CursorLoader} that loads call detail entries from {@link AnnotatedCallLog} for {@link
 * CallDetailsActivity}.
 */
public final class CallDetailsCursorLoader extends CursorLoader {

    // Columns in AnnotatedCallLog that are needed to build a CallDetailsEntry proto.
    // Be sure to update (1) constants that store indexes of the elements and (2) method
    // toCallDetailsEntry(Cursor) when updating this array.
    public static final String[] COLUMNS_FOR_CALL_DETAILS =
            new String[]{
                    AnnotatedCallLog._ID,
                    AnnotatedCallLog.CALL_TYPE,
                    AnnotatedCallLog.FEATURES,
                    AnnotatedCallLog.TIMESTAMP,
                    AnnotatedCallLog.DURATION,
                    AnnotatedCallLog.DATA_USAGE,
                    AnnotatedCallLog.PHONE_ACCOUNT_COMPONENT_NAME,
                    AnnotatedCallLog.CALL_MAPPING_ID
            };

    // Indexes for COLUMNS_FOR_CALL_DETAILS
    private static final int ID = 0;
    private static final int CALL_TYPE = 1;
    private static final int FEATURES = 2;
    private static final int TIMESTAMP = 3;
    private static final int DURATION = 4;
    private static final int DATA_USAGE = 5;
    private static final int PHONE_ACCOUNT_COMPONENT_NAME = 6;
    private static final int CALL_MAPPING_ID = 7;

    CallDetailsCursorLoader(Context context, CoalescedIds coalescedIds) {
        super(
                context,
                AnnotatedCallLog.CONTENT_URI,
                COLUMNS_FOR_CALL_DETAILS,
                annotatedCallLogIdsSelection(coalescedIds),
                annotatedCallLogIdsSelectionArgs(coalescedIds),
                AnnotatedCallLog.TIMESTAMP + " DESC");
    }

    /**
     * Build a string of the form "COLUMN_NAME IN (?, ?, ..., ?)", where COLUMN_NAME is the name of
     * the ID column in {@link AnnotatedCallLog}.
     *
     * <p>This string will be used as the {@code selection} parameter to initialize the loader.
     */
    private static String annotatedCallLogIdsSelection(CoalescedIds coalescedIds) {
        // First, build a string of question marks ('?') separated by commas (',').
        StringBuilder questionMarks = new StringBuilder();
        for (int i = 0; i < coalescedIds.getCoalescedIdCount(); i++) {
            if (i != 0) {
                questionMarks.append(", ");
            }
            questionMarks.append("?");
        }

        return AnnotatedCallLog._ID + " IN (" + questionMarks + ")";
    }

    /**
     * Returns a string that will be used as the {@code selectionArgs} parameter to initialize the
     * loader.
     */
    private static String[] annotatedCallLogIdsSelectionArgs(CoalescedIds coalescedIds) {
        String[] args = new String[coalescedIds.getCoalescedIdCount()];

        for (int i = 0; i < coalescedIds.getCoalescedIdCount(); i++) {
            args[i] = String.valueOf(coalescedIds.getCoalescedId(i));
        }

        return args;
    }

    /**
     * Creates a new {@link CallDetailsEntries} from the entire data set loaded by this loader.
     *
     * @param cursor A cursor pointing to the data set loaded by this loader. The caller must ensure
     *               the cursor is not null and the data set it points to is not empty.
     * @return A {@link CallDetailsEntries} proto.
     */
    static CallDetailsEntries toCallDetailsEntries(Context context, Cursor cursor) {
        Assert.isNotNull(cursor);
        Assert.checkArgument(cursor.moveToFirst());

        CallDetailsEntries.Builder entries = CallDetailsEntries.newBuilder();

        do {
            entries.addEntries(toCallDetailsEntry(context, cursor));
        } while (cursor.moveToNext());

        return entries.build();
    }

    /**
     * Creates a new {@link CallDetailsEntry} from the provided cursor using its current position.
     */
    private static CallDetailsEntry toCallDetailsEntry(Context context, Cursor cursor) {
        CallDetailsEntry.Builder entry = CallDetailsEntry.newBuilder();
        entry
                .setCallId(cursor.getLong(ID))
                .setCallType(cursor.getInt(CALL_TYPE))
                .setFeatures(cursor.getInt(FEATURES))
                .setDate(cursor.getLong(TIMESTAMP))
                .setDuration(cursor.getLong(DURATION))
                .setDataUsage(cursor.getLong(DATA_USAGE))
                .setCallMappingId(cursor.getString(CALL_MAPPING_ID));

        String phoneAccountComponentName = cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME);
        entry.setIsDuoCall(DuoComponent.get(context).getDuo().isDuoAccount(phoneAccountComponentName));

        return entry.build();
    }

    @Override
    public void onContentChanged() {
        // Do nothing here.
        // This is to prevent the loader to reload data when Loader.ForceLoadContentObserver detects a
        // change.
        // Without this, the app will crash when the user deletes call details as the deletion triggers
        // the data loading but no data can be fetched and we want to ensure the data set is not empty
        // when building CallDetailsEntries proto (see toCallDetailsEntries(Cursor)).
        //
        // OldCallDetailsActivity doesn't respond to underlying data changes and we decided to keep it
        // that way in CallDetailsActivity.
    }
}
