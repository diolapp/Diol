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

package app.diol.dialer.interactions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.PinnedPositions;
import android.text.TextUtils;

import app.diol.dialer.util.PermissionsUtil;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;

/**
 * This broadcast receiver is used to listen to outgoing calls and undemote formerly demoted
 * contacts if a phone call is made to a phone number belonging to that contact.
 *
 * <p>NOTE This doesn't work for corp contacts.
 */
public class UndemoteOutgoingCallReceiver extends BroadcastReceiver {

    private static final long NO_CONTACT_FOUND = -1;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!PermissionsUtil.hasPermission(context, READ_CONTACTS)
                || !PermissionsUtil.hasPermission(context, WRITE_CONTACTS)) {
            return;
        }
        if (intent != null && Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            final String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (TextUtils.isEmpty(number)) {
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    final long id = getContactIdFromPhoneNumber(context, number);
                    if (id != NO_CONTACT_FOUND) {
                        undemoteContactWithId(context, id);
                    }
                }
            }.start();
        }
    }

    private void undemoteContactWithId(Context context, long id) {
        // If the contact is not demoted, this will not do anything. Otherwise, it will
        // restore it to an unpinned position. If it was a frequently called contact, it will
        // show up once again show up on the favorites screen.
        if (PermissionsUtil.hasPermission(context, WRITE_CONTACTS)) {
            try {
                PinnedPositions.undemote(context.getContentResolver(), id);
            } catch (SecurityException e) {
                // Just in case
            }
        }
    }

    private long getContactIdFromPhoneNumber(Context context, String number) {
        if (!PermissionsUtil.hasPermission(context, READ_CONTACTS)) {
            return NO_CONTACT_FOUND;
        }
        final Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        final Cursor cursor;
        try {
            cursor =
                    context
                            .getContentResolver()
                            .query(contactUri, new String[]{PhoneLookup._ID}, null, null, null);
        } catch (SecurityException e) {
            // Just in case
            return NO_CONTACT_FOUND;
        }
        if (cursor == null) {
            return NO_CONTACT_FOUND;
        }
        try {
            if (cursor.moveToFirst()) {
                final long id = cursor.getLong(0);
                return id;
            } else {
                return NO_CONTACT_FOUND;
            }
        } finally {
            cursor.close();
        }
    }
}
