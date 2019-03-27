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
 * Keys used to lookup carrier specific configuration strings. See {@code
 * VoicemailClient.getCarrierConfigString}
 */
public interface CarrierConfigKeys {

    /**
     * Carrier config key whose value will be 'true' for carriers that allow over the top voicemail
     * transcription.
     */
    String VVM_CARRIER_ALLOWS_OTT_TRANSCRIPTION_STRING =
            "vvm_carrier_allows_ott_transcription_string";
}
