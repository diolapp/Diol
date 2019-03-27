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

package app.diol.dialer.phonelookup.cnap;

import android.content.Context;
import android.database.Cursor;
import android.telecom.Call;
import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.common.database.Selection;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.phonelookup.PhoneLookup;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.PhoneLookupInfo.CnapInfo;
import app.diol.dialer.phonelookup.database.contract.PhoneLookupHistoryContract.PhoneLookupHistory;

/**
 * PhoneLookup implementation for CNAP info.
 */
public final class CnapPhoneLookup implements PhoneLookup<CnapInfo> {

    private final Context appContext;
    private final ListeningExecutorService backgroundExecutorService;

    @Inject
    CnapPhoneLookup(
            @ApplicationContext Context appContext,
            @BackgroundExecutor ListeningExecutorService backgroundExecutorService) {
        this.appContext = appContext;
        this.backgroundExecutorService = backgroundExecutorService;
    }

    /**
     * Override the default implementation in {@link PhoneLookup#lookup(Context, Call)} as CNAP info
     * is in the provided {@link Call}.
     */
    @Override
    public ListenableFuture<CnapInfo> lookup(Context appContext, Call call) {
        String callerDisplayName = call.getDetails().getCallerDisplayName();
        return Futures.immediateFuture(
                TextUtils.isEmpty(callerDisplayName)
                        ? CnapInfo.getDefaultInstance()
                        : CnapInfo.newBuilder().setName(callerDisplayName).build());
    }

    /**
     * CNAP info cannot be retrieved when all we have is a number. The best we can do is returning the
     * existing info in {@link PhoneLookupHistory}.
     */
    @Override
    public ListenableFuture<CnapInfo> lookup(DialerPhoneNumber dialerPhoneNumber) {
        return backgroundExecutorService.submit(
                () -> {
                    Selection selection =
                            Selection.builder()
                                    .and(
                                            Selection.column(PhoneLookupHistory.NORMALIZED_NUMBER)
                                                    .is("=", dialerPhoneNumber.getNormalizedNumber()))
                                    .build();

                    try (Cursor cursor =
                                 appContext
                                         .getContentResolver()
                                         .query(
                                                 PhoneLookupHistory.CONTENT_URI,
                                                 new String[]{PhoneLookupHistory.PHONE_LOOKUP_INFO},
                                                 selection.getSelection(),
                                                 selection.getSelectionArgs(),
                                                 /* sortOrder = */ null)) {
                        if (cursor == null) {
                            LogUtil.e("CnapPhoneLookup.lookup", "null cursor");
                            return CnapInfo.getDefaultInstance();
                        }

                        if (!cursor.moveToFirst()) {
                            LogUtil.i("CnapPhoneLookup.lookup", "empty cursor");
                            return CnapInfo.getDefaultInstance();
                        }

                        // At ths point, we expect only one row in the cursor as
                        // PhoneLookupHistory.NORMALIZED_NUMBER is the primary key of table PhoneLookupHistory.
                        Assert.checkState(cursor.getCount() == 1);

                        int phoneLookupInfoColumn =
                                cursor.getColumnIndexOrThrow(PhoneLookupHistory.PHONE_LOOKUP_INFO);
                        PhoneLookupInfo phoneLookupInfo;
                        try {
                            phoneLookupInfo = PhoneLookupInfo.parseFrom(cursor.getBlob(phoneLookupInfoColumn));
                        } catch (InvalidProtocolBufferException e) {
                            throw new IllegalStateException(e);
                        }

                        return phoneLookupInfo.getCnapInfo();
                    }
                });
    }

    @Override
    public ListenableFuture<Boolean> isDirty(ImmutableSet<DialerPhoneNumber> phoneNumbers) {
        return Futures.immediateFuture(false);
    }

    @Override
    public ListenableFuture<ImmutableMap<DialerPhoneNumber, CnapInfo>> getMostRecentInfo(
            ImmutableMap<DialerPhoneNumber, CnapInfo> existingInfoMap) {
        return Futures.immediateFuture(existingInfoMap);
    }

    @Override
    public void setSubMessage(PhoneLookupInfo.Builder destination, CnapInfo subMessage) {
        destination.setCnapInfo(subMessage);
    }

    @Override
    public CnapInfo getSubMessage(PhoneLookupInfo phoneLookupInfo) {
        return phoneLookupInfo.getCnapInfo();
    }

    @Override
    public ListenableFuture<Void> onSuccessfulBulkUpdate() {
        return Futures.immediateFuture(null);
    }

    @Override
    public void registerContentObservers() {
        // No content observers for CNAP info.
    }

    @Override
    public void unregisterContentObservers() {
        // No content observers for CNAP info.
    }

    @Override
    public ListenableFuture<Void> clearData() {
        return Futures.immediateFuture(null);
    }

    @Override
    public String getLoggingName() {
        return "CnapPhoneLookup";
    }
}
