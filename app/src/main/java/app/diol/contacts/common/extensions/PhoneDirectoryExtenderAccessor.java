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

package app.diol.contacts.common.extensions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import app.diol.dialer.common.Assert;

/**
 * Accessor for the phone directory extender singleton.
 */
public final class PhoneDirectoryExtenderAccessor {

    private static PhoneDirectoryExtender instance;

    private PhoneDirectoryExtenderAccessor() {
    }

    @VisibleForTesting
    public static void setForTesting(PhoneDirectoryExtender extender) {
        instance = extender;
    }

    @NonNull
    public static PhoneDirectoryExtender get(@NonNull Context context) {
        Assert.isNotNull(context);
        if (instance != null) {
            return instance;
        }

        Context application = context.getApplicationContext();
        if (application instanceof PhoneDirectoryExtenderFactory) {
            instance = ((PhoneDirectoryExtenderFactory) application).newPhoneDirectoryExtender();
        }

        if (instance == null) {
            instance = new PhoneDirectoryExtenderStub();
        }
        return instance;
    }
}
