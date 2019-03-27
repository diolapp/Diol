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

import java.io.IOException;
import java.io.OutputStream;

public interface Part extends Fetchable {
    public void addHeader(String name, String value) throws MessagingException;

    public void removeHeader(String name) throws MessagingException;

    public void setHeader(String name, String value) throws MessagingException;

    public Body getBody() throws MessagingException;

    public void setBody(Body body) throws MessagingException;

    public String getContentType() throws MessagingException;

    public String getDisposition() throws MessagingException;

    public String getContentId() throws MessagingException;

    public String[] getHeader(String name) throws MessagingException;

    public void setExtendedHeader(String name, String value) throws MessagingException;

    public String getExtendedHeader(String name) throws MessagingException;

    public int getSize() throws MessagingException;

    public boolean isMimeType(String mimeType) throws MessagingException;

    public String getMimeType() throws MessagingException;

    public void writeTo(OutputStream out) throws IOException, MessagingException;
}
