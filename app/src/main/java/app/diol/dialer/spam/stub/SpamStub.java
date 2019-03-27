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

package app.diol.dialer.spam.stub;

import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.logging.ContactLookupResult;
import app.diol.dialer.logging.ContactSource;
import app.diol.dialer.logging.ReportingLocation;
import app.diol.dialer.spam.Spam;
import app.diol.dialer.spam.status.SimpleSpamStatus;
import app.diol.dialer.spam.status.SpamStatus;

/**
 * Default implementation of Spam.
 */
public class SpamStub implements Spam {

    private final ListeningExecutorService backgroundExecutorService;

    @Inject
    public SpamStub(@BackgroundExecutor ListeningExecutorService backgroundExecutorService) {
        this.backgroundExecutorService = backgroundExecutorService;
    }

    @Override
    public ListenableFuture<ImmutableMap<DialerPhoneNumber, SpamStatus>> batchCheckSpamStatus(
            ImmutableSet<DialerPhoneNumber> dialerPhoneNumbers) {
        return backgroundExecutorService.submit(
                () -> {
                    ImmutableMap.Builder<DialerPhoneNumber, SpamStatus> resultBuilder =
                            new ImmutableMap.Builder<>();
                    for (DialerPhoneNumber dialerPhoneNumber : dialerPhoneNumbers) {
                        resultBuilder.put(dialerPhoneNumber, SimpleSpamStatus.notSpam());
                    }
                    return resultBuilder.build();
                });
    }

    @Override
    public ListenableFuture<SpamStatus> checkSpamStatus(DialerPhoneNumber dialerPhoneNumber) {
        return Futures.immediateFuture(SimpleSpamStatus.notSpam());
    }

    @Override
    public ListenableFuture<SpamStatus> checkSpamStatus(
            String number, @Nullable String defaultCountryIso) {
        return Futures.immediateFuture(SimpleSpamStatus.notSpam());
    }

    @Override
    public ListenableFuture<Void> updateSpamListDownload(boolean isEnabledByUser) {
        // no-op
        return Futures.immediateFuture(null);
    }

    @Override
    public boolean checkSpamStatusSynchronous(String number, String countryIso) {
        return false;
    }

    @Override
    public ListenableFuture<Boolean> dataUpdatedSince(long timestampMillis) {
        return Futures.immediateFuture(false);
    }

    @Override
    public void reportSpamFromAfterCallNotification(
            String number,
            String countryIso,
            int callType,
            ReportingLocation.Type from,
            ContactLookupResult.Type contactLookupResultType) {
    }

    @Override
    public void reportSpamFromCallHistory(
            String number,
            String countryIso,
            int callType,
            ReportingLocation.Type from,
            ContactSource.Type contactSourceType) {
    }

    @Override
    public void reportNotSpamFromAfterCallNotification(
            String number,
            String countryIso,
            int callType,
            ReportingLocation.Type from,
            ContactLookupResult.Type contactLookupResultType) {
    }

    @Override
    public void reportNotSpamFromCallHistory(
            String number,
            String countryIso,
            int callType,
            ReportingLocation.Type from,
            ContactSource.Type contactSourceType) {
    }
}
