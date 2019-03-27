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

import java.io.IOException;
import java.io.InputStream;

import app.diol.dialer.common.LogUtil;

/**
 * This class reads the EXIF header of a JPEG file and stores it in {@link ExifData}.
 */
class ExifReader {

    private final ExifInterface mInterface;

    ExifReader(ExifInterface iRef) {
        mInterface = iRef;
    }

    /**
     * Parses the inputStream and and returns the EXIF data in an {@link ExifData}.
     *
     * @throws ExifInvalidFormatException
     * @throws java.io.IOException
     */
    protected ExifData read(InputStream inputStream) throws ExifInvalidFormatException, IOException {
        ExifParser parser = ExifParser.parse(inputStream, mInterface);
        ExifData exifData = new ExifData();
        ExifTag tag;

        int event = parser.next();
        while (event != ExifParser.EVENT_END) {
            switch (event) {
                case ExifParser.EVENT_START_OF_IFD:
                    exifData.addIfdData(new IfdData(parser.getCurrentIfd()));
                    break;
                case ExifParser.EVENT_NEW_TAG:
                    tag = parser.getTag();
                    if (!tag.hasValue()) {
                        parser.registerForTagValue(tag);
                    } else {
                        exifData.getIfdData(tag.getIfd()).setTag(tag);
                    }
                    break;
                case ExifParser.EVENT_VALUE_OF_REGISTERED_TAG:
                    tag = parser.getTag();
                    if (tag.getDataType() == ExifTag.TYPE_UNDEFINED) {
                        parser.readFullTagValue(tag);
                    }
                    exifData.getIfdData(tag.getIfd()).setTag(tag);
                    break;
                case ExifParser.EVENT_COMPRESSED_IMAGE:
                    byte[] buf = new byte[parser.getCompressedImageSize()];
                    if (buf.length != parser.read(buf)) {
                        LogUtil.i("ExifReader.read", "Failed to read the compressed thumbnail");
                    }
                    break;
                case ExifParser.EVENT_UNCOMPRESSED_STRIP:
                    buf = new byte[parser.getStripSize()];
                    if (buf.length != parser.read(buf)) {
                        LogUtil.i("ExifReader.read", "Failed to read the strip bytes");
                    }
                    break;
            }
            event = parser.next();
        }
        return exifData;
    }
}
