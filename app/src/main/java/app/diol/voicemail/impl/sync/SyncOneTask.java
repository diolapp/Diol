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

package app.diol.voicemail.impl.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.proguard.UsedByReflection;
import app.diol.voicemail.impl.Voicemail;
import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.scheduling.BaseTask;
import app.diol.voicemail.impl.scheduling.RetryPolicy;
import app.diol.voicemail.impl.utils.LoggerUtils;

/**
 * Task to download a single voicemail from the server. This task is initiated
 * by a SMS notifying the new voicemail arrival, and ignores the duplicated
 * tasks constraint.
 */
@UsedByReflection(value = "Tasks.java")
public class SyncOneTask extends BaseTask {

    private static final int RETRY_TIMES = 2;
    private static final int RETRY_INTERVAL_MILLIS = 5_000;

    private static final String EXTRA_PHONE_ACCOUNT_HANDLE = "extra_phone_account_handle";
    private static final String EXTRA_VOICEMAIL = "extra_voicemail";

    private PhoneAccountHandle phone;
    private Voicemail voicemail;

    public SyncOneTask() {
        super(TASK_ALLOW_DUPLICATES);
        addPolicy(new RetryPolicy(RETRY_TIMES, RETRY_INTERVAL_MILLIS));
    }

    public static void start(Context context, PhoneAccountHandle phone, Voicemail voicemail) {
        Intent intent = BaseTask.createIntent(context, SyncOneTask.class, phone);
        intent.putExtra(EXTRA_PHONE_ACCOUNT_HANDLE, phone);
        intent.putExtra(EXTRA_VOICEMAIL, voicemail);
        context.sendBroadcast(intent);
    }

    @Override
    public void onCreate(Context context, Bundle extras) {
        super.onCreate(context, extras);
        phone = extras.getParcelable(EXTRA_PHONE_ACCOUNT_HANDLE);
        voicemail = extras.getParcelable(EXTRA_VOICEMAIL);
    }

    @Override
    public void onExecuteInBackgroundThread() {
        OmtpVvmSyncService service = new OmtpVvmSyncService(getContext());
        service.sync(this, phone, voicemail, VoicemailStatus.edit(getContext(), phone));
    }

    @Override
    public Intent createRestartIntent() {
        LoggerUtils.logImpressionOnMainThread(getContext(), DialerImpression.Type.VVM_AUTO_RETRY_SYNC);
        Intent intent = super.createRestartIntent();
        intent.putExtra(EXTRA_PHONE_ACCOUNT_HANDLE, phone);
        intent.putExtra(EXTRA_VOICEMAIL, voicemail);
        return intent;
    }
}
