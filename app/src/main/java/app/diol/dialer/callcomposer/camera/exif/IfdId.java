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
 * The constants of the IFD ID defined in EXIF spec.
 */
public interface IfdId {
    int TYPE_IFD_0 = 0;
    int TYPE_IFD_1 = 1;
    int TYPE_IFD_EXIF = 2;
    int TYPE_IFD_INTEROPERABILITY = 3;
    int TYPE_IFD_GPS = 4;
    /* This is used in ExifData to allocate enough IfdData */
    int TYPE_IFD_COUNT = 5;
}
