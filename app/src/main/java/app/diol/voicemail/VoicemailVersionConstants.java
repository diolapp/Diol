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

package app.diol.voicemail;

/**
 * Shared preference keys and values relating to the voicemail version that the user has accepted.
 * Note: these can be carrier dependent.
 */
public interface VoicemailVersionConstants {
    // Preference key to check which version of the Verizon ToS that the user has accepted.
    String PREF_VVM3_TOS_VERSION_ACCEPTED_KEY = "vvm3_tos_version_accepted";

    // Preference key to check which version of the Google Dialer ToS that the user has accepted.
    String PREF_DIALER_TOS_VERSION_ACCEPTED_KEY = "dialer_tos_version_accepted";

    // Preference key to check which feature version the user has acknowledged
    String PREF_DIALER_FEATURE_VERSION_ACKNOWLEDGED_KEY = "dialer_feature_version_acknowledged";

    int CURRENT_VVM3_TOS_VERSION = 2;
    int CURRENT_DIALER_TOS_VERSION = 1;
    int LEGACY_VOICEMAIL_FEATURE_VERSION = 1; // original visual voicemail
    int TRANSCRIPTION_VOICEMAIL_FEATURE_VERSION = 2;
    int CURRENT_VOICEMAIL_FEATURE_VERSION = TRANSCRIPTION_VOICEMAIL_FEATURE_VERSION;
}
