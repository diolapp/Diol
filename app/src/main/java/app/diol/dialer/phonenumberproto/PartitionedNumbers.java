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

package app.diol.dialer.phonenumberproto;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.ArraySet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.common.Assert;

/**
 * Divides a set of {@link DialerPhoneNumber DialerPhoneNumbers} according to those that are valid
 * according to libphonenumber, and those that are not.
 *
 * <p>Numbers with post-dial portions are always considered invalid as most systems store E164
 * numbers which do not support post-dial portions.
 */
public final class PartitionedNumbers {
    private final ImmutableMap<String, ImmutableSet<DialerPhoneNumber>>
            e164NumbersToDialerPhoneNumbers;
    private final ImmutableMap<String, ImmutableSet<DialerPhoneNumber>>
            invalidNumbersToDialerPhoneNumbers;

    /**
     * Divides a set of {@link DialerPhoneNumber DialerPhoneNumbers} according to those that are valid
     * according to libphonenumber, and those that are not.
     *
     * <p>Numbers with post-dial portions are always considered invalid as most systems store E164
     * numbers which do not support post-dial portions.
     */
    @WorkerThread
    public PartitionedNumbers(@NonNull ImmutableSet<DialerPhoneNumber> dialerPhoneNumbers) {
        Assert.isWorkerThread();
        Map<String, Set<DialerPhoneNumber>> e164MapBuilder = new ArrayMap<>();
        Map<String, Set<DialerPhoneNumber>> invalidMapBuilder = new ArrayMap<>();

        for (DialerPhoneNumber dialerPhoneNumber : dialerPhoneNumbers) {
            /*
             * Numbers with post-dial digits are considered valid and can be converted to E164, but their
             * post dial digits are lost in the process. For example, the normalized version of a number
             * with a post-dial portion in the contacts database is stored without the post-dial portion.
             */
            if (dialerPhoneNumber.getIsValid() && dialerPhoneNumber.getPostDialPortion().isEmpty()) {
                String validE164 = dialerPhoneNumber.getNormalizedNumber();
                Set<DialerPhoneNumber> currentNumbers = e164MapBuilder.get(validE164);
                if (currentNumbers == null) {
                    currentNumbers = new ArraySet<>();
                    e164MapBuilder.put(validE164, currentNumbers);
                }
                currentNumbers.add(dialerPhoneNumber);
            } else {
                String invalidNumber = dialerPhoneNumber.getNormalizedNumber();
                Set<DialerPhoneNumber> currentNumbers = invalidMapBuilder.get(invalidNumber);
                if (currentNumbers == null) {
                    currentNumbers = new ArraySet<>();
                    invalidMapBuilder.put(invalidNumber, currentNumbers);
                }
                currentNumbers.add(dialerPhoneNumber);
            }
        }

        e164NumbersToDialerPhoneNumbers = makeImmutable(e164MapBuilder);
        invalidNumbersToDialerPhoneNumbers = makeImmutable(invalidMapBuilder);
    }

    private static <K, V> ImmutableMap<K, ImmutableSet<V>> makeImmutable(
            Map<K, Set<V>> mutableMapOfSet) {
        ImmutableMap.Builder<K, ImmutableSet<V>> mapBuilder = ImmutableMap.builder();
        for (Map.Entry<K, Set<V>> entry : mutableMapOfSet.entrySet()) {
            mapBuilder.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
        }
        return mapBuilder.build();
    }

    /**
     * Returns the set of invalid numbers from the original DialerPhoneNumbers
     */
    @NonNull
    public ImmutableSet<String> invalidNumbers() {
        return invalidNumbersToDialerPhoneNumbers.keySet();
    }

    /**
     * Returns the set of valid, E164 formatted numbers from the original DialerPhoneNumbers
     */
    @NonNull
    public ImmutableSet<String> validE164Numbers() {
        return e164NumbersToDialerPhoneNumbers.keySet();
    }

    /**
     * Returns the corresponding set of original DialerPhoneNumbers that map to the valid E164 number
     * from {@link #validE164Numbers()}.
     *
     * @throws NullPointerException if there are no numbers found
     */
    @NonNull
    public ImmutableSet<DialerPhoneNumber> dialerPhoneNumbersForValidE164(String validE164) {
        return Assert.isNotNull(e164NumbersToDialerPhoneNumbers.get(validE164));
    }

    /**
     * Returns the corresponding set of original DialerPhoneNumbers that map to the invalid number
     * from {@link #invalidNumbers()}.
     *
     * @throws NullPointerException if there are no numbers found
     */
    @NonNull
    public ImmutableSet<DialerPhoneNumber> dialerPhoneNumbersForInvalid(String invalidNumber) {
        return Assert.isNotNull(invalidNumbersToDialerPhoneNumbers.get(invalidNumber));
    }
}
