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

import android.support.annotation.WorkerThread;

import com.google.internal.communications.voicemailtranscription.v1.GetTranscriptRequest;
import com.google.internal.communications.voicemailtranscription.v1.SendTranscriptionFeedbackRequest;
import com.google.internal.communications.voicemailtranscription.v1.TranscribeVoicemailAsyncRequest;
import com.google.internal.communications.voicemailtranscription.v1.TranscribeVoicemailRequest;
import com.google.internal.communications.voicemailtranscription.v1.VoicemailTranscriptionServiceGrpc;

import io.grpc.StatusRuntimeException;

/**
 * Wrapper around Grpc transcription server stub
 */
public class TranscriptionClient {

    private final VoicemailTranscriptionServiceGrpc.VoicemailTranscriptionServiceBlockingStub stub;

    TranscriptionClient(VoicemailTranscriptionServiceGrpc.VoicemailTranscriptionServiceBlockingStub stub) {
        this.stub = stub;
    }

    @WorkerThread
    public TranscriptionResponseSync sendSyncRequest(TranscribeVoicemailRequest request) {
        try {
            return new TranscriptionResponseSync(stub.transcribeVoicemail(request));
        } catch (StatusRuntimeException e) {
            return new TranscriptionResponseSync(e.getStatus());
        }
    }

    @WorkerThread
    public TranscriptionResponseAsync sendUploadRequest(TranscribeVoicemailAsyncRequest request) {
        try {
            return new TranscriptionResponseAsync(stub.transcribeVoicemailAsync(request));
        } catch (StatusRuntimeException e) {
            return new TranscriptionResponseAsync(e.getStatus());
        }
    }

    @WorkerThread
    public GetTranscriptResponseAsync sendGetTranscriptRequest(GetTranscriptRequest request) {
        try {
            return new GetTranscriptResponseAsync(stub.getTranscript(request));
        } catch (StatusRuntimeException e) {
            return new GetTranscriptResponseAsync(e.getStatus());
        }
    }

    @WorkerThread
    public TranscriptionFeedbackResponseAsync sendTranscriptFeedbackRequest(SendTranscriptionFeedbackRequest request) {
        try {
            return new TranscriptionFeedbackResponseAsync(stub.sendTranscriptionFeedback(request));
        } catch (StatusRuntimeException e) {
            return new TranscriptionFeedbackResponseAsync(e.getStatus());
        }
    }
}
