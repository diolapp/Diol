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
import app.diol.voicemail.impl.scheduling.BaseTask;
import app.diol.voicemail.impl.scheduling.MinimalIntervalPolicy;
import app.diol.voicemail.impl.scheduling.RetryPolicy;
import app.diol.voicemail.impl.utils.LoggerUtils;

/**
 * System initiated sync request.
 */
@UsedByReflection(value = "Tasks.java")
public class SyncTask extends BaseTask {

    // Try sync for a total of 5 times, should take around 5 minutes before finally
    // giving up.
    private static final int RETRY_TIMES = 4;
    private static final int RETRY_INTERVAL_MILLIS = 5_000;
    private static final int MINIMAL_INTERVAL_MILLIS = 60_000;

    private static final String EXTRA_PHONE_ACCOUNT_HANDLE = "extra_phone_account_handle";

    private final RetryPolicy retryPolicy;

    private PhoneAccountHandle phone;

    public SyncTask() {
        super(TASK_SYNC);
        retryPolicy = new RetryPolicy(RETRY_TIMES, RETRY_INTERVAL_MILLIS);
        addPolicy(retryPolicy);
        addPolicy(new MinimalIntervalPolicy(MINIMAL_INTERVAL_MILLIS));
    }

    public static void start(Context context, PhoneAccountHandle phone) {
        Intent intent = BaseTask.createIntent(context, SyncTask.class, phone);
        intent.putExtra(EXTRA_PHONE_ACCOUNT_HANDLE, phone);
        context.sendBroadcast(intent);
    }

    @Override
    public void onCreate(Context context, Bundle extras) {
        super.onCreate(context, extras);
        phone = extras.getParcelable(EXTRA_PHONE_ACCOUNT_HANDLE);
    }

    @Override
    public void onExecuteInBackgroundThread() {
        OmtpVvmSyncService service = new OmtpVvmSyncService(getContext());
        service.sync(this, phone, null, retryPolicy.getVoicemailStatusEditor());
    }

    @Override
    public Intent createRestartIntent() {
        LoggerUtils.logImpressionOnMainThread(getContext(), DialerImpression.Type.VVM_AUTO_RETRY_SYNC);
        Intent intent = super.createRestartIntent();
        intent.putExtra(EXTRA_PHONE_ACCOUNT_HANDLE, phone);
        return intent;
    }
}
