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

import app.diol.dialer.common.LogUtil;

/**
 * The Mediator for Assisted Dialing.
 *
 * <p>This class is responsible for mediating location discovery of the user, determining if the
 * call is eligible for assisted dialing, and performing the transformation of numbers eligible for
 * assisted dialing.
 */
final class AssistedDialingMediatorImpl implements AssistedDialingMediator {

    private final LocationDetector locationDetector;
    private final NumberTransformer numberTransformer;

    AssistedDialingMediatorImpl(
            @NonNull LocationDetector locationDetector, @NonNull NumberTransformer numberTransformer) {
        if (locationDetector == null) {
            throw new NullPointerException("locationDetector was null");
        }

        if (numberTransformer == null) {
            throw new NullPointerException("numberTransformer was null");
        }
        this.locationDetector = locationDetector;
        this.numberTransformer = numberTransformer;
    }

    @Override
    public boolean isPlatformEligible() {
        // This impl is only instantiated if it passes platform checks in ConcreteCreator,
        // so we return true here.
        return true;
    }

    /**
     * Returns the country code in which the library thinks the user typically resides.
     */
    @Override
    public Optional<String> userHomeCountryCode() {
        return locationDetector.getUpperCaseUserHomeCountry();
    }

    /**
     * Returns an Optional of type String containing the transformed number that was provided. The
     * transformed number should be capable of dialing out of the User's current country and
     * successfully connecting with a contact in the User's home country.
     */
    @Override
    public Optional<TransformationInfo> attemptAssistedDial(@NonNull String numberToTransform) {
        Optional<String> userHomeCountryCode = locationDetector.getUpperCaseUserHomeCountry();
        Optional<String> userRoamingCountryCode = locationDetector.getUpperCaseUserRoamingCountry();

        if (!userHomeCountryCode.isPresent() || !userRoamingCountryCode.isPresent()) {
            LogUtil.i("AssistedDialingMediator.attemptAssistedDial", "Unable to determine country codes");
            return Optional.empty();
        }

        return numberTransformer.doAssistedDialingTransformation(
                numberToTransform, userHomeCountryCode.get(), userRoamingCountryCode.get());
    }
}
