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
package app.diol.voicemail.impl.transcribe.grpc;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.internal.communications.voicemailtranscription.v1.GetTranscriptResponse;
import com.google.internal.communications.voicemailtranscription.v1.TranscriptionStatus;

import app.diol.dialer.common.Assert;
import io.grpc.Status;

/**
 * Container for response and status objects for an asynchronous get-transcript
 * request
 */
public class GetTranscriptResponseAsync extends TranscriptionResponse {
    @Nullable
    private final GetTranscriptResponse response;

    @VisibleForTesting
    public GetTranscriptResponseAsync(GetTranscriptResponse response) {
        Assert.checkArgument(response != null);
        this.response = response;
    }

    @VisibleForTesting
    public GetTranscriptResponseAsync(Status status) {
        super(status);
        this.response = null;
    }

    public @Nullable
    String getTranscript() {
        if (response != null) {
            return response.getTranscript();
        }
        return null;
    }

    public @Nullable
    String getErrorDescription() {
        if (!hasRecoverableError() && !hasFatalError()) {
            return null;
        }
        if (status != null) {
            return "Grpc error: " + status;
        }
        if (response != null) {
            return "Transcription error: " + response.getStatus();
        }
        Assert.fail("Impossible state");
        return null;
    }

    public TranscriptionStatus getTranscriptionStatus() {
        if (response == null) {
            return TranscriptionStatus.TRANSCRIPTION_STATUS_UNSPECIFIED;
        } else {
            return response.getStatus();
        }
    }

    public boolean isTranscribing() {
        return response != null && response.getStatus() == TranscriptionStatus.PENDING;
    }

    @Override
    public boolean hasRecoverableError() {
        if (super.hasRecoverableError()) {
            return true;
        }

        if (response != null) {
            return response.getStatus() == TranscriptionStatus.EXPIRED
                    || response.getStatus() == TranscriptionStatus.FAILED_RETRY;
        }

        return false;
    }

    @Override
    public boolean hasFatalError() {
        if (super.hasFatalError()) {
            return true;
        }

        if (response != null) {
            return response.getStatus() == TranscriptionStatus.FAILED_NO_RETRY
                    || response.getStatus() == TranscriptionStatus.FAILED_LANGUAGE_NOT_SUPPORTED
                    || response.getStatus() == TranscriptionStatus.FAILED_NO_SPEECH_DETECTED;
        }

        return false;
    }
}
