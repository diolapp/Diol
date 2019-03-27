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

/**
 * This class stores the EXIF header in IFDs according to the JPEG specification. It is the result
 * produced by {@link ExifReader}.
 *
 * @see ExifReader
 * @see IfdData
 */
public class ExifData {

    private final IfdData[] ifdDatas = new IfdData[IfdId.TYPE_IFD_COUNT];

    /**
     * Adds IFD data. If IFD data of the same type already exists, it will be replaced by the new
     * data.
     */
    void addIfdData(IfdData data) {
        ifdDatas[data.getId()] = data;
    }

    /**
     * Returns the {@link IfdData} object corresponding to a given IFD if it exists or null.
     */
    IfdData getIfdData(int ifdId) {
        if (ExifTag.isValidIfd(ifdId)) {
            return ifdDatas[ifdId];
        }
        return null;
    }

    /**
     * Returns the tag with a given TID in the given IFD if the tag exists. Otherwise returns null.
     */
    protected ExifTag getTag(short tag, int ifd) {
        IfdData ifdData = ifdDatas[ifd];
        return (ifdData == null) ? null : ifdData.getTag(tag);
    }

    /**
     * Adds the given ExifTag to its default IFD and returns an existing ExifTag with the same TID or
     * null if none exist.
     */
    ExifTag addTag(ExifTag tag) {
        if (tag != null) {
            int ifd = tag.getIfd();
            return addTag(tag, ifd);
        }
        return null;
    }

    /**
     * Adds the given ExifTag to the given IFD and returns an existing ExifTag with the same TID or
     * null if none exist.
     */
    private ExifTag addTag(ExifTag tag, int ifdId) {
        if (tag != null && ExifTag.isValidIfd(ifdId)) {
            IfdData ifdData = getOrCreateIfdData(ifdId);
            return ifdData.setTag(tag);
        }
        return null;
    }

    /**
     * Returns the {@link IfdData} object corresponding to a given IFD or generates one if none exist.
     */
    private IfdData getOrCreateIfdData(int ifdId) {
        IfdData ifdData = ifdDatas[ifdId];
        if (ifdData == null) {
            ifdData = new IfdData(ifdId);
            ifdDatas[ifdId] = ifdData;
        }
        return ifdData;
    }
}
