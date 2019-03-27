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
package app.diol.voicemail.impl.mail.utility;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EOLConvertingOutputStream extends FilterOutputStream {
    int lastChar;

    public EOLConvertingOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int oneByte) throws IOException {
        if (oneByte == '\n') {
            if (lastChar != '\r') {
                super.write('\r');
            }
        }
        super.write(oneByte);
        lastChar = oneByte;
    }

    @Override
    public void flush() throws IOException {
        if (lastChar == '\r') {
            super.write('\n');
            lastChar = '\n';
        }
        super.flush();
    }
}
