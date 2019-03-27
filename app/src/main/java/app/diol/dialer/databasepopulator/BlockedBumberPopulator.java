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

package app.diol.dialer.databasepopulator;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.BlockedNumberContract;
import android.provider.BlockedNumberContract.BlockedNumbers;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.diol.dialer.common.Assert;

/**
 * Populates the device database with blocked number entries.
 */
public class BlockedBumberPopulator {

    private static final List<ContentValues> values =
            Arrays.asList(
                    createContentValuesWithNumber("123456789"), createContentValuesWithNumber("987654321"));

    public static void populateBlockedNumber(@NonNull Context context) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (ContentValues value : values) {
            operations.add(
                    ContentProviderOperation.newInsert(BlockedNumbers.CONTENT_URI)
                            .withValues(value)
                            .withYieldAllowed(true)
                            .build());
        }
        try {
            context.getContentResolver().applyBatch(BlockedNumberContract.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            Assert.fail("error adding block number entries: " + e);
        }
    }

    public static void deleteBlockedNumbers(@NonNull Context context) {
        // clean BlockedNumbers db
        context.getContentResolver().delete(BlockedNumbers.CONTENT_URI, null, null);
    }

    private static ContentValues createContentValuesWithNumber(String number) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number);
        return contentValues;
    }
}
