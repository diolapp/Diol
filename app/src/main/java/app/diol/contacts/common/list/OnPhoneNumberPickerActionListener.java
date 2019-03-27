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

package app.diol.contacts.common.list;

import android.net.Uri;
import android.support.v7.app.ActionBar;

import app.diol.dialer.callintent.CallSpecificAppData;

/**
 * Action callbacks that can be sent by a phone number picker.
 */
public interface OnPhoneNumberPickerActionListener {

    /**
     * Returns the selected phone number uri to the requester.
     */
    void onPickDataUri(Uri dataUri, boolean isVideoCall, CallSpecificAppData callSpecificAppData);

    /**
     * Returns the specified phone number to the requester. May call the specified phone number,
     * either as an audio or video call.
     */
    void onPickPhoneNumber(
            String phoneNumber, boolean isVideoCall, CallSpecificAppData callSpecificAppData);

    /**
     * Called when home menu in {@link ActionBar} is clicked by the user.
     */
    void onHomeInActionBarSelected();
}
