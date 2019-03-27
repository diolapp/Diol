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

package app.diol.dialer.searchfragment.common;

import app.diol.dialer.dialercontact.DialerContact;

/**
 * Interface of possible actions that can be performed by search elements.
 */
public interface RowClickListener {

    /**
     * Places a traditional voice call.
     *
     * @param ranking position in the list relative to the other elements
     */
    void placeVoiceCall(String phoneNumber, int ranking);

    /**
     * Places an IMS video call.
     *
     * @param ranking position in the list relative to the other elements
     */
    void placeVideoCall(String phoneNumber, int ranking);

    /**
     * Places a Duo video call.
     */
    void placeDuoCall(String phoneNumber);

    /**
     * Opens the enriched calling/call composer interface.
     */
    void openCallAndShare(DialerContact dialerContact);
}
