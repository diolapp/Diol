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

package app.diol.dialer.phonelookup.cp2;

import android.content.Context;
import android.database.Cursor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.function.Predicate;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.common.concurrent.Annotations.LightweightExecutor;
import app.diol.dialer.common.database.Selection;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.PhoneLookupInfo.Cp2Info;
import app.diol.dialer.phonelookup.database.contract.PhoneLookupHistoryContract.PhoneLookupHistory;

/**
 * Shared logic for handling missing permissions in CP2 lookups.
 */
final class MissingPermissionsOperations {

    private final Context appContext;
    private final ListeningExecutorService backgroundExecutor;
    private final ListeningExecutorService lightweightExecutor;

    @Inject
    MissingPermissionsOperations(
            @ApplicationContext Context appContext,
            @BackgroundExecutor ListeningExecutorService backgroundExecutor,
            @LightweightExecutor ListeningExecutorService lightweightExecutor) {
        this.appContext = appContext;
        this.backgroundExecutor = backgroundExecutor;
        this.lightweightExecutor = lightweightExecutor;
    }

    /**
     * Returns true if there is any CP2 data for the specified numbers in PhoneLookupHistory, because
     * that data needs to be cleared.
     *
     * <p>Note: This might be a little slow for users without contacts permissions, but we don't
     * expect this to often be the case. If necessary, a shared pref could be used to track the
     * permission state as an optimization.
     */
    ListenableFuture<Boolean> isDirtyForMissingPermissions(
            ImmutableSet<DialerPhoneNumber> phoneNumbers,
            Predicate<PhoneLookupInfo> phoneLookupInfoIsDirtyFn) {
        return backgroundExecutor.submit(
                () -> {
                    // Note: This loses country info when number is not valid.
                    String[] normalizedNumbers =
                            phoneNumbers
                                    .stream()
                                    .map(DialerPhoneNumber::getNormalizedNumber)
                                    .toArray(String[]::new);

                    Selection selection =
                            Selection.builder()
                                    .and(Selection.column(PhoneLookupHistory.NORMALIZED_NUMBER).in(normalizedNumbers))
                                    .build();

                    try (Cursor cursor =
                                 appContext
                                         .getContentResolver()
                                         .query(
                                                 PhoneLookupHistory.CONTENT_URI,
                                                 new String[]{
                                                         PhoneLookupHistory.PHONE_LOOKUP_INFO,
                                                 },
                                                 selection.getSelection(),
                                                 selection.getSelectionArgs(),
                                                 null)) {

                        if (cursor == null) {
                            LogUtil.w("MissingPermissionsOperations.isDirtyForMissingPermissions", "null cursor");
                            return false;
                        }

                        if (cursor.moveToFirst()) {
                            int phoneLookupInfoColumn =
                                    cursor.getColumnIndexOrThrow(PhoneLookupHistory.PHONE_LOOKUP_INFO);
                            do {
                                PhoneLookupInfo phoneLookupInfo;
                                try {
                                    phoneLookupInfo =
                                            PhoneLookupInfo.parseFrom(cursor.getBlob(phoneLookupInfoColumn));
                                } catch (InvalidProtocolBufferException e) {
                                    throw new IllegalStateException(e);
                                }
                                if (phoneLookupInfoIsDirtyFn.test(phoneLookupInfo)) {
                                    return true;
                                }
                            } while (cursor.moveToNext());
                        }
                    }
                    return false;
                });
    }

    /**
     * Clears all CP2 info because permissions are missing.
     */
    ListenableFuture<ImmutableMap<DialerPhoneNumber, Cp2Info>> getMostRecentInfoForMissingPermissions(
            ImmutableMap<DialerPhoneNumber, Cp2Info> existingInfoMap) {
        return lightweightExecutor.submit(
                () -> {
                    ImmutableMap.Builder<DialerPhoneNumber, Cp2Info> clearedInfos = ImmutableMap.builder();
                    for (DialerPhoneNumber number : existingInfoMap.keySet()) {
                        clearedInfos.put(number, Cp2Info.getDefaultInstance());
                    }
                    return clearedInfos.build();
                });
    }
}
