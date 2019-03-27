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

package app.diol.voicemail.impl.mail.store.imap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.mail.FixedLengthInputStream;

/**
 * Subclass of {@link ImapString} used for literals backed by an in-memory byte
 * array.
 */
public class ImapMemoryLiteral extends ImapString {
    private final String TAG = "ImapMemoryLiteral";
    private byte[] data;

    /* package */ ImapMemoryLiteral(FixedLengthInputStream in) throws IOException {
        // We could use ByteArrayOutputStream and IOUtils.copy, but it'd perform an
        // unnecessary
        // copy....
        data = new byte[in.getLength()];
        int pos = 0;
        while (pos < data.length) {
            int read = in.read(data, pos, data.length - pos);
            if (read < 0) {
                break;
            }
            pos += read;
        }
        if (pos != data.length) {
            VvmLog.w(TAG, "length mismatch");
        }
    }

    @Override
    public void destroy() {
        data = null;
        super.destroy();
    }

    @Override
    public String getString() {
        try {
            return new String(data, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            VvmLog.e(TAG, "Unsupported encoding: ", e);
        }
        return null;
    }

    @Override
    public InputStream getAsStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public String toString() {
        return String.format("{%d byte literal(memory)}", data.length);
    }
}
