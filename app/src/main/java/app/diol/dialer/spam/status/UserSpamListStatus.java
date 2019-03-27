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

package app.diol.dialer.spam.status;

import android.support.annotation.IntDef;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A value class representing a number's spam status in the user spam list.
 */
@AutoValue
@SuppressWarnings("Guava")
public abstract class UserSpamListStatus {

    public static UserSpamListStatus notOnList() {
        return new AutoValue_UserSpamListStatus(Status.NOT_ON_LIST, Optional.absent());
    }

    public static UserSpamListStatus whitelisted(long timestampMillis) {
        return new AutoValue_UserSpamListStatus(Status.WHITELISTED, Optional.of(timestampMillis));
    }

    public static UserSpamListStatus blacklisted(long timestampMillis) {
        return new AutoValue_UserSpamListStatus(Status.BLACKLISTED, Optional.of(timestampMillis));
    }

    public abstract @Status
    int getStatus();

    /**
     * Returns the timestamp (in milliseconds) representing when a number's spam status was put on the
     * list, or {@code Optional.absent()} if the number is not on the list.
     */
    public abstract Optional<Long> getTimestampMillis();

    /**
     * Integers representing the spam status in the user spam list.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.NOT_ON_LIST, Status.WHITELISTED, Status.BLACKLISTED})
    public @interface Status {
        int NOT_ON_LIST = 1;
        int WHITELISTED = 2;
        int BLACKLISTED = 3;
    }
}
