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

package app.diol.dialer.callcomposer;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.CursorLoader;

/**
 * A BoundCursorLoader that reads local media on the device.
 */
public class GalleryCursorLoader extends CursorLoader {
    public static final String MEDIA_SCANNER_VOLUME_EXTERNAL = "external";
    public static final String[] ACCEPTABLE_IMAGE_TYPES =
            new String[]{"image/jpeg", "image/jpg", "image/png", "image/webp"};

    private static final Uri STORAGE_URI = Files.getContentUri(MEDIA_SCANNER_VOLUME_EXTERNAL);
    private static final String SORT_ORDER = Media.DATE_MODIFIED + " DESC";
    private static final String IMAGE_SELECTION = createSelection();

    public GalleryCursorLoader(Context context) {
        super(
                context,
                STORAGE_URI,
                GalleryGridItemData.IMAGE_PROJECTION,
                IMAGE_SELECTION,
                null,
                SORT_ORDER);
    }

    private static String createSelection() {
        return "mime_type IN ('image/jpeg', 'image/jpg', 'image/png', 'image/webp')"
                + " AND media_type in ("
                + FileColumns.MEDIA_TYPE_IMAGE
                + ")";
    }
}
