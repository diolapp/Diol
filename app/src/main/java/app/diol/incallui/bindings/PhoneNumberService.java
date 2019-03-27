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

package app.diol.incallui.bindings;

import app.diol.dialer.logging.ContactLookupResult;

/**
 * Provides phone number lookup services.
 */
public interface PhoneNumberService {

    /**
     * Get a phone number number asynchronously.
     *
     * @param phoneNumber The phone number to lookup.
     * @param listener    The listener to notify when the phone number lookup is complete.
     */
    void getPhoneNumberInfo(String phoneNumber, NumberLookupListener listener);

    interface NumberLookupListener {

        /**
         * Callback when a phone number has been looked up.
         *
         * @param info The looked up information. Or (@literal null} if there are no results.
         */
        void onPhoneNumberInfoComplete(PhoneNumberInfo info);
    }

    interface PhoneNumberInfo {

        String getDisplayName();

        String getNumber();

        int getPhoneType();

        String getPhoneLabel();

        String getNormalizedNumber();

        String getImageUrl();

        String getLookupKey();

        boolean isBusiness();

        ContactLookupResult.Type getLookupSource();
    }
}
