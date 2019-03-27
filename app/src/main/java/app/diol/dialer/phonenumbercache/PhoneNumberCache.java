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

package app.diol.dialer.phonenumbercache;

import android.content.Context;

import java.util.Objects;

/**
 * Accessor for the phone number cache bindings.
 */
public class PhoneNumberCache {

    private static PhoneNumberCacheBindings phoneNumberCacheBindings;

    private PhoneNumberCache() {
    }

    public static PhoneNumberCacheBindings get(Context context) {
        Objects.requireNonNull(context);
        if (phoneNumberCacheBindings != null) {
            return phoneNumberCacheBindings;
        }

        Context application = context.getApplicationContext();
        if (application instanceof PhoneNumberCacheBindingsFactory) {
            phoneNumberCacheBindings =
                    ((PhoneNumberCacheBindingsFactory) application).newPhoneNumberCacheBindings();
        }

        if (phoneNumberCacheBindings == null) {
            phoneNumberCacheBindings = new PhoneNumberCacheBindingsStub();
        }
        return phoneNumberCacheBindings;
    }

    public static void setForTesting(PhoneNumberCacheBindings phoneNumberCacheBindings) {
        PhoneNumberCache.phoneNumberCacheBindings = phoneNumberCacheBindings;
    }
}
