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
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import app.diol.dialer.callcomposer.util.BitmapResizer;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.util.DialerUtils;

/**
 * Task for copying and resizing images to be shared with RCS process.
 */
class CopyAndResizeImageWorker implements Worker<Uri, Pair<File, String>> {
    private static final String MIME_TYPE = "image/jpeg";

    private final Context context;

    CopyAndResizeImageWorker(@NonNull Context context) {
        this.context = Assert.isNotNull(context);
    }

    private static int exifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * @param input The filepath where the image is located.
     * @return a Pair where the File contains the resized image, and the String is the result File's
     * MIME type.
     */
    @Nullable
    @Override
    public Pair<File, String> doInBackground(@Nullable Uri input) throws Throwable {
        // BitmapFactory.decodeStream strips exif data, so we need to save it here and apply it later.
        int rotation = 0;
        try {
            rotation =
                    new ExifInterface(input.getPath())
                            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception ignored) {
            // Couldn't get exif tags, not the end of the world
        }

        try (InputStream inputStream = context.getContentResolver().openInputStream(input)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = BitmapResizer.resizeForEnrichedCalling(bitmap, exifToDegrees(rotation));

            File outputFile = DialerUtils.createShareableFile(context);
            try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                // Encode images to jpeg as it is better for camera pictures which we expect to be sending
                bitmap.compress(CompressFormat.JPEG, 80, outputStream);
                return new Pair<>(outputFile, MIME_TYPE);
            }
        }
    }
}
