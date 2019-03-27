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

import com.google.internal.communications.voicemailtranscription.v1.TranscribeVoicemailAsyncResponse;

import app.diol.dialer.common.Assert;
import io.grpc.Status;

/**
 * Container for response and status objects for an asynchronous transcription
 * upload request
 */
public class TranscriptionResponseAsync extends TranscriptionResponse {
    @Nullable
    private final TranscribeVoicemailAsyncResponse response;

    @VisibleForTesting
    public TranscriptionResponseAsync(TranscribeVoicemailAsyncResponse response) {
        Assert.checkArgument(response != null);
        this.response = response;
    }

    @VisibleForTesting
    public TranscriptionResponseAsync(Status status) {
        super(status);
        this.response = null;
    }

    public @Nullable
    String getTranscriptionId() {
        if (response != null) {
            return response.getTranscriptionId();
        }
        return null;
    }

    public long getEstimatedWaitMillis() {
        if (response != null) {
            return response.getEstimatedWaitSecs() * 1_000L;
        }
        return 0;
    }

    @Override
    public String toString() {
        return super.toString() + ", response: " + response;
    }
}
