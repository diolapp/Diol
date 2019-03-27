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
 * A filtering InputStream that allows single byte "peeks" without consuming the byte. The client of
 * this stream can call peek() to see the next available byte in the stream and a subsequent read
 * will still return the peeked byte.
 */
public class PeekableInputStream extends InputStream {
    private final InputStream in;
    private boolean peeked;
    private int peekedByte;

    public PeekableInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        if (!peeked) {
            return in.read();
        } else {
            peeked = false;
            return peekedByte;
        }
    }

    public int peek() throws IOException {
        if (!peeked) {
            peekedByte = read();
            peeked = true;
        }
        return peekedByte;
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        if (!peeked) {
            return in.read(b, offset, length);
        } else {
            b[0] = (byte) peekedByte;
            peeked = false;
            int r = in.read(b, offset + 1, length - 1);
            if (r == -1) {
                return 1;
            } else {
                return r + 1;
            }
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public String toString() {
        return String.format(
                "PeekableInputStream(in=%s, peeked=%b, peekedByte=%d)", in.toString(), peeked, peekedByte);
    }
}
