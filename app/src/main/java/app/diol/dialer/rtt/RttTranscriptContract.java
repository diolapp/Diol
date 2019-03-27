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

package app.diol.dialer.rtt;

/**
 * Contract for the RTT transcript database.
 */
public final class RttTranscriptContract {

    /**
     * Columns for RTT transcript.
     */
    static final class RttTranscriptColumn {

        /**
         * Unique key that should match {@link android.provider.CallLog.Calls#DATE} of the data row it
         * is associated with.
         *
         * <p>TYPE: TEXT
         */
        static final String TRANSCRIPT_ID = "rtt_transcript_id";

        /**
         * Transcript data, encoded as {@link RttTranscript} proto.
         *
         * <p>TYPE: BLOB
         */
        static final String TRANSCRIPT_DATA = "transcript_data";
    }
}
