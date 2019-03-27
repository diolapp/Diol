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

package app.diol.dialer.phonelookup.blockednumber;

import android.content.Context;
import android.database.Cursor;
import android.provider.BlockedNumberContract.BlockedNumbers;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.ArraySet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Set;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.blocking.FilteredNumberCompat;
import app.diol.dialer.calllog.observer.MarkDirtyObserver;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.common.database.Selection;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.phonelookup.PhoneLookup;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.PhoneLookupInfo.BlockedState;
import app.diol.dialer.phonelookup.PhoneLookupInfo.Builder;
import app.diol.dialer.phonelookup.PhoneLookupInfo.SystemBlockedNumberInfo;
import app.diol.dialer.phonenumberproto.PartitionedNumbers;

/**
 * Lookup blocked numbers in the system database. Requires N+ and migration from dialer database
 * completed (need user consent to move data into system).
 */
public class SystemBlockedNumberPhoneLookup implements PhoneLookup<SystemBlockedNumberInfo> {

    private final Context appContext;
    private final ListeningExecutorService executorService;
    private final MarkDirtyObserver markDirtyObserver;

    @Inject
    SystemBlockedNumberPhoneLookup(
            @ApplicationContext Context appContext,
            @BackgroundExecutor ListeningExecutorService executorService,
            MarkDirtyObserver markDirtyObserver) {
        this.appContext = appContext;
        this.executorService = executorService;
        this.markDirtyObserver = markDirtyObserver;
    }

    @Override
    public ListenableFuture<SystemBlockedNumberInfo> lookup(@NonNull DialerPhoneNumber number) {
        if (!FilteredNumberCompat.useNewFiltering(appContext)) {
            return Futures.immediateFuture(SystemBlockedNumberInfo.getDefaultInstance());
        }
        return executorService.submit(() -> queryNumbers(ImmutableSet.of(number)).get(number));
    }

    @Override
    public ListenableFuture<Boolean> isDirty(ImmutableSet<DialerPhoneNumber> phoneNumbers) {
        // Dirty state is recorded with PhoneLookupDataSource.markDirtyAndNotify(), which will force
        // rebuild with the CallLogFramework
        return Futures.immediateFuture(false);
    }

    @Override
    public ListenableFuture<ImmutableMap<DialerPhoneNumber, SystemBlockedNumberInfo>>
    getMostRecentInfo(ImmutableMap<DialerPhoneNumber, SystemBlockedNumberInfo> existingInfoMap) {
        LogUtil.enterBlock("SystemBlockedNumberPhoneLookup.getMostRecentPhoneLookupInfo");
        if (!FilteredNumberCompat.useNewFiltering(appContext)) {
            return Futures.immediateFuture(existingInfoMap);
        }
        return executorService.submit(() -> queryNumbers(existingInfoMap.keySet()));
    }

    @WorkerThread
    private ImmutableMap<DialerPhoneNumber, SystemBlockedNumberInfo> queryNumbers(
            ImmutableSet<DialerPhoneNumber> numbers) {
        Assert.isWorkerThread();
        PartitionedNumbers partitionedNumbers = new PartitionedNumbers(numbers);

        Set<DialerPhoneNumber> blockedNumbers = new ArraySet<>();

        Selection normalizedSelection =
                Selection.column(BlockedNumbers.COLUMN_E164_NUMBER)
                        .in(partitionedNumbers.validE164Numbers());
        try (Cursor cursor =
                     appContext
                             .getContentResolver()
                             .query(
                                     BlockedNumbers.CONTENT_URI,
                                     new String[]{BlockedNumbers.COLUMN_E164_NUMBER},
                                     normalizedSelection.getSelection(),
                                     normalizedSelection.getSelectionArgs(),
                                     null)) {
            while (cursor != null && cursor.moveToNext()) {
                blockedNumbers.addAll(
                        partitionedNumbers.dialerPhoneNumbersForValidE164(cursor.getString(0)));
            }
        }

        Selection rawSelection =
                Selection.column(BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
                        .in(partitionedNumbers.invalidNumbers());
        try (Cursor cursor =
                     appContext
                             .getContentResolver()
                             .query(
                                     BlockedNumbers.CONTENT_URI,
                                     new String[]{BlockedNumbers.COLUMN_ORIGINAL_NUMBER},
                                     rawSelection.getSelection(),
                                     rawSelection.getSelectionArgs(),
                                     null)) {
            while (cursor != null && cursor.moveToNext()) {
                blockedNumbers.addAll(partitionedNumbers.dialerPhoneNumbersForInvalid(cursor.getString(0)));
            }
        }

        ImmutableMap.Builder<DialerPhoneNumber, SystemBlockedNumberInfo> result =
                ImmutableMap.builder();

        for (DialerPhoneNumber number : numbers) {
            result.put(
                    number,
                    SystemBlockedNumberInfo.newBuilder()
                            .setBlockedState(
                                    blockedNumbers.contains(number) ? BlockedState.BLOCKED : BlockedState.NOT_BLOCKED)
                            .build());
        }

        return result.build();
    }

    @Override
    public void setSubMessage(Builder phoneLookupInfo, SystemBlockedNumberInfo subMessage) {
        phoneLookupInfo.setSystemBlockedNumberInfo(subMessage);
    }

    @Override
    public SystemBlockedNumberInfo getSubMessage(PhoneLookupInfo phoneLookupInfo) {
        return phoneLookupInfo.getSystemBlockedNumberInfo();
    }

    @Override
    public ListenableFuture<Void> onSuccessfulBulkUpdate() {
        return Futures.immediateFuture(null);
    }

    @Override
    public void registerContentObservers() {
        appContext
                .getContentResolver()
                .registerContentObserver(
                        BlockedNumbers.CONTENT_URI,
                        true, // BlockedNumbers notifies on the item
                        markDirtyObserver);
    }

    @Override
    public void unregisterContentObservers() {
        appContext.getContentResolver().unregisterContentObserver(markDirtyObserver);
    }

    @Override
    public ListenableFuture<Void> clearData() {
        return Futures.immediateFuture(null);
    }

    @Override
    public String getLoggingName() {
        return "SystemBlockedNumberPhoneLookup";
    }
}
