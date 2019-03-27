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
import android.support.annotation.VisibleForTesting;
import android.util.Pair;

import com.google.internal.communications.voicemailtranscription.v1.DonationPreference;
import com.google.internal.communications.voicemailtranscription.v1.TranscribeVoicemailAsyncRequest;
import com.google.internal.communications.voicemailtranscription.v1.TranscriptionStatus;

import app.diol.dialer.logging.DialerImpression;
import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.transcribe.TranscriptionService.JobCallback;
import app.diol.voicemail.impl.transcribe.grpc.TranscriptionClientFactory;
import app.diol.voicemail.impl.transcribe.grpc.TranscriptionResponseAsync;

/**
 * Background task to get a voicemail transcription using the asynchronous API.
 * The async API works as follows:
 *
 * <ol>
 * <li>client uploads voicemail data to the server
 * <li>server responds with a transcription-id and an estimated transcription
 * wait time
 * <li>client waits appropriate amount of time then begins polling for the
 * result
 * </ol>
 * <p>
 * This implementation blocks until the response or an error is received, even
 * though it is using the asynchronous server API.
 */
public class TranscriptionTaskAsync extends TranscriptionTask {
    private static final String TAG = "TranscriptionTaskAsync";

    public TranscriptionTaskAsync(Context context, JobCallback callback, JobWorkItem workItem,
                                  TranscriptionClientFactory clientFactory, TranscriptionConfigProvider configProvider) {
        super(context, callback, workItem, clientFactory, configProvider);
    }

    @Override
    protected Pair<String, TranscriptionStatus> getTranscription() {
        VvmLog.i(TAG, "getTranscription");

        if (GetTranscriptReceiver.hasPendingAlarm(context)) {
            // Don't start a transcription while another is still active
            VvmLog.i(TAG, "getTranscription, pending transcription, postponing transcription of: " + voicemailUri);
            return new Pair<>(null, null);
        }

        TranscribeVoicemailAsyncRequest uploadRequest = getUploadRequest();
        VvmLog.i(TAG,
                "getTranscription, uploading voicemail: " + voicemailUri + ", id: " + uploadRequest.getTranscriptionId());
        TranscriptionResponseAsync uploadResponse = (TranscriptionResponseAsync) sendRequest(
                (client) -> client.sendUploadRequest(uploadRequest));

        if (cancelled) {
            VvmLog.i(TAG, "getTranscription, cancelled.");
            return new Pair<>(null, TranscriptionStatus.FAILED_NO_RETRY);
        } else if (uploadResponse == null) {
            VvmLog.i(TAG, "getTranscription, failed to upload voicemail.");
            return new Pair<>(null, TranscriptionStatus.FAILED_NO_RETRY);
        } else if (uploadResponse.isStatusAlreadyExists()) {
            VvmLog.i(TAG, "getTranscription, transcription already exists.");
            GetTranscriptReceiver.beginPolling(context, voicemailUri, uploadRequest.getTranscriptionId(), 0, configProvider,
                    phoneAccountHandle);
            return new Pair<>(null, null);
        } else if (uploadResponse.getTranscriptionId() == null) {
            VvmLog.i(TAG, "getTranscription, upload error: " + uploadResponse.status);
            return new Pair<>(null, TranscriptionStatus.FAILED_NO_RETRY);
        } else {
            VvmLog.i(TAG, "getTranscription, begin polling for: " + uploadResponse.getTranscriptionId());
            GetTranscriptReceiver.beginPolling(context, voicemailUri, uploadResponse.getTranscriptionId(),
                    uploadResponse.getEstimatedWaitMillis(), configProvider, phoneAccountHandle);
            // This indicates that the result is not available yet
            return new Pair<>(null, null);
        }
    }

    @Override
    protected DialerImpression.Type getRequestSentImpression() {
        return DialerImpression.Type.VVM_TRANSCRIPTION_REQUEST_SENT_ASYNC;
    }

    @VisibleForTesting
    TranscribeVoicemailAsyncRequest getUploadRequest() {
        TranscribeVoicemailAsyncRequest.Builder builder = TranscribeVoicemailAsyncRequest.newBuilder()
                .setVoicemailData(audioData).setAudioFormat(encoding)
                .setDonationPreference(isDonationEnabled() ? DonationPreference.DONATE : DonationPreference.DO_NOT_DONATE);
        // Generate the transcript id locally if configured to do so, or if voicemail
        // donation is
        // available (because rating donating voicemails requires locally generated
        // voicemail ids).
        if (configProvider.useClientGeneratedVoicemailIds() || VoicemailComponent.get(context).getVoicemailClient()
                .isVoicemailDonationAvailable(context, phoneAccountHandle)) {
            // The server currently can't handle repeated transcription id's so if we add
            // the Uri to the
            // fingerprint (which contains the voicemail id) which is different each time a
            // voicemail is
            // downloaded. If this becomes a problem then it should be possible to change
            // the server
            // behavior to allow id's to be re-used, a bug
            String salt = voicemailUri.toString();
            builder.setTranscriptionId(TranscriptionUtils.getFingerprintFor(audioData, salt));
        }
        return builder.build();
    }

    private boolean isDonationEnabled() {
        return phoneAccountHandle != null
                && VoicemailComponent.get(context).getVoicemailClient().isVoicemailDonationEnabled(context, phoneAccountHandle);
    }
}
