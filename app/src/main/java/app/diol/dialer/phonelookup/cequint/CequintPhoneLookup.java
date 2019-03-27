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

package app.diol.dialer.phonelookup.cequint;

import android.content.Context;
import android.telecom.Call;
import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.common.concurrent.Annotations.LightweightExecutor;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.location.GeoUtil;
import app.diol.dialer.oem.CequintCallerIdManager;
import app.diol.dialer.oem.CequintCallerIdManager.CequintCallerIdContact;
import app.diol.dialer.phonelookup.PhoneLookup;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.PhoneLookupInfo.CequintInfo;
import app.diol.dialer.phonenumberproto.DialerPhoneNumberUtil;
import app.diol.dialer.telecom.TelecomCallUtil;

/**
 * PhoneLookup implementation for Cequint.
 */
public class CequintPhoneLookup implements PhoneLookup<CequintInfo> {

    private final Context appContext;
    private final ListeningExecutorService backgroundExecutorService;
    private final ListeningExecutorService lightweightExecutorService;

    @Inject
    CequintPhoneLookup(
            @ApplicationContext Context appContext,
            @BackgroundExecutor ListeningExecutorService backgroundExecutorService,
            @LightweightExecutor ListeningExecutorService lightweightExecutorService) {
        this.appContext = appContext;
        this.backgroundExecutorService = backgroundExecutorService;
        this.lightweightExecutorService = lightweightExecutorService;
    }

    /**
     * Builds a {@link CequintInfo} proto based on the given {@link CequintCallerIdContact} returned
     * by {@link CequintCallerIdManager}.
     */
    private static CequintInfo buildCequintInfo(CequintCallerIdContact cequintCallerIdContact) {
        CequintInfo.Builder cequintInfoBuilder = CequintInfo.newBuilder();

        // Every field in CequintCallerIdContact can be null.
        if (!TextUtils.isEmpty(cequintCallerIdContact.name())) {
            cequintInfoBuilder.setName(cequintCallerIdContact.name());
        }
        if (!TextUtils.isEmpty(cequintCallerIdContact.geolocation())) {
            cequintInfoBuilder.setGeolocation(cequintCallerIdContact.geolocation());
        }
        if (!TextUtils.isEmpty(cequintCallerIdContact.photoUri())) {
            cequintInfoBuilder.setPhotoUri(cequintCallerIdContact.photoUri());
        }

        return cequintInfoBuilder.build();
    }

    @Override
    public ListenableFuture<CequintInfo> lookup(Context appContext, Call call) {
        if (!CequintCallerIdManager.isCequintCallerIdEnabled(appContext)) {
            return Futures.immediateFuture(CequintInfo.getDefaultInstance());
        }

        ListenableFuture<DialerPhoneNumber> dialerPhoneNumberFuture =
                backgroundExecutorService.submit(
                        () -> {
                            DialerPhoneNumberUtil dialerPhoneNumberUtil = new DialerPhoneNumberUtil();
                            return dialerPhoneNumberUtil.parse(
                                    TelecomCallUtil.getNumber(call), GeoUtil.getCurrentCountryIso(appContext));
                        });
        String callerDisplayName = call.getDetails().getCallerDisplayName();
        boolean isIncomingCall = (call.getState() == Call.STATE_RINGING);

        return Futures.transformAsync(
                dialerPhoneNumberFuture,
                dialerPhoneNumber ->
                        backgroundExecutorService.submit(
                                () ->
                                        buildCequintInfo(
                                                CequintCallerIdManager.getCequintCallerIdContactForCall(
                                                        appContext,
                                                        Assert.isNotNull(dialerPhoneNumber).getNormalizedNumber(),
                                                        callerDisplayName,
                                                        isIncomingCall))),
                lightweightExecutorService);
    }

    @Override
    public ListenableFuture<CequintInfo> lookup(DialerPhoneNumber dialerPhoneNumber) {
        if (!CequintCallerIdManager.isCequintCallerIdEnabled(appContext)) {
            return Futures.immediateFuture(CequintInfo.getDefaultInstance());
        }

        return backgroundExecutorService.submit(
                () ->
                        buildCequintInfo(
                                CequintCallerIdManager.getCequintCallerIdContactForNumber(
                                        appContext, dialerPhoneNumber.getNormalizedNumber())));
    }

    @Override
    public ListenableFuture<Boolean> isDirty(ImmutableSet<DialerPhoneNumber> phoneNumbers) {
        return Futures.immediateFuture(false);
    }

    @Override
    public ListenableFuture<ImmutableMap<DialerPhoneNumber, CequintInfo>> getMostRecentInfo(
            ImmutableMap<DialerPhoneNumber, CequintInfo> existingInfoMap) {
        return Futures.immediateFuture(existingInfoMap);
    }

    @Override
    public void setSubMessage(PhoneLookupInfo.Builder destination, CequintInfo subMessage) {
        destination.setCequintInfo(subMessage);
    }

    @Override
    public CequintInfo getSubMessage(PhoneLookupInfo phoneLookupInfo) {
        return phoneLookupInfo.getCequintInfo();
    }

    @Override
    public ListenableFuture<Void> onSuccessfulBulkUpdate() {
        return Futures.immediateFuture(null);
    }

    @Override
    public void registerContentObservers() {
        // No need to register a content observer as the Cequint content provider doesn't support batch
        // queries.
    }

    @Override
    public void unregisterContentObservers() {
        // Nothing to be done as no content observer is registered.
    }

    @Override
    public ListenableFuture<Void> clearData() {
        return Futures.immediateFuture(null);
    }

    @Override
    public String getLoggingName() {
        return "CequintPhoneLookup";
    }
}
