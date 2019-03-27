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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import app.diol.voicemail.impl.mail.BodyPart;
import app.diol.voicemail.impl.mail.MessagingException;
import app.diol.voicemail.impl.mail.Multipart;

public class MimeMultipart extends Multipart {
    protected String preamble;

    protected String contentType;

    protected String boundary;

    protected String subType;

    public MimeMultipart() throws MessagingException {
        boundary = generateBoundary();
        setSubType("mixed");
    }

    public MimeMultipart(String contentType) throws MessagingException {
        this.contentType = contentType;
        try {
            subType = MimeUtility.getHeaderParameter(contentType, null).split("/")[1];
            boundary = MimeUtility.getHeaderParameter(contentType, "boundary");
            if (boundary == null) {
                throw new MessagingException("MultiPart does not contain boundary: " + contentType);
            }
        } catch (Exception e) {
            throw new MessagingException(
                    "Invalid MultiPart Content-Type; must contain subtype and boundary. ("
                            + contentType
                            + ")",
                    e);
        }
    }

    public String generateBoundary() {
        StringBuffer sb = new StringBuffer();
        sb.append("----");
        for (int i = 0; i < 30; i++) {
            sb.append(Integer.toString((int) (Math.random() * 35), 36));
        }
        return sb.toString().toUpperCase();
    }

    public String getPreamble() throws MessagingException {
        return preamble;
    }

    public void setPreamble(String preamble) throws MessagingException {
        this.preamble = preamble;
    }

    @Override
    public String getContentType() throws MessagingException {
        return contentType;
    }

    public void setSubType(String subType) throws MessagingException {
        this.subType = subType;
        contentType = String.format("multipart/%s; boundary=\"%s\"", subType, boundary);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException, MessagingException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);

        if (preamble != null) {
            writer.write(preamble + "\r\n");
        }

        for (int i = 0, count = parts.size(); i < count; i++) {
            BodyPart bodyPart = parts.get(i);
            writer.write("--" + boundary + "\r\n");
            writer.flush();
            bodyPart.writeTo(out);
            writer.write("\r\n");
        }

        writer.write("--" + boundary + "--\r\n");
        writer.flush();
    }

    @Override
    public InputStream getInputStream() throws MessagingException {
        return null;
    }

    public String getSubTypeForTest() {
        return subType;
    }
}
