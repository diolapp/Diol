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

package app.diol.dialer.postcall;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.enrichedcall.EnrichedCallComponent;
import app.diol.dialer.enrichedcall.EnrichedCallManager;
import app.diol.dialer.util.PermissionsUtil;
import app.diol.dialer.widget.DialerToolbar;
import app.diol.dialer.widget.MessageFragment;

/**
 * Activity used to send post call messages after a phone call.
 */
public class PostCallActivity extends AppCompatActivity implements MessageFragment.Listener {

    public static final String KEY_PHONE_NUMBER = "phone_number";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_RCS_POST_CALL = "rcs_post_call";
    private static final int REQUEST_CODE_SEND_SMS = 1;

    private boolean useRcs;

    public static Intent newIntent(
            @NonNull Context context, @NonNull String number, boolean isRcsPostCall) {
        Intent intent = new Intent(Assert.isNotNull(context), PostCallActivity.class);
        intent.putExtra(KEY_PHONE_NUMBER, Assert.isNotNull(number));
        intent.putExtra(KEY_RCS_POST_CALL, isRcsPostCall);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.post_call_activity);

        ((DialerToolbar) findViewById(R.id.toolbar)).setTitle(R.string.post_call_message);
        useRcs = getIntent().getBooleanExtra(KEY_RCS_POST_CALL, false);
        LogUtil.i("PostCallActivity.onCreate", "useRcs: %b", useRcs);

        int postCallCharLimit =
                useRcs
                        ? getResources().getInteger(R.integer.post_call_char_limit)
                        : MessageFragment.NO_CHAR_LIMIT;
        String[] messages =
                new String[]{
                        getString(R.string.post_call_message_1),
                        getString(R.string.post_call_message_2),
                        getString(R.string.post_call_message_3)
                };
        MessageFragment fragment =
                MessageFragment.builder()
                        .setCharLimit(postCallCharLimit)
                        .showSendIcon()
                        .setMessages(messages)
                        .build();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.message_container, fragment)
                .commit();
    }

    @Override
    public void onMessageFragmentSendMessage(@NonNull String message) {
        String number = Assert.isNotNull(getIntent().getStringExtra(KEY_PHONE_NUMBER));
        getIntent().putExtra(KEY_MESSAGE, message);

        if (useRcs) {
            LogUtil.i("PostCallActivity.onMessageFragmentSendMessage", "sending post call Rcs.");
            getEnrichedCallManager().sendPostCallNote(number, message);
            PostCall.onMessageSent(this, number);
            finish();
        } else if (PermissionsUtil.hasPermission(this, permission.SEND_SMS)) {
            LogUtil.i("PostCallActivity.sendMessage", "Sending post call SMS.");
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendMultipartTextMessage(
                    number, null, smsManager.divideMessage(message), null, null);
            PostCall.onMessageSent(this, number);
            finish();
        } else if (PermissionsUtil.isFirstRequest(this, permission.SEND_SMS)
                || shouldShowRequestPermissionRationale(permission.SEND_SMS)) {
            LogUtil.i("PostCallActivity.sendMessage", "Requesting SMS_SEND permission.");
            requestPermissions(new String[]{permission.SEND_SMS}, REQUEST_CODE_SEND_SMS);
        } else {
            LogUtil.i(
                    "PostCallActivity.sendMessage", "Permission permanently denied, sending to settings.");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivity(intent);
        }
    }

    @Override
    public void onMessageFragmentAfterTextChange(String message) {
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0 && permissions[0].equals(permission.SEND_SMS)) {
            PermissionsUtil.permissionRequested(this, permissions[0]);
        }
        if (requestCode == REQUEST_CODE_SEND_SMS
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMessageFragmentSendMessage(getIntent().getStringExtra(KEY_MESSAGE));
        }
    }

    @NonNull
    private EnrichedCallManager getEnrichedCallManager() {
        return EnrichedCallComponent.get(this).getEnrichedCallManager();
    }
}
