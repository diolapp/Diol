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
import java.io.InputStream;

/**
 * A filtering InputStream that stops allowing reads after the given length has
 * been read. This is used to allow a client to read directly from an underlying
 * protocol stream without reading past where the protocol handler intended the
 * client to read.
 */
public class FixedLengthInputStream extends InputStream {
    private final InputStream in;
    private final int length;
    private int count;

    public FixedLengthInputStream(InputStream in, int length) {
        this.in = in;
        this.length = length;
    }

    @Override
    public int available() throws IOException {
        return length - count;
    }

    @Override
    public int read() throws IOException {
        if (count < length) {
            count++;
            return in.read();
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        if (count < this.length) {
            int d = in.read(b, offset, Math.min(this.length - count, length));
            if (d == -1) {
                return -1;
            } else {
                count += d;
                return d;
            }
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return String.format("FixedLengthInputStream(in=%s, length=%d)", in.toString(), length);
    }
}
