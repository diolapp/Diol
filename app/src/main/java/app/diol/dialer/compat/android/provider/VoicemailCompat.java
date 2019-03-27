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
package app.diol.dialer.compat.android.provider;

/**
 * Provide access to Voicemail Transcription API constants as they won't be publicly available.
 *
 * <p>Copied from android.provider.VoicemailContract.Voicemails. These do not plan to become public
 * in O-MR1 or in the near future.
 */
public class VoicemailCompat {

    /**
     * The state of the voicemail transcription.
     *
     * <p>Possible values: {@link #TRANSCRIPTION_NOT_STARTED}, {@link #TRANSCRIPTION_IN_PROGRESS},
     * {@link #TRANSCRIPTION_FAILED}, {@link #TRANSCRIPTION_AVAILABLE}.
     *
     * <p>Type: INTEGER
     */
    public static final String TRANSCRIPTION_STATE = "transcription_state";

    /**
     * Value of {@link #TRANSCRIPTION_STATE} when the voicemail transcription has not yet been
     * attempted.
     */
    public static final int TRANSCRIPTION_NOT_STARTED = 0;

    /**
     * Value of {@link #TRANSCRIPTION_STATE} when the voicemail transcription has begun but is not yet
     * complete.
     */
    public static final int TRANSCRIPTION_IN_PROGRESS = 1;

    /**
     * Value of {@link #TRANSCRIPTION_STATE} when the voicemail transcription has been attempted and
     * failed for an unspecified reason.
     */
    public static final int TRANSCRIPTION_FAILED = 2;

    /**
     * Value of {@link #TRANSCRIPTION_STATE} when the voicemail transcription has completed and the
     * result has been stored in the {@link #TRANSCRIPTION} column.
     */
    public static final int TRANSCRIPTION_AVAILABLE = 3;

    /**
     * Value of {@link #TRANSCRIPTION_STATE} when the voicemail transcription has been attempted and
     * failed because no speech was detected.
     *
     * <p>Internal dialer use only, not part of the public SDK.
     */
    public static final int TRANSCRIPTION_FAILED_NO_SPEECH_DETECTED = -1;

    /**
     * Value of {@link #TRANSCRIPTION_STATE} when the voicemail transcription has been attempted and
     * failed because the language was not supported.
     *
     * <p>Internal dialer use only, not part of the public SDK.
     */
    public static final int TRANSCRIPTION_FAILED_LANGUAGE_NOT_SUPPORTED = -2;

    /**
     * Value of {@link #TRANSCRIPTION_STATE} when the voicemail transcription has completed and the
     * result has been stored in the {@link #TRANSCRIPTION} column of the database, and the user has
     * provided a quality rating for the transcription.
     */
    public static final int TRANSCRIPTION_AVAILABLE_AND_RATED = -3;

    /**
     * Voicemail transcription quality rating value sent to the server indicating a good transcription
     */
    public static final int TRANSCRIPTION_QUALITY_RATING_GOOD = 1;

    /**
     * Voicemail transcription quality rating value sent to the server indicating a bad transcription
     */
    public static final int TRANSCRIPTION_QUALITY_RATING_BAD = 2;
}
