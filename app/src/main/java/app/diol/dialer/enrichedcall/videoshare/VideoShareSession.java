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

import android.support.annotation.NonNull;
import android.view.SurfaceView;

/**
 * Holds state information and data about video share sessions.
 */
public interface VideoShareSession {
    long getSessionId();

    void setSessionId(long sessionId);

    int getState();

    void pause();

    void unpause();

    void dispose();

    void setSurfaceView(@NonNull SurfaceView surfaceView);

    void setCamera(String cameraId);
}
