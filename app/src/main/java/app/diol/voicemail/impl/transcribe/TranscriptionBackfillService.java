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
package app.diol.voicemail.impl.transcribe;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.support.v4.app.JobIntentService;
import android.support.v4.os.BuildCompat;
import android.telecom.PhoneAccountHandle;

import java.util.List;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.ThreadUtil;
import app.diol.dialer.constants.ScheduledJobIds;

/**
 * JobScheduler service for transcribing old voicemails. This service does a
 * database scan for un-transcribed voicemails and schedules transcription tasks
 * for them, once we have an un-metered network connection.
 */
public class TranscriptionBackfillService extends JobIntentService {

    /**
     * Schedule a task to scan the database for untranscribed voicemails
     */
    public static boolean scheduleTask(Context context, PhoneAccountHandle account) {
        if (BuildCompat.isAtLeastO()) {
            LogUtil.enterBlock("TranscriptionBackfillService.transcribeOldVoicemails");
            ComponentName componentName = new ComponentName(context, TranscriptionBackfillService.class);
            JobInfo.Builder builder = new JobInfo.Builder(ScheduledJobIds.VVM_TRANSCRIPTION_BACKFILL_JOB, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            JobScheduler scheduler = context.getSystemService(JobScheduler.class);
            return scheduler.enqueue(builder.build(), makeWorkItem(account)) == JobScheduler.RESULT_SUCCESS;
        } else {
            LogUtil.i("TranscriptionBackfillService.transcribeOldVoicemails", "not supported");
            return false;
        }
    }

    private static JobWorkItem makeWorkItem(PhoneAccountHandle account) {
        Intent intent = new Intent();
        intent.putExtra(TranscriptionService.EXTRA_ACCOUNT_HANDLE, account);
        return new JobWorkItem(intent);
    }

    @Override
    @WorkerThread
    protected void onHandleWork(Intent intent) {
        LogUtil.enterBlock("TranscriptionBackfillService.onHandleWork");

        Bundle bundle = intent.getExtras();
        final PhoneAccountHandle account = (PhoneAccountHandle) bundle.get(TranscriptionService.EXTRA_ACCOUNT_HANDLE);

        TranscriptionDbHelper dbHelper = new TranscriptionDbHelper(this);
        List<Uri> untranscribed = dbHelper.getUntranscribedVoicemails();
        LogUtil.i("TranscriptionBackfillService.onHandleWork",
                "found " + untranscribed.size() + " untranscribed voicemails");
        // TODO(mdooley): Consider doing the actual transcriptions here instead of
        // scheduling jobs.
        for (Uri uri : untranscribed) {
            ThreadUtil.postOnUiThread(() -> {
                TranscriptionService.scheduleNewVoicemailTranscriptionJob(this, uri, account, false);
            });
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.enterBlock("TranscriptionBackfillService.onDestroy");
        super.onDestroy();
    }
}
