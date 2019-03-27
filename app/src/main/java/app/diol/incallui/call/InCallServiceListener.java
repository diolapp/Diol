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

package app.diol.incallui.call;

import android.telecom.InCallService;

/**
 * Interface implemented by In-Call components that maintain a reference to the Telecom API {@code
 * InCallService} object. Clarifies the expectations associated with the relevant method calls.
 */
public interface InCallServiceListener {

    /**
     * Called once at {@code InCallService} startup time with a valid instance. At that time, there
     * will be no existing {@code DialerCall}s.
     *
     * @param inCallService The {@code InCallService} object.
     */
    void setInCallService(InCallService inCallService);

    /**
     * Called once at {@code InCallService} shutdown time. At that time, any {@code DialerCall}s will
     * have transitioned through the disconnected state and will no longer exist.
     */
    void clearInCallService();
}
