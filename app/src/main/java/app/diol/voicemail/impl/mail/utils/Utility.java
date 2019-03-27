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
package app.diol.voicemail.impl.mail.utils;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Simple utility methods used in email functions.
 */
public class Utility {
    public static final Charset ASCII = Charset.forName("US-ASCII");

    public static final String[] EMPTY_STRINGS = new String[0];

    /**
     * Returns a concatenated string containing the output of every Object's
     * toString() method, each separated by the given separator character.
     */
    public static String combine(Object[] parts, char separator) {
        if (parts == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i].toString());
            if (i < parts.length - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * Converts a String to ASCII bytes
     */
    public static byte[] toAscii(String s) {
        return encode(ASCII, s);
    }

    /**
     * Builds a String from ASCII bytes
     */
    public static String fromAscii(byte[] b) {
        return decode(ASCII, b);
    }

    private static byte[] encode(Charset charset, String s) {
        if (s == null) {
            return null;
        }
        final ByteBuffer buffer = charset.encode(CharBuffer.wrap(s));
        final byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        return bytes;
    }

    private static String decode(Charset charset, byte[] b) {
        if (b == null) {
            return null;
        }
        final CharBuffer cb = charset.decode(ByteBuffer.wrap(b));
        return new String(cb.array(), 0, cb.length());
    }

    public static ByteArrayInputStream streamFromAsciiString(String ascii) {
        return new ByteArrayInputStream(toAscii(ascii));
    }
}
