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

package app.diol.dialer.enrichedcall.videoshare;

/**
 * Interface for receiving updates on session initialization failure or termination.
 */
public interface VideoShareSessionListener {

    void onSessionTerminated(VideoShareSession session);

    void onSessionInitializationFailed(VideoShareSession session, Exception e);

    /**
     * Called when a session hasn't received a keep-alive or video packet within the timeout time
     * period.
     *
     * @param session The session that timed out
     */
    void onSessionTimedOut(VideoShareSession session);
}
