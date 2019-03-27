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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import app.diol.contacts.common.database.ContactUpdateUtils;

/**
 * Service for updating primary number on a contact.
 */
public class ContactUpdateService extends IntentService {

    public static final String EXTRA_PHONE_NUMBER_DATA_ID = "phone_number_data_id";

    public ContactUpdateService() {
        super(ContactUpdateService.class.getSimpleName());
        setIntentRedelivery(true);
    }

    /**
     * Creates an intent that sets the selected data item as super primary (default)
     */
    public static Intent createSetSuperPrimaryIntent(Context context, long dataId) {
        Intent serviceIntent = new Intent(context, ContactUpdateService.class);
        serviceIntent.putExtra(EXTRA_PHONE_NUMBER_DATA_ID, dataId);
        return serviceIntent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Currently this service only handles one type of update.
        long dataId = intent.getLongExtra(EXTRA_PHONE_NUMBER_DATA_ID, -1);

        ContactUpdateUtils.setSuperPrimary(this, dataId);
    }
}
