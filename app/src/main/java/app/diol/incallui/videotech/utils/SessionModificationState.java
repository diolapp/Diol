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

package app.diol.incallui.videotech.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines different states of session modify requests, which are used to upgrade to video, or
 * downgrade to audio.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        SessionModificationState.NO_REQUEST,
        SessionModificationState.WAITING_FOR_UPGRADE_TO_VIDEO_RESPONSE,
        SessionModificationState.REQUEST_FAILED,
        SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST,
        SessionModificationState.UPGRADE_TO_VIDEO_REQUEST_TIMED_OUT,
        SessionModificationState.UPGRADE_TO_VIDEO_REQUEST_FAILED,
        SessionModificationState.REQUEST_REJECTED,
        SessionModificationState.WAITING_FOR_RESPONSE
})
public @interface SessionModificationState {
    int NO_REQUEST = 0;
    int WAITING_FOR_UPGRADE_TO_VIDEO_RESPONSE = 1;
    int REQUEST_FAILED = 2;
    int RECEIVED_UPGRADE_TO_VIDEO_REQUEST = 3;
    int UPGRADE_TO_VIDEO_REQUEST_TIMED_OUT = 4;
    int UPGRADE_TO_VIDEO_REQUEST_FAILED = 5;
    int REQUEST_REJECTED = 6;
    int WAITING_FOR_RESPONSE = 7;
}
