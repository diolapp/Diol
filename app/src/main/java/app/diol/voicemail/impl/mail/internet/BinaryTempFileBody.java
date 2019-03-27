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
import android.util.Base64OutputStream;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import app.diol.voicemail.impl.mail.Body;
import app.diol.voicemail.impl.mail.MessagingException;
import app.diol.voicemail.impl.mail.TempDirectory;

/**
 * A Body that is backed by a temp file. The Body exposes a getOutputStream
 * method that allows the user to write to the temp file. After the write the
 * body is available via getInputStream and writeTo one time. After writeTo is
 * called, or the InputStream returned from getInputStream is closed the file is
 * deleted and the Body should be considered disposed of.
 */
public class BinaryTempFileBody implements Body {
    private File file;

    /**
     * An alternate way to put data into a BinaryTempFileBody is to simply supply an
     * already- created file. Note that this file will be deleted after it is read.
     *
     * @param filePath The file containing the data to be stored on disk temporarily
     */
    public void setFile(String filePath) {
        file = new File(filePath);
    }

    public OutputStream getOutputStream() throws IOException {
        file = File.createTempFile("body", null, TempDirectory.getTempDirectory());
        file.deleteOnExit();
        return new FileOutputStream(file);
    }

    @Override
    public InputStream getInputStream() throws MessagingException {
        try {
            return new BinaryTempFileBodyInputStream(new FileInputStream(file));
        } catch (IOException ioe) {
            throw new MessagingException("Unable to open body", ioe);
        }
    }

    @Override
    public void writeTo(OutputStream out) throws IOException, MessagingException {
        InputStream in = getInputStream();
        Base64OutputStream base64Out = new Base64OutputStream(out, Base64.CRLF | Base64.NO_CLOSE);
        IOUtils.copy(in, base64Out);
        base64Out.close();
        file.delete();
        in.close();
    }

    class BinaryTempFileBodyInputStream extends FilterInputStream {
        public BinaryTempFileBodyInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            super.close();
            file.delete();
        }
    }
}
