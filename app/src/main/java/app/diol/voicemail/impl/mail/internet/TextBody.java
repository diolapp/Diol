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
package app.diol.voicemail.impl.mail.internet;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import app.diol.voicemail.impl.mail.Body;
import app.diol.voicemail.impl.mail.MessagingException;

public class TextBody implements Body {
    String body;

    public TextBody(String body) {
        this.body = body;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException, MessagingException {
        byte[] bytes = body.getBytes("UTF-8");
        out.write(Base64.encode(bytes, Base64.CRLF));
    }

    /**
     * Get the text of the body in it's unencoded format.
     *
     * @return
     */
    public String getText() {
        return body;
    }

    /**
     * Returns an InputStream that reads this body's text in UTF-8 format.
     */
    @Override
    public InputStream getInputStream() throws MessagingException {
        try {
            byte[] b = body.getBytes("UTF-8");
            return new ByteArrayInputStream(b);
        } catch (UnsupportedEncodingException usee) {
            return null;
        }
    }
}
