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

package app.diol.dialer.phonelookup.emergency;

import android.content.Context;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.phonelookup.PhoneLookup;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.PhoneLookupInfo.EmergencyInfo;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;

/**
 * PhoneLookup implementation for checking if a number is an emergency number.
 *
 * <p>The check has to be done in a PhoneLookup as it involves detecting the user's location and
 * obtaining SIM info, which are expensive operations. Doing it in the main thread will make the UI
 * super janky.
 */
public class EmergencyPhoneLookup implements PhoneLookup<EmergencyInfo> {

    private final Context appContext;
    private final ListeningExecutorService backgroundExecutorService;

    @Inject
    EmergencyPhoneLookup(
            @ApplicationContext Context appContext,
            @BackgroundExecutor ListeningExecutorService backgroundExecutorService) {
        this.appContext = appContext;
        this.backgroundExecutorService = backgroundExecutorService;
    }

    @Override
    public ListenableFuture<EmergencyInfo> lookup(DialerPhoneNumber dialerPhoneNumber) {
        return backgroundExecutorService.submit(
                () ->
                        EmergencyInfo.newBuilder()
                                .setIsEmergencyNumber(
                                        PhoneNumberHelper.isLocalEmergencyNumber(
                                                appContext, dialerPhoneNumber.getNormalizedNumber()))
                                .build());
    }

    @Override
    public ListenableFuture<Boolean> isDirty(ImmutableSet<DialerPhoneNumber> phoneNumbers) {
        return Futures.immediateFuture(false);
    }

    @Override
    public ListenableFuture<ImmutableMap<DialerPhoneNumber, EmergencyInfo>> getMostRecentInfo(
            ImmutableMap<DialerPhoneNumber, EmergencyInfo> existingInfoMap) {
        // We can update EmergencyInfo for all numbers in the provided map, but the negative impact on
        // performance is intolerable as checking a single number involves detecting the user's location
        // and obtaining SIM info, which will take more than 100ms (see
        // android.telephony.PhoneNumberUtils#isLocalEmergencyNumber(Context, int, String) for details).
        //
        // As emergency numbers won't change in a country, the only case we will miss is that
        //   (1) a number is an emergency number in country A but not in country B,
        //   (2) a user has an emergency call entry when they are in country A, and
        //   (3) they travel from A to B,
        // which is a rare event.
        //
        // We can update the implementation if telecom supports batch check in the future.
        return Futures.immediateFuture(existingInfoMap);
    }

    @Override
    public void setSubMessage(PhoneLookupInfo.Builder destination, EmergencyInfo subMessage) {
        destination.setEmergencyInfo(subMessage);
    }

    @Override
    public EmergencyInfo getSubMessage(PhoneLookupInfo phoneLookupInfo) {
        return phoneLookupInfo.getEmergencyInfo();
    }

    @Override
    public ListenableFuture<Void> onSuccessfulBulkUpdate() {
        return Futures.immediateFuture(null);
    }

    @Override
    public void registerContentObservers() {
        // No content observer to register.
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
        return "EmergencyPhoneLookup";
    }
}
