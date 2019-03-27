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

package app.diol.dialer.shortcuts;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * {@link AsyncTask} used by the periodic job service to refresh dynamic and pinned shortcuts.
 */
@TargetApi(VERSION_CODES.N_MR1) // Shortcuts introduced in N MR1
final class RefreshShortcutsTask extends AsyncTask<JobParameters, Void, JobParameters> {

    private final JobService jobService;

    RefreshShortcutsTask(@NonNull JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * @param params array with length 1, provided from PeriodicJobService
     */
    @Override
    @NonNull
    @WorkerThread
    protected JobParameters doInBackground(JobParameters... params) {
        Assert.isWorkerThread();
        LogUtil.enterBlock("RefreshShortcutsTask.doInBackground");

        // Dynamic shortcuts are refreshed from the UI but icons can become stale, so update them
        // periodically using the job service.
        //
        // The reason that icons can become is stale is that there is no last updated timestamp for
        // pictures; there is only a last updated timestamp for the entire contact row, which changes
        // frequently (for example, when they are called their "times_contacted" is incremented).
        // Relying on such a spuriously updated timestamp would result in too frequent shortcut updates,
        // so instead we just allow the icon to become stale in the case that the contact's photo is
        // updated, and then rely on the job service to periodically force update it.
        new DynamicShortcuts(jobService, new IconFactory(jobService)).updateIcons(); // Blocking
        new PinnedShortcuts(jobService).refresh(); // Blocking

        return params[0];
    }

    @Override
    @MainThread
    protected void onPostExecute(JobParameters params) {
        Assert.isMainThread();
        LogUtil.enterBlock("RefreshShortcutsTask.onPostExecute");

        jobService.jobFinished(params, false /* needsReschedule */);
    }
}
