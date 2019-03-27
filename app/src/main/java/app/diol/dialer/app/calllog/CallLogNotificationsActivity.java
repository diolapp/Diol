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

package app.diol.dialer.app.calllog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.util.PermissionsUtil;

/**
 * Provides operations for managing call-related notifications. This is used to forward intent
 * that's requiring to unlock screen and it will never be visible to user.
 *
 * <p>It handles the following actions:
 *
 * <ul>
 * <li>Sending an SMS from a missed call
 * </ul>
 */
public class CallLogNotificationsActivity extends AppCompatActivity {

    public static final String ACTION_SEND_SMS_FROM_MISSED_CALL_NOTIFICATION =
            "app.diol.dialer.calllog.SEND_SMS_FROM_MISSED_CALL_NOTIFICATION";

    /**
     * Extra to be included with {@link #ACTION_SEND_SMS_FROM_MISSED_CALL_NOTIFICATION} to identify
     * the number to text back.
     *
     * <p>It must be a {@link String}.
     */
    public static final String EXTRA_MISSED_CALL_NUMBER = "MISSED_CALL_NUMBER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if (!PermissionsUtil.hasPermission(this, android.Manifest.permission.READ_CALL_LOG)) {
            return;
        }

        String action = intent.getAction();
        switch (action) {
            case ACTION_SEND_SMS_FROM_MISSED_CALL_NOTIFICATION:
                MissedCallNotifier.getInstance(this)
                        .sendSmsFromMissedCall(
                                intent.getStringExtra(EXTRA_MISSED_CALL_NUMBER), intent.getData());
                break;
            default:
                LogUtil.d("CallLogNotificationsActivity.onCreate", "could not handle: " + intent);
                break;
        }
        finish();
    }
}
