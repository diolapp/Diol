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

package app.diol.dialer.assisteddialing;

import android.support.annotation.NonNull;

import java.util.Optional;

/**
 * A stub assisted dialing implementation.
 */
public final class AssistedDialingMediatorStub implements AssistedDialingMediator {

    /**
     * Always returns an empty Optional.
     */
    @Override
    public Optional<TransformationInfo> attemptAssistedDial(@NonNull String numberToTransform) {
        return Optional.empty();
    }

    /**
     * Always returns an empty Optional.
     */
    @Override
    public Optional<String> userHomeCountryCode() {
        return Optional.empty();
    }

    @Override
    public boolean isPlatformEligible() {
        return false;
    }
}
