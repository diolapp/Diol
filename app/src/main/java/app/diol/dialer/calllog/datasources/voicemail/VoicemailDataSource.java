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

package app.diol.dialer.calllog.datasources.voicemail;

import android.content.ContentValues;
import android.content.Context;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Map.Entry;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.calllog.database.contract.AnnotatedCallLogContract.AnnotatedCallLog;
import app.diol.dialer.calllog.datasources.CallLogDataSource;
import app.diol.dialer.calllog.datasources.CallLogMutations;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.compat.telephony.TelephonyManagerCompat;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.telecom.TelecomUtil;
import app.diol.dialer.util.PermissionsUtil;

/**
 * Provide information for whether the call is a call to the voicemail inbox.
 */
public class VoicemailDataSource implements CallLogDataSource {

    private final Context appContext;
    private final ListeningExecutorService backgroundExecutor;

    @Inject
    VoicemailDataSource(
            @ApplicationContext Context appContext,
            @BackgroundExecutor ListeningExecutorService backgroundExecutor) {
        this.appContext = appContext;
        this.backgroundExecutor = backgroundExecutor;
    }

    @Override
    public ListenableFuture<Boolean> isDirty() {
        // The isVoicemail status is immutable and permanent. The call will always show as "Voicemail"
        // even if the SIM is swapped. Dialing the row will result in some unexpected number after a SIM
        // swap but this is deemed acceptable.
        return Futures.immediateFuture(false);
    }

    @Override
    @SuppressWarnings("missingPermission")
    public ListenableFuture<Void> fill(CallLogMutations mutations) {
        if (!PermissionsUtil.hasReadPhoneStatePermissions(appContext)) {
            for (Entry<Long, ContentValues> insert : mutations.getInserts().entrySet()) {
                insert.getValue().put(AnnotatedCallLog.IS_VOICEMAIL_CALL, 0);
            }
            return Futures.immediateFuture(null);
        }

        return backgroundExecutor.submit(
                () -> {
                    TelecomManager telecomManager = appContext.getSystemService(TelecomManager.class);
                    for (Entry<Long, ContentValues> insert : mutations.getInserts().entrySet()) {
                        ContentValues values = insert.getValue();
                        PhoneAccountHandle phoneAccountHandle =
                                TelecomUtil.composePhoneAccountHandle(
                                        values.getAsString(AnnotatedCallLog.PHONE_ACCOUNT_COMPONENT_NAME),
                                        values.getAsString(AnnotatedCallLog.PHONE_ACCOUNT_ID));
                        DialerPhoneNumber dialerPhoneNumber;
                        try {
                            dialerPhoneNumber =
                                    DialerPhoneNumber.parseFrom(values.getAsByteArray(AnnotatedCallLog.NUMBER));
                        } catch (InvalidProtocolBufferException e) {
                            throw new IllegalStateException(e);
                        }

                        if (telecomManager.isVoiceMailNumber(
                                phoneAccountHandle, dialerPhoneNumber.getNormalizedNumber())) {
                            values.put(AnnotatedCallLog.IS_VOICEMAIL_CALL, 1);
                            TelephonyManager telephonyManager =
                                    TelephonyManagerCompat.getTelephonyManagerForPhoneAccountHandle(
                                            appContext, phoneAccountHandle);
                            values.put(
                                    AnnotatedCallLog.VOICEMAIL_CALL_TAG, telephonyManager.getVoiceMailAlphaTag());
                        } else {
                            values.put(AnnotatedCallLog.IS_VOICEMAIL_CALL, 0);
                        }
                    }
                    return null;
                });
    }

    @Override
    public ListenableFuture<Void> onSuccessfulFill() {
        return Futures.immediateFuture(null);
    }

    @Override
    public void registerContentObservers() {
    }

    @Override
    public void unregisterContentObservers() {
    }

    @Override
    public ListenableFuture<Void> clearData() {
        return Futures.immediateFuture(null);
    }

    @Override
    public String getLoggingName() {
        return "VoicemailDataSource";
    }
}
