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
package app.diol.incallui;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.Nullable;
import android.telecom.Call;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import app.diol.dialer.calllog.config.CallLogConfigComponent;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.phonelookup.PhoneLookupComponent;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.database.contract.PhoneLookupHistoryContract.PhoneLookupHistory;
import app.diol.dialer.telecom.TelecomCallUtil;

/**
 * Fetches the current {@link PhoneLookupInfo} for the provided call and writes it to the
 * PhoneLookupHistory.
 */
final class PhoneLookupHistoryRecorder {

    /**
     * If the call log framework is enabled, fetches the current {@link PhoneLookupInfo} for the
     * provided call and writes it to the PhoneLookupHistory. Otherwise does nothing.
     */
    static void recordPhoneLookupInfo(Context appContext, Call call) {
        if (!CallLogConfigComponent.get(appContext).callLogConfig().isCallLogFrameworkEnabled()) {
            return;
        }

        ListenableFuture<PhoneLookupInfo> infoFuture =
                PhoneLookupComponent.get(appContext).compositePhoneLookup().lookup(call);

        Futures.addCallback(
                infoFuture,
                new FutureCallback<PhoneLookupInfo>() {
                    @Override
                    public void onSuccess(@Nullable PhoneLookupInfo result) {
                        Assert.checkArgument(result != null);
                        Optional<String> normalizedNumber =
                                TelecomCallUtil.getNormalizedNumber(appContext, call);
                        if (!normalizedNumber.isPresent()) {
                            LogUtil.w("PhoneLookupHistoryRecorder.onSuccess", "couldn't get a number");
                            return;
                        }
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PhoneLookupHistory.PHONE_LOOKUP_INFO, result.toByteArray());
                        contentValues.put(PhoneLookupHistory.LAST_MODIFIED, System.currentTimeMillis());
                        appContext
                                .getContentResolver()
                                .update(
                                        PhoneLookupHistory.contentUriForNumber(normalizedNumber.get()),
                                        contentValues,
                                        null,
                                        null);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // TODO(zachh): Consider how to best handle this; take measures to repair call log?
                        LogUtil.w(
                                "PhoneLookupHistoryRecorder.onFailure", "could not write PhoneLookupHistory", t);
                    }
                },
                DialerExecutorComponent.get(appContext).backgroundExecutor());
    }
}
