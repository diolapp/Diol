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
package app.diol.voicemail.impl.settings;

import android.content.ContentValues;
import android.content.Context;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.VoicemailContract.Voicemails;
import android.support.annotation.VisibleForTesting;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.common.database.Selection;
import app.diol.dialer.compat.android.provider.VoicemailCompat;
import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.OmtpVvmCarrierConfigHelper;
import app.diol.voicemail.impl.VisualVoicemailPreferences;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.sync.VvmAccountManager;

/**
 * Save whether or not a particular account is enabled in shared to be retrieved
 * later.
 */
public class VisualVoicemailSettingsUtil {

    @VisibleForTesting
    public static final String IS_ENABLED_KEY = "is_enabled";
    private static final String ARCHIVE_ENABLED_KEY = "archive_is_enabled";
    private static final String TRANSCRIBE_VOICEMAILS_KEY = "transcribe_voicemails";
    private static final String DONATE_VOICEMAILS_KEY = "donate_voicemails";

    public static void setEnabled(Context context, PhoneAccountHandle phoneAccount, boolean isEnabled) {
        VvmLog.i("VisualVoicemailSettingsUtil.setEnable", phoneAccount + " enabled:" + isEnabled);
        new VisualVoicemailPreferences(context, phoneAccount).edit().putBoolean(IS_ENABLED_KEY, isEnabled).apply();
        OmtpVvmCarrierConfigHelper config = new OmtpVvmCarrierConfigHelper(context, phoneAccount);
        if (isEnabled) {
            config.startActivation();
        } else {
            VvmAccountManager.removeAccount(context, phoneAccount);
            config.startDeactivation();
            // Remove all voicemails from the database
            DialerExecutorComponent.get(context).dialerExecutorFactory()
                    .createNonUiTaskBuilder(new VoicemailDeleteWorker(context)).onSuccess(VisualVoicemailSettingsUtil::onSuccess)
                    .onFailure(VisualVoicemailSettingsUtil::onFailure).build().executeParallel(null);
        }
    }

    private static void onSuccess(Void unused) {
        VvmLog.i("VisualVoicemailSettingsUtil.onSuccess", "delete voicemails");
    }

    private static void onFailure(Throwable t) {
        VvmLog.e("VisualVoicemailSettingsUtil.onFailure", "delete voicemails", t);
    }

    public static void setArchiveEnabled(Context context, PhoneAccountHandle phoneAccount, boolean isEnabled) {
        Assert.checkArgument(VoicemailComponent.get(context).getVoicemailClient().isVoicemailArchiveAvailable(context));
        new VisualVoicemailPreferences(context, phoneAccount).edit().putBoolean(ARCHIVE_ENABLED_KEY, isEnabled).apply();
    }

    public static void setVoicemailTranscriptionEnabled(Context context, PhoneAccountHandle phoneAccount,
                                                        boolean isEnabled) {
        Assert.checkArgument(
                VoicemailComponent.get(context).getVoicemailClient().isVoicemailTranscriptionAvailable(context, phoneAccount));
        new VisualVoicemailPreferences(context, phoneAccount).edit().putBoolean(TRANSCRIBE_VOICEMAILS_KEY, isEnabled)
                .apply();

        if (!isEnabled) {
            VvmLog.i("VisualVoicemailSettingsUtil.setVoicemailTranscriptionEnabled",
                    "clear all Google transcribed voicemail.");
            DialerExecutorComponent.get(context).dialerExecutorFactory()
                    .createNonUiTaskBuilder(new ClearGoogleTranscribedVoicemailTranscriptionWorker(context))
                    .onSuccess((result) -> VvmLog.i("VisualVoicemailSettingsUtil.setVoicemailTranscriptionEnabled",
                            "voicemail transciptions cleared successfully"))
                    .onFailure((throwable) -> VvmLog.e("VisualVoicemailSettingsUtil.setVoicemailTranscriptionEnabled",
                            "unable to clear Google transcribed voicemails", throwable))
                    .build().executeParallel(null);
        }
    }

    public static void setVoicemailDonationEnabled(Context context, PhoneAccountHandle phoneAccount, boolean isEnabled) {
        Assert.checkArgument(
                VoicemailComponent.get(context).getVoicemailClient().isVoicemailTranscriptionAvailable(context, phoneAccount));
        new VisualVoicemailPreferences(context, phoneAccount).edit().putBoolean(DONATE_VOICEMAILS_KEY, isEnabled).apply();
    }

    public static boolean isEnabled(Context context, PhoneAccountHandle phoneAccount) {
        if (phoneAccount == null) {
            LogUtil.i("VisualVoicemailSettingsUtil.isEnabled", "phone account is null");
            return false;
        }

        VisualVoicemailPreferences prefs = new VisualVoicemailPreferences(context, phoneAccount);
        if (prefs.contains(IS_ENABLED_KEY)) {
            // isEnableByDefault is a bit expensive, so don't use it as default value of
            // getBoolean(). The "false" here should never be actually used.
            return prefs.getBoolean(IS_ENABLED_KEY, false);
        }
        return new OmtpVvmCarrierConfigHelper(context, phoneAccount).isEnabledByDefault();
    }

    public static boolean isArchiveEnabled(Context context, PhoneAccountHandle phoneAccount) {
        Assert.isNotNull(phoneAccount);

        VisualVoicemailPreferences prefs = new VisualVoicemailPreferences(context, phoneAccount);
        return prefs.getBoolean(ARCHIVE_ENABLED_KEY, false);
    }

    public static boolean isVoicemailTranscriptionEnabled(Context context, PhoneAccountHandle phoneAccount) {
        Assert.isNotNull(phoneAccount);

        VisualVoicemailPreferences prefs = new VisualVoicemailPreferences(context, phoneAccount);
        return prefs.getBoolean(TRANSCRIBE_VOICEMAILS_KEY, false);
    }

    public static boolean isVoicemailDonationEnabled(Context context, PhoneAccountHandle phoneAccount) {
        Assert.isNotNull(phoneAccount);

        VisualVoicemailPreferences prefs = new VisualVoicemailPreferences(context, phoneAccount);
        return prefs.getBoolean(DONATE_VOICEMAILS_KEY, false);
    }

    /**
     * Whether the client enabled status is explicitly set by user or by
     * default(Whether carrier VVM app is installed). This is used to determine
     * whether to disable the client when the carrier VVM app is installed. If the
     * carrier VVM app is installed the client should give priority to it if the
     * settings are not touched.
     */
    public static boolean isEnabledUserSet(Context context, PhoneAccountHandle phoneAccount) {
        if (phoneAccount == null) {
            return false;
        }
        VisualVoicemailPreferences prefs = new VisualVoicemailPreferences(context, phoneAccount);
        return prefs.contains(IS_ENABLED_KEY);
    }

    /**
     * Delete all the voicemails whose source_package field matches this package
     */
    private static class VoicemailDeleteWorker implements Worker<Void, Void> {
        private final Context context;

        VoicemailDeleteWorker(Context context) {
            this.context = context;
        }

        @Override
        public Void doInBackground(Void unused) {
            int deleted = context.getContentResolver().delete(Voicemails.buildSourceUri(context.getPackageName()), null,
                    null);

            VvmLog.i("VisualVoicemailSettingsUtil.doInBackground", "deleted " + deleted + " voicemails");
            return null;
        }
    }

    /**
     * Clears all the voicemail transcripts in the call log whose source_package
     * field matches this package
     */
    private static class ClearGoogleTranscribedVoicemailTranscriptionWorker implements Worker<Void, Void> {
        private final Context context;

        ClearGoogleTranscribedVoicemailTranscriptionWorker(Context context) {
            this.context = context;
        }

        @Override
        public Void doInBackground(Void unused) {
            ContentValues contentValues = new ContentValues();
            contentValues.putNull(Voicemails.TRANSCRIPTION);
            contentValues.put(VoicemailCompat.TRANSCRIPTION_STATE, VoicemailCompat.TRANSCRIPTION_NOT_STARTED);

            Selection selection = Selection.builder().and(Selection.column(CallLog.Calls.TYPE).is("=", Calls.VOICEMAIL_TYPE))
                    .and(Selection.column(Voicemails.SOURCE_PACKAGE).is("=", context.getPackageName())).build();

            int cleared = context.getContentResolver().update(Calls.CONTENT_URI_WITH_VOICEMAIL, contentValues,
                    selection.getSelection(), selection.getSelectionArgs());

            VvmLog.i("VisualVoicemailSettingsUtil.doInBackground", "cleared " + cleared + " voicemail transcription");
            return null;
        }
    }
}
