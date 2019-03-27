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

package app.diol.dialer.phonelookup.spam;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Map.Entry;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.common.concurrent.Annotations.LightweightExecutor;
import app.diol.dialer.phonelookup.PhoneLookup;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.PhoneLookupInfo.SpamInfo;
import app.diol.dialer.spam.Spam;
import app.diol.dialer.spam.status.SpamStatus;
import app.diol.dialer.storage.Unencrypted;

/**
 * PhoneLookup implementation for Spam info.
 */
public final class SpamPhoneLookup implements PhoneLookup<SpamInfo> {

    @VisibleForTesting
    static final String PREF_LAST_TIMESTAMP_PROCESSED = "spamPhoneLookupLastTimestampProcessed";

    private final ListeningExecutorService lightweightExecutorService;
    private final ListeningExecutorService backgroundExecutorService;
    private final SharedPreferences sharedPreferences;
    private final Spam spam;

    @Nullable
    private Long currentLastTimestampProcessed;

    @Inject
    SpamPhoneLookup(
            @BackgroundExecutor ListeningExecutorService backgroundExecutorService,
            @LightweightExecutor ListeningExecutorService lightweightExecutorService,
            @Unencrypted SharedPreferences sharedPreferences,
            Spam spam) {
        this.backgroundExecutorService = backgroundExecutorService;
        this.lightweightExecutorService = lightweightExecutorService;
        this.sharedPreferences = sharedPreferences;
        this.spam = spam;
    }

    @Override
    public ListenableFuture<SpamInfo> lookup(DialerPhoneNumber dialerPhoneNumber) {
        return Futures.transform(
                spam.batchCheckSpamStatus(ImmutableSet.of(dialerPhoneNumber)),
                spamStatusMap ->
                        SpamInfo.newBuilder()
                                .setIsSpam(Assert.isNotNull(spamStatusMap.get(dialerPhoneNumber)).isSpam())
                                .build(),
                lightweightExecutorService);
    }

    @Override
    public ListenableFuture<Boolean> isDirty(ImmutableSet<DialerPhoneNumber> phoneNumbers) {
        ListenableFuture<Long> lastTimestampProcessedFuture =
                backgroundExecutorService.submit(
                        () -> sharedPreferences.getLong(PREF_LAST_TIMESTAMP_PROCESSED, 0L));

        return Futures.transformAsync(
                lastTimestampProcessedFuture, spam::dataUpdatedSince, lightweightExecutorService);
    }

    @Override
    public ListenableFuture<ImmutableMap<DialerPhoneNumber, SpamInfo>> getMostRecentInfo(
            ImmutableMap<DialerPhoneNumber, SpamInfo> existingInfoMap) {
        currentLastTimestampProcessed = null;

        ListenableFuture<ImmutableMap<DialerPhoneNumber, SpamStatus>> spamStatusMapFuture =
                spam.batchCheckSpamStatus(existingInfoMap.keySet());

        return Futures.transform(
                spamStatusMapFuture,
                spamStatusMap -> {
                    ImmutableMap.Builder<DialerPhoneNumber, SpamInfo> mostRecentSpamInfo =
                            new ImmutableMap.Builder<>();

                    for (Entry<DialerPhoneNumber, SpamStatus> dialerPhoneNumberAndSpamStatus :
                            spamStatusMap.entrySet()) {
                        DialerPhoneNumber dialerPhoneNumber = dialerPhoneNumberAndSpamStatus.getKey();
                        SpamStatus spamStatus = dialerPhoneNumberAndSpamStatus.getValue();
                        mostRecentSpamInfo.put(
                                dialerPhoneNumber, SpamInfo.newBuilder().setIsSpam(spamStatus.isSpam()).build());

                        Optional<Long> timestampMillis = spamStatus.getTimestampMillis();
                        if (timestampMillis.isPresent()) {
                            currentLastTimestampProcessed =
                                    currentLastTimestampProcessed == null
                                            ? timestampMillis.get()
                                            : Math.max(timestampMillis.get(), currentLastTimestampProcessed);
                        }
                    }

                    // If currentLastTimestampProcessed is null, it means none of the numbers in
                    // existingInfoMap has spam status in the underlying data source.
                    // We should set currentLastTimestampProcessed to the current timestamp to avoid
                    // triggering the bulk update flow repeatedly.
                    if (currentLastTimestampProcessed == null) {
                        currentLastTimestampProcessed = System.currentTimeMillis();
                    }

                    return mostRecentSpamInfo.build();
                },
                lightweightExecutorService);
    }

    @Override
    public SpamInfo getSubMessage(PhoneLookupInfo phoneLookupInfo) {
        return phoneLookupInfo.getSpamInfo();
    }

    @Override
    public void setSubMessage(PhoneLookupInfo.Builder destination, SpamInfo subMessage) {
        destination.setSpamInfo(subMessage);
    }

    @Override
    public ListenableFuture<Void> onSuccessfulBulkUpdate() {
        return backgroundExecutorService.submit(
                () -> {
                    sharedPreferences
                            .edit()
                            .putLong(
                                    PREF_LAST_TIMESTAMP_PROCESSED, Assert.isNotNull(currentLastTimestampProcessed))
                            .apply();
                    return null;
                });
    }

    @Override
    public void registerContentObservers() {
        // No content observer can be registered as Spam is not based on a content provider.
        // Each Spam implementation should be responsible for notifying any data changes.
    }

    @Override
    public void unregisterContentObservers() {
    }

    @Override
    public ListenableFuture<Void> clearData() {
        return backgroundExecutorService.submit(
                () -> {
                    sharedPreferences.edit().remove(PREF_LAST_TIMESTAMP_PROCESSED).apply();
                    return null;
                });
    }

    @Override
    public String getLoggingName() {
        return "SpamPhoneLookup";
    }
}
