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

package app.diol.dialer.calllog.config;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Determines if new call log components are enabled.
 */
public interface CallLogConfig {

    /**
     * Updates the config values. This may kick off a lot of work so should be done infrequently, for
     * example by a scheduled job or broadcast receiver which rarely fires.
     */
    ListenableFuture<Void> update();

    boolean isNewCallLogFragmentEnabled();

    boolean isNewVoicemailFragmentEnabled();

    boolean isNewPeerEnabled();

    boolean isCallLogFrameworkEnabled();

    /**
     * Schedules a job to periodically update the config.
     */
    void schedulePollingJob();
}
