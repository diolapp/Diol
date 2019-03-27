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

class JpegHeader {
    static final short SOI = (short) 0xFFD8;
    static final short APP1 = (short) 0xFFE1;
    static final short EOI = (short) 0xFFD9;

    /**
     * SOF (start of frame). All value between SOF0 and SOF15 is SOF marker except for DHT, JPG, and
     * DAC marker.
     */
    private static final short SOF0 = (short) 0xFFC0;

    private static final short SOF15 = (short) 0xFFCF;
    private static final short DHT = (short) 0xFFC4;
    private static final short JPG = (short) 0xFFC8;
    private static final short DAC = (short) 0xFFCC;

    static boolean isSofMarker(short marker) {
        return marker >= SOF0 && marker <= SOF15 && marker != DHT && marker != JPG && marker != DAC;
    }
}
