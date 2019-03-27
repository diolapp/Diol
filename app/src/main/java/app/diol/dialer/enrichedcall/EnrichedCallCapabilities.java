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

package app.diol.dialer.enrichedcall;

import com.google.auto.value.AutoValue;

/**
 * Value type holding enriched call capabilities.
 */
@AutoValue
public abstract class EnrichedCallCapabilities {

    public static final EnrichedCallCapabilities NO_CAPABILITIES = builder().build();

    public static final EnrichedCallCapabilities ALL_CAPABILITIES =
            builder()
                    .setCallComposerCapable(true)
                    .setPostCallCapable(true)
                    .setVideoShareCapable(true)
                    .build();

    /**
     * Creates an instance of {@link Builder}.
     *
     * <p>Unless otherwise set, all fields will default to false.
     */
    public static Builder builder() {
        return new AutoValue_EnrichedCallCapabilities.Builder()
                .setCallComposerCapable(false)
                .setPostCallCapable(false)
                .setVideoShareCapable(false)
                .setTemporarilyUnavailable(false);
    }

    public abstract boolean isCallComposerCapable();

    public abstract boolean isPostCallCapable();

    public abstract boolean isVideoShareCapable();

    public abstract Builder toBuilder();

    /**
     * Returns {@code true} if these capabilities represent those of a user that is temporarily
     * unavailable. This is an indication that capabilities should be refreshed.
     */
    public abstract boolean isTemporarilyUnavailable();

    /**
     * Creates instances of {@link EnrichedCallCapabilities}.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setCallComposerCapable(boolean isCapable);

        public abstract Builder setPostCallCapable(boolean isCapable);

        public abstract Builder setVideoShareCapable(boolean isCapable);

        public abstract Builder setTemporarilyUnavailable(boolean temporarilyUnavailable);

        public abstract EnrichedCallCapabilities build();
    }
}
