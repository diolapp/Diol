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

import com.google.internal.communications.voicemailtranscription.v1.TranscribeVoicemailResponse;

import app.diol.dialer.common.Assert;
import io.grpc.Status;

/**
 * Container for response and status objects for a synchronous transcription
 * request
 */
public class TranscriptionResponseSync extends TranscriptionResponse {
    @Nullable
    private final TranscribeVoicemailResponse response;

    @VisibleForTesting
    public TranscriptionResponseSync(Status status) {
        super(status);
        this.response = null;
    }

    @VisibleForTesting
    public TranscriptionResponseSync(TranscribeVoicemailResponse response) {
        Assert.checkArgument(response != null);
        this.response = response;
    }

    public @Nullable
    String getTranscript() {
        return (response != null) ? response.getTranscript() : null;
    }

    @Override
    public String toString() {
        return super.toString() + ", response: " + response;
    }
}
