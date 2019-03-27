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

import android.support.v7.app.AppCompatActivity;

/**
 * Class containing {@link AppCompatActivity#onActivityResult(int, int, android.content.Intent)}
 * request codes.
 */
public final class ActivityRequestCodes {

    /**
     * Request code for {@link android.speech.RecognizerIntent#ACTION_RECOGNIZE_SPEECH} intent.
     */
    public static final int DIALTACTS_VOICE_SEARCH = 1;
    /**
     * Request code for {@link app.diol.dialer.callcomposer.CallComposerActivity} intent.
     */
    public static final int DIALTACTS_CALL_COMPOSER = 2;
    /**
     * Request code for {@link app.diol.dialer.duo.Duo#getCallIntent(String)}.
     */
    public static final int DIALTACTS_DUO = 3;
    /**
     * Request code for {@link app.diol.dialer.calldetails.OldCallDetailsActivity} intent.
     */
    public static final int DIALTACTS_CALL_DETAILS = 4;
    /**
     * Request code for {@link app.diol.dialer.speeddial.SpeedDialFragment} contact picker intent.
     */
    public static final int SPEED_DIAL_ADD_FAVORITE = 5;

    private ActivityRequestCodes() {
    }
}
