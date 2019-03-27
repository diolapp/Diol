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

package app.diol.dialer.glidephotomanager.impl;

import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.DisplayNameSources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.diol.dialer.glidephotomanager.PhotoInfo;

/**
 * Generate a lookup URI that will populate the quick contact with the number. Used when the lookup
 * URI is not available (non-contact and no other sources). The info is encoded into a JSON string
 * that is not governed by any public interface. URI format:
 * content://com.android.contacts/contacts/lookup/encoded/[JSON]
 *
 * <p>The JSON is a object containing "display_name", "display_name_source" ({@link
 * DisplayNameSources}), and several {@link ContactsContract.Data} rows keyed by the {@link
 * ContactsContract.Data#MIMETYPE}. In this case only {@link
 * ContactsContract.CommonDataKinds.Phone#CONTENT_ITEM_TYPE} is available.
 *
 * <p>Example JSON:<br>
 * {"display_name":"+1 650-253-0000","display_name_source":30,"vnd.android.cursor.item\/contact":{
 * "vnd.android.cursor.item\/phone_v2":[{"data1":"+1 650-253-0000","data2":12}]}}
 */
final class DefaultLookupUriGenerator {

    static Uri generateUri(PhotoInfo photoInfo) {
        JSONObject lookupJson = new JSONObject();
        try {
            lookupJson.put(Contacts.DISPLAY_NAME, photoInfo.getFormattedNumber());
            // DISPLAY_NAME_SOURCE required by contacts, otherwise the URI will not be recognized.
            lookupJson.put(Contacts.DISPLAY_NAME_SOURCE, DisplayNameSources.PHONE);
            JSONObject contactRows = new JSONObject();
            JSONObject phone = new JSONObject();
            phone.put(CommonDataKinds.Phone.NUMBER, photoInfo.getFormattedNumber());
            contactRows.put(CommonDataKinds.Phone.CONTENT_ITEM_TYPE, new JSONArray().put(phone));

            lookupJson.put(Contacts.CONTENT_ITEM_TYPE, contactRows);
        } catch (JSONException e) {
            throw new AssertionError(e);
        }
        return Contacts.CONTENT_LOOKUP_URI
                .buildUpon()
                .appendPath("encoded")
                .encodedFragment(lookupJson.toString())
                // Directory is required in the URI but it does not exist, use MAX_VALUE to avoid clashing
                // with other directory
                .appendQueryParameter(
                        ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Integer.MAX_VALUE))
                .build();
    }
}
