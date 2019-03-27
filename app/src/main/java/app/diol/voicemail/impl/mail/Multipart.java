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

import java.util.ArrayList;

public abstract class Multipart implements Body {
    protected Part parent;

    protected ArrayList<BodyPart> parts = new ArrayList<BodyPart>();

    protected String contentType;

    public void addBodyPart(BodyPart part) throws MessagingException {
        parts.add(part);
    }

    public void addBodyPart(BodyPart part, int index) throws MessagingException {
        parts.add(index, part);
    }

    public BodyPart getBodyPart(int index) throws MessagingException {
        return parts.get(index);
    }

    public String getContentType() throws MessagingException {
        return contentType;
    }

    public int getCount() throws MessagingException {
        return parts.size();
    }

    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        return parts.remove(part);
    }

    public void removeBodyPart(int index) throws MessagingException {
        parts.remove(index);
    }

    public Part getParent() throws MessagingException {
        return parent;
    }

    public void setParent(Part parent) throws MessagingException {
        this.parent = parent;
    }
}
