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
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.diol.dialer.common.Assert;

/**
 * Populates the device database with call log entries.
 */
public final class CallLogPopulator {
    // Phone numbers from https://www.google.com/about/company/facts/locations/
    private static final CallEntry.Builder[] SIMPLE_CALL_LOG = {
            CallEntry.builder().setType(Calls.MISSED_TYPE).setNumber("+1-302-6365454"),
            CallEntry.builder()
                    .setType(Calls.MISSED_TYPE)
                    .setNumber("")
                    .setPresentation(Calls.PRESENTATION_UNKNOWN),
            CallEntry.builder().setType(Calls.REJECTED_TYPE).setNumber("+1-302-6365454"),
            CallEntry.builder().setType(Calls.INCOMING_TYPE).setNumber("+1-302-6365454"),
            CallEntry.builder()
                    .setType(Calls.MISSED_TYPE)
                    .setNumber("1234")
                    .setPresentation(Calls.PRESENTATION_RESTRICTED),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("+1-302-6365454"),
            CallEntry.builder().setType(Calls.BLOCKED_TYPE).setNumber("+1-302-6365454"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("(425) 739-5600"),
            CallEntry.builder().setType(Calls.ANSWERED_EXTERNALLY_TYPE).setNumber("(425) 739-5600"),
            CallEntry.builder().setType(Calls.MISSED_TYPE).setNumber("+1 (425) 739-5600"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("739-5600"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("711"),
            CallEntry.builder().setType(Calls.INCOMING_TYPE).setNumber("711"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("(425) 739-5600"),
            CallEntry.builder().setType(Calls.MISSED_TYPE).setNumber("+44 (0) 20 7031 3000"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("+1-650-2530000"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("+1 303-245-0086;123,456"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("+1 303-245-0086"),
            CallEntry.builder().setType(Calls.INCOMING_TYPE).setNumber("+1-650-2530000"),
            CallEntry.builder().setType(Calls.MISSED_TYPE).setNumber("650-2530000"),
            CallEntry.builder().setType(Calls.REJECTED_TYPE).setNumber("2530000"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("+1 404-487-9000"),
            CallEntry.builder().setType(Calls.INCOMING_TYPE).setNumber("+61 2 9374 4001"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("+33 (0)1 42 68 53 00"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("972-74-746-6245"),
            CallEntry.builder().setType(Calls.INCOMING_TYPE).setNumber("+971 4 4509500"),
            CallEntry.builder().setType(Calls.INCOMING_TYPE).setNumber("+971 4 4509500"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("55-31-2128-6800"),
            CallEntry.builder().setType(Calls.MISSED_TYPE).setNumber("611"),
            CallEntry.builder().setType(Calls.OUTGOING_TYPE).setNumber("*86 512-343-5283"),
    };

    private CallLogPopulator() {
    }

    @WorkerThread
    public static void populateCallLog(@NonNull Context context, boolean isWithoutMissedCalls) {
        populateCallLog(context, isWithoutMissedCalls, false);
    }

    @WorkerThread
    public static void populateCallLog(@NonNull Context context) {
        populateCallLog(context, false);
    }

    @WorkerThread
    public static void populateCallLog(
            @NonNull Context context, boolean isWithoutMissedCalls, boolean fastMode) {
        Assert.isWorkerThread();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        // Do this 4 times to make the call log 4 times bigger.
        long timeMillis = System.currentTimeMillis();
        List<CallEntry.Builder> callLogs = new ArrayList<>();
        if (fastMode) {
            callLogs.add(SIMPLE_CALL_LOG[0]);
        } else {
            callLogs = Arrays.asList(SIMPLE_CALL_LOG);
        }
        for (int i = 0; i < 4; i++) {
            for (CallEntry.Builder builder : callLogs) {
                CallEntry callEntry = builder.setTimeMillis(timeMillis).build();
                if (isWithoutMissedCalls && builder.build().getType() == Calls.MISSED_TYPE) {
                    continue;
                }
                operations.add(
                        ContentProviderOperation.newInsert(Calls.CONTENT_URI)
                                .withValues(callEntry.getAsContentValues())
                                .withYieldAllowed(true)
                                .build());
                timeMillis -= TimeUnit.HOURS.toMillis(1);
            }
        }
        try {
            context.getContentResolver().applyBatch(CallLog.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            Assert.fail("error adding call entries: " + e);
        }
    }

    @WorkerThread
    public static void populateCallLogWithoutMissed(@NonNull Context context) {
        populateCallLog(context, true);
    }

    @WorkerThread
    public static void deleteAllCallLog(@NonNull Context context) {
        Assert.isWorkerThread();
        try {
            context
                    .getContentResolver()
                    .applyBatch(
                            CallLog.AUTHORITY,
                            new ArrayList<>(
                                    Arrays.asList(ContentProviderOperation.newDelete(Calls.CONTENT_URI).build())));
        } catch (RemoteException | OperationApplicationException e) {
            Assert.fail("failed to delete call log: " + e);
        }
    }

    @AutoValue
    abstract static class CallEntry {
        static Builder builder() {
            return new AutoValue_CallLogPopulator_CallEntry.Builder()
                    .setPresentation(Calls.PRESENTATION_ALLOWED);
        }

        @NonNull
        abstract String getNumber();

        abstract int getType();

        abstract int getPresentation();

        abstract long getTimeMillis();

        ContentValues getAsContentValues() {
            ContentValues values = new ContentValues();
            values.put(Calls.TYPE, getType());
            values.put(Calls.NUMBER, getNumber());
            values.put(Calls.NUMBER_PRESENTATION, getPresentation());
            values.put(Calls.DATE, getTimeMillis());
            return values;
        }

        @AutoValue.Builder
        abstract static class Builder {
            abstract Builder setNumber(@NonNull String number);

            abstract Builder setType(int type);

            abstract Builder setPresentation(int presentation);

            abstract Builder setTimeMillis(long timeMillis);

            abstract CallEntry build();
        }
    }
}
