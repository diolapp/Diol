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

package app.diol.dialer.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Registry of scheduled job ids used by the dialer UID.
 *
 * <p>Any dialer jobs which use the android JobScheduler should register their IDs here, to avoid
 * the same ID accidentally being reused.
 *
 * <p>Do not change any existing IDs.
 */
public final class ScheduledJobIds {
    public static final int SPAM_JOB_WIFI = 50;
    public static final int SPAM_JOB_ANY_NETWORK = 51;
    // This job refreshes dynamic launcher shortcuts.
    public static final int SHORTCUT_PERIODIC_JOB = 100;
    public static final int VVM_TASK_SCHEDULER_JOB = 200;
    public static final int VVM_STATUS_CHECK_JOB = 201;
    public static final int VVM_DEVICE_PROVISIONED_JOB = 202;
    public static final int VVM_TRANSCRIPTION_JOB = 203;
    public static final int VVM_TRANSCRIPTION_BACKFILL_JOB = 204;
    public static final int VVM_NOTIFICATION_JOB = 205;
    public static final int VVM_TRANSCRIPTION_RATING_JOB = 206;
    public static final int VOIP_REGISTRATION = 300;
    public static final int CALL_LOG_CONFIG_POLLING_JOB = 400;
    // Job Ids from 10_000 to 10_100 should be reserved for proto upload jobs.
    public static final int PROTO_UPLOAD_JOB_MIN_ID = 10_000;
    public static final int PROTO_UPLOAD_JOB_MAX_ID = 10_100;
    /**
     * Spam job type including all spam job IDs.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SPAM_JOB_WIFI, SPAM_JOB_ANY_NETWORK})
    public @interface SpamJobType {
    }
}
