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

import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple pass-thru OutputStream that also counts how many bytes are written
 * to it and makes that count available to callers.
 */
public class CountingOutputStream extends OutputStream {
    private final OutputStream outputStream;
    private long count;

    public CountingOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public long getCount() {
        return count;
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        outputStream.write(buffer, offset, count);
        this.count += count;
    }

    @Override
    public void write(int oneByte) throws IOException {
        outputStream.write(oneByte);
        count++;
    }
}
