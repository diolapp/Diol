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

package app.diol.voicemail.impl;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

/**
 * Matches a {@link CarrierIdentifier}. Full equality check on CarrierIdentifiers is often unfit
 * because non-MVNO carriers usually just specify the {@link CarrierIdentifier#mccMnc()} while their
 * {@link CarrierIdentifier#gid1()} could be anything. This matcher ignore fields that are not
 * specified in the matcher.
 */
@AutoValue
public abstract class CarrierIdentifierMatcher {

    public static Builder builder() {
        return new AutoValue_CarrierIdentifierMatcher.Builder();
    }

    public abstract String mccMnc();

    public abstract Optional<String> gid1();

    public boolean matches(CarrierIdentifier carrierIdentifier) {
        if (!mccMnc().equals(carrierIdentifier.mccMnc())) {
            return false;
        }
        if (gid1().isPresent()) {
            if (!gid1().get().equalsIgnoreCase(carrierIdentifier.gid1())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builder for the matcher
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setMccMnc(String mccMnc);

        public abstract Builder setGid1(String gid1);

        public abstract CarrierIdentifierMatcher build();
    }
}
