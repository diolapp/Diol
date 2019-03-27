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

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

/**
 * Holds a boolean and long to represent spam status.
 */
@AutoValue
public abstract class SimpleSpamStatus implements SpamStatus {

    /**
     * Returns a SimpleSpamStatus with the given boolean and timestamp.
     */
    public static SimpleSpamStatus create(boolean isSpam, @Nullable Long timestampMillis) {
        return builder()
                .setSpam(isSpam)
                .setTimestampMillis(timestampMillis)
                .setSpamMetadata(SpamMetadata.empty())
                .build();
    }

    /**
     * Returns a SimpleSpamStatus that's not marked as spam and has no timestamp.
     */
    public static SimpleSpamStatus notSpam() {
        return create(false, null);
    }

    public static Builder builder() {
        return new AutoValue_SimpleSpamStatus.Builder();
    }

    /**
     * Creates instances of SimpleSpamStatus.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setSpam(boolean isSpam);

        abstract Builder setTimestampMillis(Optional<Long> timestamp);

        public Builder setTimestampMillis(@Nullable Long timestampMillis) {
            return setTimestampMillis(Optional.fromNullable(timestampMillis));
        }

        public abstract Builder setSpamMetadata(SpamMetadata spamMetadata);

        public abstract SimpleSpamStatus build();
    }
}
