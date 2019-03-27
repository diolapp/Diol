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

package app.diol.dialer.app.contactinfo;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import app.diol.dialer.phonenumbercache.ContactInfo;

/**
 * A request for contact details for the given number, used by the ContactInfoCache.
 */
public final class ContactInfoRequest implements Comparable<ContactInfoRequest> {

    public static final int TYPE_LOCAL = 0;
    /**
     * If cannot find the contact locally, do remote lookup later.
     */
    public static final int TYPE_LOCAL_AND_REMOTE = 1;
    public static final int TYPE_REMOTE = 2;
    private static final AtomicLong NEXT_SEQUENCE_NUMBER = new AtomicLong(0);
    /**
     * The number to look-up.
     */
    public final String number;
    /**
     * The country in which a call to or from this number was placed or received.
     */
    public final String countryIso;
    /**
     * The cached contact information stored in the call log.
     */
    public final ContactInfo callLogInfo;
    /**
     * Is the request a remote lookup. Remote requests are treated as lower priority.
     */
    @TYPE
    public final int type;
    private final long sequenceNumber;

    public ContactInfoRequest(
            String number, String countryIso, ContactInfo callLogInfo, @TYPE int type) {
        this.sequenceNumber = NEXT_SEQUENCE_NUMBER.getAndIncrement();
        this.number = number;
        this.countryIso = countryIso;
        this.callLogInfo = callLogInfo;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ContactInfoRequest)) {
            return false;
        }

        ContactInfoRequest other = (ContactInfoRequest) obj;

        if (!TextUtils.equals(number, other.number)) {
            return false;
        }
        if (!TextUtils.equals(countryIso, other.countryIso)) {
            return false;
        }
        if (!Objects.equals(callLogInfo, other.callLogInfo)) {
            return false;
        }

        if (type != other.type) {
            return false;
        }

        return true;
    }

    public boolean isLocalRequest() {
        return type == TYPE_LOCAL || type == TYPE_LOCAL_AND_REMOTE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNumber, number, countryIso, callLogInfo, type);
    }

    @Override
    public int compareTo(ContactInfoRequest other) {
        // Local query always comes first.
        if (isLocalRequest() && !other.isLocalRequest()) {
            return -1;
        }
        if (!isLocalRequest() && other.isLocalRequest()) {
            return 1;
        }
        // First come first served.
        return sequenceNumber < other.sequenceNumber ? -1 : 1;
    }

    /**
     * Specifies the type of the request is.
     */
    @IntDef(
            value = {
                    TYPE_LOCAL,
                    TYPE_LOCAL_AND_REMOTE,
                    TYPE_REMOTE,
            }
    )
    @Retention(RetentionPolicy.SOURCE)
    public @interface TYPE {
    }
}
