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

package app.diol.incallui.rtt.protocol;

import android.support.v4.app.Fragment;

import java.util.List;

import app.diol.dialer.rtt.RttTranscript;
import app.diol.dialer.rtt.RttTranscriptMessage;
import app.diol.incallui.incall.protocol.InCallScreen;

/**
 * Interface for call RTT call module.
 */
public interface RttCallScreen extends InCallScreen {

    void onRttScreenStart();

    void onRttScreenStop();

    void onRemoteMessage(String message);

    void onRestoreRttChat(RttTranscript rttTranscript);

    List<RttTranscriptMessage> getRttTranscriptMessageList();

    Fragment getRttCallScreenFragment();

    String getCallId();
}
