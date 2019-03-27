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
package app.diol.voicemail.impl.fetch;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.VoicemailContract.Voicemails;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.concurrent.ThreadUtil;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.imap.VoicemailPayload;
import app.diol.voicemail.impl.transcribe.TranscriptionService;

/**
 * Callback for when a voicemail payload is fetched. It copies the returned
 * stream to the data file corresponding to the voicemail.
 */
public class VoicemailFetchedCallback {
    private static final String TAG = "VoicemailFetchedCallback";

    private final Context context;
    private final ContentResolver contentResolver;
    private final Uri uri;
    private final PhoneAccountHandle phoneAccountHandle;

    public VoicemailFetchedCallback(Context context, Uri uri, PhoneAccountHandle phoneAccountHandle) {
        this.context = context;
        contentResolver = context.getContentResolver();
        this.uri = uri;
        this.phoneAccountHandle = phoneAccountHandle;
    }

    /**
     * Saves the voicemail payload data into the voicemail provider then sets the
     * "has_content" bit of the voicemail to "1".
     *
     * @param voicemailPayload The object containing the content data for the
     *                         voicemail
     */
    public void setVoicemailContent(@Nullable VoicemailPayload voicemailPayload) {
        Assert.isWorkerThread();
        if (voicemailPayload == null) {
            VvmLog.i(TAG, "Payload not found, message has unsupported format");
            ContentValues values = new ContentValues();
            values.put(Voicemails.TRANSCRIPTION, context.getString(R.string.vvm_unsupported_message_format,
                    context.getSystemService(TelecomManager.class).getVoiceMailNumber(phoneAccountHandle)));
            updateVoicemail(values);
            return;
        }

        VvmLog.d(TAG, String.format("Writing new voicemail content: %s", uri));
        OutputStream outputStream = null;

        try {
            outputStream = contentResolver.openOutputStream(uri);
            byte[] inputBytes = voicemailPayload.getBytes();
            if (inputBytes != null) {
                outputStream.write(inputBytes);
            }
        } catch (IOException e) {
            VvmLog.w(TAG, String.format("File not found for %s", uri));
            return;
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        // Update mime_type & has_content after we are done with file update.
        ContentValues values = new ContentValues();
        values.put(Voicemails.MIME_TYPE, voicemailPayload.getMimeType());
        values.put(Voicemails.HAS_CONTENT, true);
        if (updateVoicemail(values)) {
            ThreadUtil.postOnUiThread(() -> {
                if (!TranscriptionService.scheduleNewVoicemailTranscriptionJob(context, uri, phoneAccountHandle, true)) {
                    VvmLog.w(TAG, String.format("Failed to schedule transcription for %s", uri));
                }
            });
        }
    }

    private boolean updateVoicemail(ContentValues values) {
        int updatedCount = contentResolver.update(uri, values, null, null);
        if (updatedCount != 1) {
            VvmLog.e(TAG, "Updating voicemail should have updated 1 row, was: " + updatedCount);
            return false;
        } else {
            return true;
        }
    }
}
