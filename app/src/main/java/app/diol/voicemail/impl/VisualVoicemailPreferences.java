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

import android.content.Context;
import android.preference.PreferenceManager;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.common.PerAccountSharedPreferences;

/**
 * Save visual voicemail values in shared preferences to be retrieved later.
 * Because a voicemail source is tied 1:1 to a phone account, the phone account
 * handle is used in the key for each voicemail source and the associated data.
 */
public class VisualVoicemailPreferences extends PerAccountSharedPreferences {

    public VisualVoicemailPreferences(Context context, PhoneAccountHandle phoneAccountHandle) {
        super(context, phoneAccountHandle, PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()),
                "visual_voicemail_");
    }
}
