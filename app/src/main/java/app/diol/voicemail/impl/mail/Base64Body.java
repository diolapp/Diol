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
package app.diol.voicemail.impl.mail;

import android.util.Base64;
import android.util.Base64OutputStream;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Base64Body implements Body {
    private final InputStream source;
    // Because we consume the input stream, we can only write out once
    private boolean alreadyWritten;

    public Base64Body(InputStream source) {
        this.source = source;
    }

    @Override
    public InputStream getInputStream() throws MessagingException {
        return source;
    }

    /**
     * This method consumes the input stream, so can only be called once
     *
     * @param out Stream to write to
     * @throws IllegalStateException If called more than once
     * @throws IOException
     * @throws MessagingException
     */
    @Override
    public void writeTo(OutputStream out) throws IllegalStateException, IOException, MessagingException {
        if (alreadyWritten) {
            throw new IllegalStateException("Base64Body can only be written once");
        }
        alreadyWritten = true;
        try {
            final Base64OutputStream b64out = new Base64OutputStream(out, Base64.DEFAULT);
            IOUtils.copyLarge(source, b64out);
        } finally {
            source.close();
        }
    }
}
