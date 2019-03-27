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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.VoicemailContract.Voicemails;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.VoicemailVersionConstants;

/**
 * Receives MY_PACKAGE_REPLACED to trigger VVM activation and to check for legacy voicemail users.
 */
public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        VvmLog.i("PackageReplacedReceiver.onReceive", "package replaced, starting activation");

        if (!VoicemailComponent.get(context).getVoicemailClient().isVoicemailModuleEnabled()) {
            VvmLog.e("PackageReplacedReceiver.onReceive", "module disabled");
            return;
        }

        for (PhoneAccountHandle phoneAccountHandle :
                context.getSystemService(TelecomManager.class).getCallCapablePhoneAccounts()) {
            ActivationTask.start(context, phoneAccountHandle, null);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.contains(VoicemailVersionConstants.PREF_DIALER_FEATURE_VERSION_ACKNOWLEDGED_KEY)) {
            setVoicemailFeatureVersionAsync(context);
        }
    }

    private void setVoicemailFeatureVersionAsync(Context context) {
        LogUtil.enterBlock("PackageReplacedReceiver.setVoicemailFeatureVersionAsync");

        // Check if user is already using voicemail (ie do they have any voicemails), and set the
        // acknowledged feature value accordingly.
        PendingResult pendingResult = goAsync();
        DialerExecutorComponent.get(context)
                .dialerExecutorFactory()
                .createNonUiTaskBuilder(new ExistingVoicemailCheck(context))
                .onSuccess(
                        output -> {
                            LogUtil.i("PackageReplacedReceiver.setVoicemailFeatureVersionAsync", "success");
                            pendingResult.finish();
                        })
                .onFailure(
                        throwable -> {
                            LogUtil.i("PackageReplacedReceiver.setVoicemailFeatureVersionAsync", "failure");
                            pendingResult.finish();
                        })
                .build()
                .executeParallel(null);
    }

    private static class ExistingVoicemailCheck implements Worker<Void, Void> {
        private static final String[] PROJECTION = new String[]{Voicemails._ID};

        private final Context context;

        ExistingVoicemailCheck(Context context) {
            this.context = context;
        }

        @Override
        public Void doInBackground(Void arg) throws Throwable {
            LogUtil.i("PackageReplacedReceiver.ExistingVoicemailCheck.doInBackground", "");

            // Check the database for existing voicemails.
            boolean hasVoicemails = false;
            Uri uri = Voicemails.buildSourceUri(context.getPackageName());
            String whereClause = Calls.TYPE + " = " + Calls.VOICEMAIL_TYPE;
            try (Cursor cursor =
                         context.getContentResolver().query(uri, PROJECTION, whereClause, null, null)) {
                if (cursor == null) {
                    LogUtil.e(
                            "PackageReplacedReceiver.ExistingVoicemailCheck.doInBackground",
                            "failed to check for existing voicemails");
                } else if (cursor.moveToFirst()) {
                    hasVoicemails = true;
                }
            }

            LogUtil.i(
                    "PackageReplacedReceiver.ExistingVoicemailCheck.doInBackground",
                    "has voicemails: " + hasVoicemails);
            int version = hasVoicemails ? VoicemailVersionConstants.LEGACY_VOICEMAIL_FEATURE_VERSION : 0;
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putInt(VoicemailVersionConstants.PREF_DIALER_FEATURE_VERSION_ACKNOWLEDGED_KEY, version)
                    .apply();
            return null;
        }
    }
}
