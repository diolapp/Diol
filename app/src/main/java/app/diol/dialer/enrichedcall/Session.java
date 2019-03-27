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

package app.diol.dialer.enrichedcall;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.diol.dialer.multimedia.MultimediaData;

/**
 * Holds state information and data about enriched calling sessions.
 */
public interface Session {

    int STATE_NONE = 0;
    int STATE_STARTING = STATE_NONE + 1;
    int STATE_STARTED = STATE_STARTING + 1;
    int STATE_START_FAILED = STATE_STARTED + 1;
    int STATE_MESSAGE_SENT = STATE_START_FAILED + 1;
    int STATE_MESSAGE_FAILED = STATE_MESSAGE_SENT + 1;
    int STATE_CLOSED = STATE_MESSAGE_FAILED + 1;
    /**
     * Id used for sessions that fail to start.
     */
    long NO_SESSION_ID = -1;
    /**
     * An id for the specific case when sending a message fails so early that a message id isn't
     * created.
     */
    String MESSAGE_ID_COULD_NOT_CREATE_ID = "messageIdCouldNotCreateId";

    /**
     * Returns the id associated with this session, or {@link #NO_SESSION_ID} if this represents a
     * session that failed to start.
     */
    long getSessionId();

    /**
     * Returns the id of the dialer call associated with this session, or null if there isn't one.
     */
    @Nullable
    String getUniqueDialerCallId();

    void setUniqueDialerCallId(@NonNull String id);

    /**
     * Returns the number associated with the remote end of this session.
     */
    @NonNull
    String getRemoteNumber();

    /**
     * Returns the {@link State} for this session.
     */
    @State
    int getState();

    /**
     * Returns the {@link MultimediaData} associated with this session.
     */
    @NonNull
    MultimediaData getMultimediaData();

    /**
     * Returns type of this session, based on some arbitrarily defined type.
     */
    int getType();

    /**
     * Sets the {@link MultimediaData} for this session.
     *
     * @throws IllegalArgumentException if the type is invalid
     */
    void setSessionData(@NonNull MultimediaData multimediaData, int type);

    /**
     * Possible states for call composer sessions.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            STATE_NONE,
            STATE_STARTING,
            STATE_STARTED,
            STATE_START_FAILED,
            STATE_MESSAGE_SENT,
            STATE_MESSAGE_FAILED,
            STATE_CLOSED,
    })
    @interface State {
    }
}
