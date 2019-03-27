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

package app.diol.dialer.activecalls;

import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

/**
 * Info of an active call
 */
@AutoValue
@SuppressWarnings("Guava")
public abstract class ActiveCallInfo {

    public static Builder builder() {
        return new AutoValue_ActiveCallInfo.Builder();
    }

    /**
     * The {@link PhoneAccountHandle} the call is made with
     */
    public abstract Optional<PhoneAccountHandle> phoneAccountHandle();

    /**
     * Builder for {@link ActiveCallInfo}. Only In Call UI should create ActiveCallInfo
     */
    @AutoValue.Builder
    public abstract static class Builder {

        public Builder setPhoneAccountHandle(@Nullable PhoneAccountHandle phoneAccountHandle) {
            return setPhoneAccountHandle(Optional.fromNullable(phoneAccountHandle));
        }

        public abstract Builder setPhoneAccountHandle(Optional<PhoneAccountHandle> phoneAccountHandle);

        public abstract ActiveCallInfo build();
    }
}
