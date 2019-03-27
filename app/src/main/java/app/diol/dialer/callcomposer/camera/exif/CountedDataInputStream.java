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

package app.diol.dialer.callcomposer.camera.exif;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import app.diol.dialer.common.Assert;

class CountedDataInputStream extends FilterInputStream {

    // allocate a byte buffer for a long value;
    private final byte[] byteArray = new byte[8];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
    private int count = 0;

    CountedDataInputStream(InputStream in) {
        super(in);
    }

    int getReadByteCount() {
        return count;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int r = in.read(b);
        count += (r >= 0) ? r : 0;
        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = in.read(b, off, len);
        count += (r >= 0) ? r : 0;
        return r;
    }

    @Override
    public int read() throws IOException {
        int r = in.read();
        count += (r >= 0) ? 1 : 0;
        return r;
    }

    @Override
    public long skip(long length) throws IOException {
        long skip = in.skip(length);
        count += skip;
        return skip;
    }

    private void skipOrThrow(long length) throws IOException {
        if (skip(length) != length) {
            throw new EOFException();
        }
    }

    void skipTo(long target) throws IOException {
        long cur = count;
        long diff = target - cur;
        Assert.checkArgument(diff >= 0);
        skipOrThrow(diff);
    }

    private void readOrThrow(byte[] b, int off, int len) throws IOException {
        int r = read(b, off, len);
        if (r != len) {
            throw new EOFException();
        }
    }

    private void readOrThrow(byte[] b) throws IOException {
        readOrThrow(b, 0, b.length);
    }

    ByteOrder getByteOrder() {
        return byteBuffer.order();
    }

    void setByteOrder(ByteOrder order) {
        byteBuffer.order(order);
    }

    short readShort() throws IOException {
        readOrThrow(byteArray, 0, 2);
        byteBuffer.rewind();
        return byteBuffer.getShort();
    }

    int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    int readInt() throws IOException {
        readOrThrow(byteArray, 0, 4);
        byteBuffer.rewind();
        return byteBuffer.getInt();
    }

    long readUnsignedInt() throws IOException {
        return readInt() & 0xffffffffL;
    }

    String readString(int n, Charset charset) throws IOException {
        byte[] buf = new byte[n];
        readOrThrow(buf);
        return new String(buf, charset);
    }
}
