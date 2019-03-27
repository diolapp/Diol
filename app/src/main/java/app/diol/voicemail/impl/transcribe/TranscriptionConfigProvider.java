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
import android.os.Build;

import java.util.concurrent.TimeUnit;

import app.diol.dialer.configprovider.ConfigProviderComponent;

/**
 * Provides configuration values needed to connect to the transcription server.
 */
public class TranscriptionConfigProvider {
    private final Context context;

    public TranscriptionConfigProvider(Context context) {
        this.context = context;
    }

    public boolean isVoicemailTranscriptionAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ConfigProviderComponent.get(context).getConfigProvider()
                .getBoolean("voicemail_transcription_available", false);
    }

    public String getServerAddress() {
        // Private voicemail transcription service
        return ConfigProviderComponent.get(context).getConfigProvider().getString("voicemail_transcription_server_address",
                "voicemailtranscription-pa.googleapis.com");
    }

    public String getApiKey() {
        // Android API key restricted to com.google.android.dialer
        return ConfigProviderComponent.get(context).getConfigProvider().getString("voicemail_transcription_client_api_key",
                "AIzaSyAXdDnif6B7sBYxU8hzw9qAp3pRPVHs060");
    }

    public String getAuthToken() {
        return null;
    }

    public boolean shouldUsePlaintext() {
        return ConfigProviderComponent.get(context).getConfigProvider()
                .getBoolean("voicemail_transcription_server_use_plaintext", false);
    }

    public boolean shouldUseSyncApi() {
        return ConfigProviderComponent.get(context).getConfigProvider()
                .getBoolean("voicemail_transcription_server_use_sync_api", false);
    }

    public long getMaxTranscriptionRetries() {
        return ConfigProviderComponent.get(context).getConfigProvider()
                .getLong("voicemail_transcription_max_transcription_retries", 2L);
    }

    public int getMaxGetTranscriptPolls() {
        return (int) ConfigProviderComponent.get(context).getConfigProvider()
                .getLong("voicemail_transcription_max_get_transcript_polls", 20L);
    }

    public long getInitialGetTranscriptPollDelayMillis() {
        return ConfigProviderComponent.get(context).getConfigProvider()
                .getLong("voicemail_transcription_get_initial_transcript_poll_delay_millis", TimeUnit.SECONDS.toMillis(1));
    }

    public long getMaxGetTranscriptPollTimeMillis() {
        return ConfigProviderComponent.get(context).getConfigProvider()
                .getLong("voicemail_transcription_get_max_transcript_poll_time_millis", TimeUnit.MINUTES.toMillis(20));
    }

    public boolean isVoicemailDonationAvailable() {
        return ConfigProviderComponent.get(context).getConfigProvider()
                .getBoolean("voicemail_transcription_donation_available", false);
    }

    public boolean useClientGeneratedVoicemailIds() {
        return ConfigProviderComponent.get(context).getConfigProvider()
                .getBoolean("voicemail_transcription_client_generated_voicemail_ids", false);
    }

    @Override
    public String toString() {
        return String.format(
                "{ address: %s, api key: %s, auth token: %s, plaintext: %b, sync: %b, retries: %d, polls:"
                        + " %d, poll ms: %d }",
                getServerAddress(), getApiKey(), getAuthToken(), shouldUsePlaintext(), shouldUseSyncApi(),
                getMaxTranscriptionRetries(), getMaxGetTranscriptPolls(), getMaxGetTranscriptPollTimeMillis());
    }
}
