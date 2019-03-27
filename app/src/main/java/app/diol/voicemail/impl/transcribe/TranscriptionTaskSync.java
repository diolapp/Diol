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
package app.diol.voicemail.impl.transcribe;

import android.app.job.JobWorkItem;
import android.content.Context;
import android.util.Pair;

import com.google.internal.communications.voicemailtranscription.v1.TranscribeVoicemailRequest;
import com.google.internal.communications.voicemailtranscription.v1.TranscriptionStatus;

import app.diol.dialer.logging.DialerImpression;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.transcribe.TranscriptionService.JobCallback;
import app.diol.voicemail.impl.transcribe.grpc.TranscriptionClientFactory;
import app.diol.voicemail.impl.transcribe.grpc.TranscriptionResponseSync;

/**
 * Background task to get a voicemail transcription using the synchronous API
 */
public class TranscriptionTaskSync extends TranscriptionTask {
    private static final String TAG = "TranscriptionTaskSync";

    public TranscriptionTaskSync(Context context, JobCallback callback, JobWorkItem workItem,
                                 TranscriptionClientFactory clientFactory, TranscriptionConfigProvider configProvider) {
        super(context, callback, workItem, clientFactory, configProvider);
    }

    @Override
    protected Pair<String, TranscriptionStatus> getTranscription() {
        VvmLog.i(TAG, "getTranscription");

        TranscriptionResponseSync response = (TranscriptionResponseSync) sendRequest(
                (client) -> client.sendSyncRequest(getSyncRequest()));
        if (response == null) {
            VvmLog.i(TAG, "getTranscription, failed to transcribe voicemail.");
            return new Pair<>(null, TranscriptionStatus.FAILED_NO_RETRY);
        } else {
            VvmLog.i(TAG, "getTranscription, got transcription");
            return new Pair<>(response.getTranscript(), TranscriptionStatus.SUCCESS);
        }
    }

    @Override
    protected DialerImpression.Type getRequestSentImpression() {
        return DialerImpression.Type.VVM_TRANSCRIPTION_REQUEST_SENT;
    }

    private TranscribeVoicemailRequest getSyncRequest() {
        return TranscribeVoicemailRequest.newBuilder().setVoicemailData(audioData).setAudioFormat(encoding).build();
    }
}
