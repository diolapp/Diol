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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import app.diol.voicemail.impl.VvmLog;

/**
 * Subclass of {@link ImapString} used for non literals.
 */
public class ImapSimpleString extends ImapString {
    private final String TAG = "ImapSimpleString";
    private String string;

    /* package */ ImapSimpleString(String string) {
        this.string = (string != null) ? string : "";
    }

    @Override
    public void destroy() {
        string = null;
        super.destroy();
    }

    @Override
    public String getString() {
        return string;
    }

    @Override
    public InputStream getAsStream() {
        try {
            return new ByteArrayInputStream(string.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            VvmLog.e(TAG, "Unsupported encoding: ", e);
        }
        return null;
    }

    @Override
    public String toString() {
        // Purposefully not return just mString, in order to prevent using it instead of
        // getString.
        return "\"" + string + "\"";
    }
}
