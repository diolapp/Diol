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

package app.diol.incallui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;

/**
 * Activity that contains an alert dialog with OK and Cancel buttons to allow user to Accept or
 * Reject the WAIT inserted as part of the Dial string.
 */
public class PostCharDialogActivity extends AppCompatActivity implements CallList.Listener {

    public static final String EXTRA_CALL_ID = "extra_call_id";
    public static final String EXTRA_POST_DIAL_STRING = "extra_post_dial_string";
    private static final String TAG_INTERNATIONAL_CALL_ON_WIFI = "tag_international_call_on_wifi";

    private String callId;

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        callId = getIntent().getStringExtra(EXTRA_CALL_ID);
        String postDialString = getIntent().getStringExtra(EXTRA_POST_DIAL_STRING);
        if (callId == null || postDialString == null) {
            finish();
            return;
        }

        PostCharDialogFragment fragment = new PostCharDialogFragment(callId, postDialString);
        fragment.show(getSupportFragmentManager(), TAG_INTERNATIONAL_CALL_ON_WIFI);

        CallList.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CallList.getInstance().removeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // We don't expect the activity to resume, except for orientation change.
        if (!isChangingConfigurations()) {
            finish();
        }
    }

    @Override
    public void onDisconnect(DialerCall call) {
        if (callId.equals(call.getId())) {
            finish();
        }
    }

    @Override
    public void onIncomingCall(DialerCall call) {
    }

    @Override
    public void onUpgradeToVideo(DialerCall call) {
    }

    @Override
    public void onUpgradeToRtt(DialerCall call, int rttRequestId) {
    }

    @Override
    public void onSessionModificationStateChange(DialerCall call) {
    }

    @Override
    public void onCallListChange(CallList callList) {
    }

    @Override
    public void onWiFiToLteHandover(DialerCall call) {
    }

    @Override
    public void onHandoverToWifiFailed(DialerCall call) {
    }

    @Override
    public void onInternationalCallOnWifi(@NonNull DialerCall call) {
    }
}
