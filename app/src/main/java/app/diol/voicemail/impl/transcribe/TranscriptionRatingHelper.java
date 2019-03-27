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

import android.content.Context;
import android.net.Uri;

import com.google.internal.communications.voicemailtranscription.v1.SendTranscriptionFeedbackRequest;
import com.google.internal.communications.voicemailtranscription.v1.TranscriptionRating;
import com.google.internal.communications.voicemailtranscription.v1.TranscriptionRatingValue;
import com.google.protobuf.ByteString;

import app.diol.dialer.common.concurrent.DialerExecutor;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.compat.android.provider.VoicemailCompat;

/**
 * Send voicemail transcription rating feedback to the server and record the
 * fact that feedback was provided in the local database.
 */
public class TranscriptionRatingHelper {

    /**
     * Method for sending a user voicemail transcription feedback rating to the
     * server and recording the fact that the voicemail was rated in the local
     * database.
     */
    public static void sendRating(Context context, TranscriptionRatingValue ratingValue, Uri voicemailUri,
                                  SuccessListener successListener, FailureListener failureListener) {
        DialerExecutorComponent.get(context).dialerExecutorFactory()
                .createNonUiTaskBuilder(new RatingWorker(context, ratingValue, voicemailUri))
                .onSuccess(output -> successListener.onRatingSuccess(voicemailUri))
                .onFailure(e -> failureListener.onRatingFailure(e)).build().executeParallel(null);
    }

    /**
     * Callback invoked after the feedback has been recorded locally
     */
    public interface SuccessListener {
        void onRatingSuccess(Uri voicemailUri);
    }

    /**
     * Callback invoked if there was an error recording the feedback
     */
    public interface FailureListener {
        void onRatingFailure(Throwable t);
    }

    /**
     * Worker class used to record a user's quality rating of a voicemail
     * transcription.
     */
    private static class RatingWorker implements DialerExecutor.Worker<Void, Void> {
        private final Context context;
        private final TranscriptionRatingValue ratingValue;
        private final Uri voicemailUri;

        private RatingWorker(Context context, TranscriptionRatingValue ratingValue, Uri voicemailUri) {
            this.context = context;
            this.ratingValue = ratingValue;
            this.voicemailUri = voicemailUri;
        }

        @Override
        public Void doInBackground(Void input) {
            // Schedule a task to upload the feedback (requires network connectivity)
            TranscriptionRatingService.scheduleTask(context, getFeedbackRequest());

            // Record the fact that the transcription has been rated
            TranscriptionDbHelper dbHelper = new TranscriptionDbHelper(context, voicemailUri);
            dbHelper.setTranscriptionState(VoicemailCompat.TRANSCRIPTION_AVAILABLE_AND_RATED);
            return null;
        }

        private SendTranscriptionFeedbackRequest getFeedbackRequest() {
            ByteString audioData = TranscriptionUtils.getAudioData(context, voicemailUri);
            String salt = voicemailUri.toString();
            String voicemailId = TranscriptionUtils.getFingerprintFor(audioData, salt);
            TranscriptionRating rating = TranscriptionRating.newBuilder().setTranscriptionId(voicemailId)
                    .setRatingValue(ratingValue).build();
            return SendTranscriptionFeedbackRequest.newBuilder().addRating(rating).build();
        }
    }
}
