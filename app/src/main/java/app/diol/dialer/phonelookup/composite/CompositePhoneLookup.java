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

package app.diol.dialer.phonelookup.composite;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.VisibleForTesting;
import android.telecom.Call;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.calllog.CallLogState;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.Annotations.LightweightExecutor;
import app.diol.dialer.common.concurrent.DialerFutures;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.metrics.FutureTimer;
import app.diol.dialer.metrics.FutureTimer.LogCatMode;
import app.diol.dialer.metrics.Metrics;
import app.diol.dialer.phonelookup.PhoneLookup;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.PhoneLookupInfo.Builder;

/**
 * {@link PhoneLookup} which delegates to a configured set of {@link PhoneLookup PhoneLookups},
 * iterating, prioritizing, and coalescing data as necessary.
 *
 * <p>TODO(zachh): Consider renaming and moving this file since it does not implement PhoneLookup.
 */
public final class CompositePhoneLookup {

    private final Context appContext;
    private final ImmutableList<PhoneLookup> phoneLookups;
    private final FutureTimer futureTimer;
    private final CallLogState callLogState;
    private final ListeningExecutorService lightweightExecutorService;

    @VisibleForTesting
    @Inject
    public CompositePhoneLookup(
            @ApplicationContext Context appContext,
            ImmutableList<PhoneLookup> phoneLookups,
            FutureTimer futureTimer,
            CallLogState callLogState,
            @LightweightExecutor ListeningExecutorService lightweightExecutorService) {
        this.appContext = appContext;
        this.phoneLookups = phoneLookups;
        this.futureTimer = futureTimer;
        this.callLogState = callLogState;
        this.lightweightExecutorService = lightweightExecutorService;
    }

    private static String getMostRecentInfoEventName(String loggingName, boolean isBuilt) {
        return String.format(
                !isBuilt
                        ? Metrics.INITIAL_GET_MOST_RECENT_INFO_TEMPLATE
                        : Metrics.GET_MOST_RECENT_INFO_TEMPLATE,
                loggingName);
    }

    private static String onSuccessfulBulkUpdatedEventName(String loggingName, boolean isBuilt) {
        return String.format(
                !isBuilt
                        ? Metrics.INITIAL_ON_SUCCESSFUL_BULK_UPDATE_TEMPLATE
                        : Metrics.ON_SUCCESSFUL_BULK_UPDATE_TEMPLATE,
                loggingName);
    }

    /**
     * Delegates to a set of dependent lookups to build a complete {@link PhoneLookupInfo} for the
     * number associated with the provided call.
     *
     * <p>Note: If any of the dependent lookups fails, the returned future will also fail. If any of
     * the dependent lookups does not complete, the returned future will also not complete.
     */
    public ListenableFuture<PhoneLookupInfo> lookup(Call call) {
        // TODO(zachh): Add short-circuiting logic so that this call is not blocked on low-priority
        // lookups finishing when a higher-priority one has already finished.
        List<ListenableFuture<?>> futures = new ArrayList<>();
        for (PhoneLookup<?> phoneLookup : phoneLookups) {
            ListenableFuture<?> lookupFuture = phoneLookup.lookup(appContext, call);
            String eventName =
                    String.format(Metrics.LOOKUP_FOR_CALL_TEMPLATE, phoneLookup.getLoggingName());
            futureTimer.applyTiming(lookupFuture, eventName);
            futures.add(lookupFuture);
        }
        ListenableFuture<PhoneLookupInfo> combinedFuture = combineSubMessageFutures(futures);
        String eventName = String.format(Metrics.LOOKUP_FOR_CALL_TEMPLATE, getLoggingName());
        futureTimer.applyTiming(combinedFuture, eventName);
        return combinedFuture;
    }

    /**
     * Delegates to a set of dependent lookups to build a complete {@link PhoneLookupInfo} for the
     * provided number.
     *
     * <p>Note: If any of the dependent lookups fails, the returned future will also fail. If any of
     * the dependent lookups does not complete, the returned future will also not complete.
     */
    public ListenableFuture<PhoneLookupInfo> lookup(DialerPhoneNumber dialerPhoneNumber) {
        // TODO(zachh): Add short-circuiting logic so that this call is not blocked on low-priority
        // lookups finishing when a higher-priority one has already finished.
        List<ListenableFuture<?>> futures = new ArrayList<>();
        for (PhoneLookup<?> phoneLookup : phoneLookups) {
            ListenableFuture<?> lookupFuture = phoneLookup.lookup(dialerPhoneNumber);
            String eventName =
                    String.format(Metrics.LOOKUP_FOR_NUMBER_TEMPLATE, phoneLookup.getLoggingName());
            futureTimer.applyTiming(lookupFuture, eventName);
            futures.add(lookupFuture);
        }
        ListenableFuture<PhoneLookupInfo> combinedFuture = combineSubMessageFutures(futures);
        String eventName = String.format(Metrics.LOOKUP_FOR_NUMBER_TEMPLATE, getLoggingName());
        futureTimer.applyTiming(combinedFuture, eventName);
        return combinedFuture;
    }

    /**
     * Combines a list of sub-message futures into a future for {@link PhoneLookupInfo}.
     */
    @SuppressWarnings({"unchecked", "rawtype"})
    private ListenableFuture<PhoneLookupInfo> combineSubMessageFutures(
            List<ListenableFuture<?>> subMessageFutures) {
        return Futures.transform(
                Futures.allAsList(subMessageFutures),
                subMessages -> {
                    Preconditions.checkNotNull(subMessages);
                    Builder mergedInfo = PhoneLookupInfo.newBuilder();
                    for (int i = 0; i < subMessages.size(); i++) {
                        PhoneLookup phoneLookup = phoneLookups.get(i);
                        phoneLookup.setSubMessage(mergedInfo, subMessages.get(i));
                    }
                    return mergedInfo.build();
                },
                lightweightExecutorService);
    }

    /**
     * Delegates to sub-lookups' {@link PhoneLookup#isDirty(ImmutableSet)} completing when the first
     * sub-lookup which returns true completes.
     */
    public ListenableFuture<Boolean> isDirty(ImmutableSet<DialerPhoneNumber> phoneNumbers) {
        List<ListenableFuture<Boolean>> futures = new ArrayList<>();
        for (PhoneLookup<?> phoneLookup : phoneLookups) {
            ListenableFuture<Boolean> isDirtyFuture = phoneLookup.isDirty(phoneNumbers);
            futures.add(isDirtyFuture);
            String eventName = String.format(Metrics.IS_DIRTY_TEMPLATE, phoneLookup.getLoggingName());
            futureTimer.applyTiming(isDirtyFuture, eventName, LogCatMode.LOG_VALUES);
        }
        // Executes all child lookups (possibly in parallel), completing when the first composite lookup
        // which returns "true" completes, and cancels the others.
        ListenableFuture<Boolean> firstMatching =
                DialerFutures.firstMatching(futures, Preconditions::checkNotNull, false /* defaultValue */);
        String eventName = String.format(Metrics.IS_DIRTY_TEMPLATE, getLoggingName());
        futureTimer.applyTiming(firstMatching, eventName, LogCatMode.LOG_VALUES);
        return firstMatching;
    }

    /**
     * Delegates to a set of dependent lookups and combines results.
     *
     * <p>Note: If any of the dependent lookups fails, the returned future will also fail. If any of
     * the dependent lookups does not complete, the returned future will also not complete.
     */
    @SuppressWarnings("unchecked")
    public ListenableFuture<ImmutableMap<DialerPhoneNumber, PhoneLookupInfo>> getMostRecentInfo(
            ImmutableMap<DialerPhoneNumber, PhoneLookupInfo> existingInfoMap) {
        return Futures.transformAsync(
                callLogState.isBuilt(),
                isBuilt -> {
                    Preconditions.checkNotNull(isBuilt);
                    List<ListenableFuture<ImmutableMap<DialerPhoneNumber, ?>>> futures = new ArrayList<>();
                    for (PhoneLookup phoneLookup : phoneLookups) {
                        futures.add(buildSubmapAndGetMostRecentInfo(existingInfoMap, phoneLookup, isBuilt));
                    }
                    ListenableFuture<ImmutableMap<DialerPhoneNumber, PhoneLookupInfo>> combinedFuture =
                            Futures.transform(
                                    Futures.allAsList(futures),
                                    (allMaps) -> {
                                        Preconditions.checkNotNull(allMaps);
                                        ImmutableMap.Builder<DialerPhoneNumber, PhoneLookupInfo> combinedMap =
                                                ImmutableMap.builder();
                                        for (DialerPhoneNumber dialerPhoneNumber : existingInfoMap.keySet()) {
                                            PhoneLookupInfo.Builder combinedInfo = PhoneLookupInfo.newBuilder();
                                            for (int i = 0; i < allMaps.size(); i++) {
                                                ImmutableMap<DialerPhoneNumber, ?> map = allMaps.get(i);
                                                Object subInfo = map.get(dialerPhoneNumber);
                                                if (subInfo == null) {
                                                    throw new IllegalStateException(
                                                            "A sublookup didn't return an info for number: "
                                                                    + LogUtil.sanitizePhoneNumber(
                                                                    dialerPhoneNumber.getNormalizedNumber()));
                                                }
                                                phoneLookups.get(i).setSubMessage(combinedInfo, subInfo);
                                            }
                                            combinedMap.put(dialerPhoneNumber, combinedInfo.build());
                                        }
                                        return combinedMap.build();
                                    },
                                    lightweightExecutorService);
                    String eventName = getMostRecentInfoEventName(getLoggingName(), isBuilt);
                    futureTimer.applyTiming(combinedFuture, eventName);
                    return combinedFuture;
                },
                MoreExecutors.directExecutor());
    }

    private <T> ListenableFuture<ImmutableMap<DialerPhoneNumber, T>> buildSubmapAndGetMostRecentInfo(
            ImmutableMap<DialerPhoneNumber, PhoneLookupInfo> existingInfoMap,
            PhoneLookup<T> phoneLookup,
            boolean isBuilt) {
        Map<DialerPhoneNumber, T> submap =
                Maps.transformEntries(
                        existingInfoMap,
                        (dialerPhoneNumber, phoneLookupInfo) ->
                                phoneLookup.getSubMessage(existingInfoMap.get(dialerPhoneNumber)));
        ListenableFuture<ImmutableMap<DialerPhoneNumber, T>> mostRecentInfoFuture =
                phoneLookup.getMostRecentInfo(ImmutableMap.copyOf(submap));
        String eventName = getMostRecentInfoEventName(phoneLookup.getLoggingName(), isBuilt);
        futureTimer.applyTiming(mostRecentInfoFuture, eventName);
        return mostRecentInfoFuture;
    }

    /**
     * Delegates to sub-lookups' {@link PhoneLookup#onSuccessfulBulkUpdate()}.
     */
    public ListenableFuture<Void> onSuccessfulBulkUpdate() {
        return Futures.transformAsync(
                callLogState.isBuilt(),
                isBuilt -> {
                    Preconditions.checkNotNull(isBuilt);
                    List<ListenableFuture<Void>> futures = new ArrayList<>();
                    for (PhoneLookup<?> phoneLookup : phoneLookups) {
                        ListenableFuture<Void> phoneLookupFuture = phoneLookup.onSuccessfulBulkUpdate();
                        futures.add(phoneLookupFuture);
                        String eventName =
                                onSuccessfulBulkUpdatedEventName(phoneLookup.getLoggingName(), isBuilt);
                        futureTimer.applyTiming(phoneLookupFuture, eventName);
                    }
                    ListenableFuture<Void> combinedFuture =
                            Futures.transform(
                                    Futures.allAsList(futures), unused -> null, lightweightExecutorService);
                    String eventName = onSuccessfulBulkUpdatedEventName(getLoggingName(), isBuilt);
                    futureTimer.applyTiming(combinedFuture, eventName);
                    return combinedFuture;
                },
                MoreExecutors.directExecutor());
    }

    /**
     * Delegates to sub-lookups' {@link PhoneLookup#registerContentObservers()}.
     */
    @MainThread
    public void registerContentObservers() {
        for (PhoneLookup phoneLookup : phoneLookups) {
            phoneLookup.registerContentObservers();
        }
    }

    /**
     * Delegates to sub-lookups' {@link PhoneLookup#unregisterContentObservers()}.
     */
    @MainThread
    public void unregisterContentObservers() {
        for (PhoneLookup phoneLookup : phoneLookups) {
            phoneLookup.unregisterContentObservers();
        }
    }

    /**
     * Delegates to sub-lookups' {@link PhoneLookup#clearData()}.
     */
    public ListenableFuture<Void> clearData() {
        List<ListenableFuture<Void>> futures = new ArrayList<>();
        for (PhoneLookup<?> phoneLookup : phoneLookups) {
            ListenableFuture<Void> phoneLookupFuture = phoneLookup.clearData();
            futures.add(phoneLookupFuture);
        }
        return Futures.transform(
                Futures.allAsList(futures), unused -> null, lightweightExecutorService);
    }

    private String getLoggingName() {
        return "CompositePhoneLookup";
    }
}
