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

package app.diol.dialer.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Utilities for creation of intents in Dialer.
 */
public class IntentUtil {

    private static final String SMS_URI_PREFIX = "sms:";
    private static final int NO_PHONE_TYPE = -1;

    public static Intent getSendSmsIntent(CharSequence phoneNumber) {
        return new Intent(Intent.ACTION_SENDTO, Uri.parse(SMS_URI_PREFIX + phoneNumber));
    }

    public static Intent getNewContactIntent() {
        return new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public static Intent getNewContactIntent(CharSequence phoneNumber) {
        return getNewContactIntent(null /* name */, phoneNumber /* phoneNumber */, NO_PHONE_TYPE);
    }

    public static Intent getNewContactIntent(
            CharSequence name, CharSequence phoneNumber, int phoneNumberType) {
        Intent intent = getNewContactIntent();
        populateContactIntent(intent, name, phoneNumber, phoneNumberType);
        return intent;
    }

    public static Intent getAddToExistingContactIntent() {
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        return intent;
    }

    public static Intent getAddToExistingContactIntent(CharSequence phoneNumber) {
        return getAddToExistingContactIntent(
                null /* name */, phoneNumber /* phoneNumber */, NO_PHONE_TYPE);
    }

    public static Intent getAddToExistingContactIntent(
            CharSequence name, CharSequence phoneNumber, int phoneNumberType) {
        Intent intent = getAddToExistingContactIntent();
        populateContactIntent(intent, name, phoneNumber, phoneNumberType);
        return intent;
    }

    private static void populateContactIntent(
            Intent intent, CharSequence name, CharSequence phoneNumber, int phoneNumberType) {
        if (phoneNumber != null) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        }
        if (name != null) {
            intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        }
        if (phoneNumberType != NO_PHONE_TYPE) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, phoneNumberType);
        }
    }
}
