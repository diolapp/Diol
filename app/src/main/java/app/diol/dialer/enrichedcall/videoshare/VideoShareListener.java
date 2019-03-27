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

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

/**
 * Receives updates when video share status has changed.
 */
public interface VideoShareListener {

    /**
     * Callback fired when video share has changed (service connected / disconnected, video share
     * invite received or canceled, or when a session changes).
     */
    @MainThread
    void onVideoShareChanged(@NonNull Context context);
}
