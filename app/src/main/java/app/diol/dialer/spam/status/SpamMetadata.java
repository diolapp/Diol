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

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

/**
 * Holds information which can be used to determine a number's spam status.
 *
 * @see SpamStatus
 */
@AutoValue
public abstract class SpamMetadata {

    /**
     * Returns an empty spam metadata, no optional data is set.
     */
    public static SpamMetadata empty() {
        return builder().build();
    }

    public static SpamMetadata.Builder builder() {
        return new AutoValue_SpamMetadata.Builder();
    }

    public abstract Optional<GlobalSpamListStatus> globalSpamListStatus();

    public abstract Optional<UserSpamListStatus> userSpamListStatus();

    /**
     * Creates instances of SpamMetadata.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setGlobalSpamListStatus(GlobalSpamListStatus globalSpamListStatus);

        public abstract Builder setUserSpamListStatus(UserSpamListStatus userSpamListStatus);

        public abstract SpamMetadata build();
    }
}
