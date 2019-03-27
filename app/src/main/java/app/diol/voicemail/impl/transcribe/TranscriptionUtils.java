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

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.google.internal.communications.voicemailtranscription.v1.AudioFormat;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import app.diol.dialer.common.Assert;

/**
 * Utility methods used by this transcription package.
 */
public class TranscriptionUtils {
    static final String AMR_PREFIX = "#!AMR\n";

    static ByteString getAudioData(Context context, Uri voicemailUri) {
        try (InputStream in = context.getContentResolver().openInputStream(voicemailUri)) {
            return ByteString.readFrom(in);
        } catch (IOException e) {
            return null;
        }
    }

    static AudioFormat getAudioFormat(ByteString audioData) {
        return audioData != null && audioData.startsWith(ByteString.copyFromUtf8(AMR_PREFIX)) ? AudioFormat.AMR_NB_8KHZ
                : AudioFormat.AUDIO_FORMAT_UNSPECIFIED;
    }

    @TargetApi(VERSION_CODES.O)
    static String getFingerprintFor(ByteString data, @Nullable String salt) {
        Assert.checkArgument(data != null);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (salt != null) {
                md.update(salt.getBytes());
            }
            byte[] md5Bytes = md.digest(data.toByteArray());
            return Base64.encodeToString(md5Bytes, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.toString());
        }
        return null;
    }
}
