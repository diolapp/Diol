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

import app.diol.dialer.proguard.UsedByReflection;
import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.scheduling.BaseTask;
import app.diol.voicemail.impl.scheduling.PostponePolicy;

/**
 * Upload task triggered by database changes. Will wait until the database has
 * been stable for {@link #POSTPONE_MILLIS} to execute.
 */
@UsedByReflection(value = "Tasks.java")
public class UploadTask extends BaseTask {

    private static final String TAG = "VvmUploadTask";

    private static final int POSTPONE_MILLIS = 5_000;

    public UploadTask() {
        super(TASK_UPLOAD);
        addPolicy(new PostponePolicy(POSTPONE_MILLIS));
    }

    public static void start(Context context, PhoneAccountHandle phoneAccountHandle) {
        Intent intent = BaseTask.createIntent(context, UploadTask.class, phoneAccountHandle);
        context.sendBroadcast(intent);
    }

    @Override
    public void onCreate(Context context, Bundle extras) {
        super.onCreate(context, extras);
    }

    @Override
    public void onExecuteInBackgroundThread() {
        OmtpVvmSyncService service = new OmtpVvmSyncService(getContext());

        PhoneAccountHandle phoneAccountHandle = getPhoneAccountHandle();
        if (phoneAccountHandle == null) {
            // This should never happen
            VvmLog.e(TAG, "null phone account for phoneAccountHandle " + getPhoneAccountHandle());
            return;
        }
        service.sync(this, phoneAccountHandle, null, VoicemailStatus.edit(getContext(), phoneAccountHandle));
    }
}
