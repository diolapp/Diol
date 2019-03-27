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

package app.diol.dialer.speeddial.database;

import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * POJO representation of database rows returned by {@link SpeedDialEntryDao}.
 */
@AutoValue
public abstract class SpeedDialEntry {

    public static Builder builder() {
        return new AutoValue_SpeedDialEntry.Builder().setPinnedPosition(Optional.absent());
    }

    /**
     * Unique ID
     *
     * <p>Must be null when inserting, and an ID will be generated and returned after inserting.
     */
    @Nullable
    public abstract Long id();

    /**
     * Position the contact is pinned to in the UI. Will be absent if it hasn't be set yet.
     */
    public abstract Optional<Integer> pinnedPosition();

    /**
     * @see {@link Contacts#_ID}
     */
    public abstract long contactId();

    /**
     * @see {@link Contacts#LOOKUP_KEY}
     */
    public abstract String lookupKey();

    /**
     * {@link Channel} that is associated with this entry.
     *
     * <p>Contacts with multiple channels do not have a default until specified by the user. Once the
     * default channel is determined, all calls should be placed to this channel.
     */
    @Nullable
    public abstract Channel defaultChannel();

    public abstract Builder toBuilder();

    /**
     * Builder class for speed dial entry.
     */
    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setId(Long id);

        public abstract Builder setPinnedPosition(Optional<Integer> pinnedPosition);

        public abstract Builder setContactId(long contactId);

        public abstract Builder setLookupKey(String lookupKey);

        public abstract Builder setDefaultChannel(@Nullable Channel defaultChannel);

        public abstract SpeedDialEntry build();
    }

    /**
     * POJO representation of a relevant phone number columns in {@link SpeedDialEntryDao}.
     */
    @AutoValue
    public abstract static class Channel {

        public static final int UNKNOWN = 0;
        public static final int VOICE = 1;
        public static final int IMS_VIDEO = 2;
        public static final int DUO = 3;

        public static Builder builder() {
            return new AutoValue_SpeedDialEntry_Channel.Builder();
        }

        public boolean isVideoTechnology() {
            return technology() == IMS_VIDEO || technology() == DUO;
        }

        /**
         * Raw phone number as the user entered it.
         *
         * @see Phone#NUMBER
         */
        public abstract String number();

        /**
         * @see Phone#TYPE
         */
        public abstract int phoneType();

        /**
         * @see Phone#LABEL
         */
        public abstract String label();

        public abstract @Technology
        int technology();

        public abstract Builder toBuilder();

        /**
         * Whether the Channel is for an audio or video call.
         */
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({UNKNOWN, VOICE, IMS_VIDEO, DUO})
        public @interface Technology {
        }

        /**
         * Builder class for {@link Channel}.
         */
        @AutoValue.Builder
        public abstract static class Builder {

            public abstract Builder setNumber(String number);

            public abstract Builder setPhoneType(int phoneType);

            public abstract Builder setLabel(String label);

            public abstract Builder setTechnology(@Technology int technology);

            public abstract Channel build();
        }
    }
}
