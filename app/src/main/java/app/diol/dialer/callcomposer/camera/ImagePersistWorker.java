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

package app.diol.dialer.callcomposer.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.google.auto.value.AutoValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import app.diol.dialer.callcomposer.camera.ImagePersistWorker.Result;
import app.diol.dialer.callcomposer.camera.exif.ExifInterface;
import app.diol.dialer.callcomposer.util.BitmapResizer;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.constants.Constants;
import app.diol.dialer.util.DialerUtils;

/**
 * Persisting image routine.
 */
public class ImagePersistWorker implements Worker<Void, Result> {
    private final float heightPercent;
    private final byte[] bytes;
    private final Context context;
    private int width;
    private int height;

    ImagePersistWorker(
            final int width,
            final int height,
            final float heightPercent,
            final byte[] bytes,
            final Context context) {
        Assert.checkArgument(heightPercent >= 0 && heightPercent <= 1);
        Assert.isNotNull(bytes);
        Assert.isNotNull(context);
        this.width = width;
        this.height = height;
        this.heightPercent = heightPercent;
        this.bytes = bytes;
        this.context = context;
    }

    @Override
    public Result doInBackground(Void unused) throws Exception {
        File outputFile = DialerUtils.createShareableFile(context);

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            writeClippedBitmap(outputStream);
        }

        return Result.builder()
                .setUri(
                        FileProvider.getUriForFile(
                                context, Constants.get().getFileProviderAuthority(), outputFile))
                .setWidth(width)
                .setHeight(height)
                .build();
    }

    private void writeClippedBitmap(OutputStream outputStream) throws IOException {
        int orientation = android.media.ExifInterface.ORIENTATION_UNDEFINED;
        final ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(bytes);
            final Integer orientationValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            if (orientationValue != null) {
                orientation = orientationValue.intValue();
            }
        } catch (final IOException e) {
            // Couldn't get exif tags, not the end of the world
        }

        ExifInterface.OrientationParams params = ExifInterface.getOrientationParams(orientation);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        final int clippedWidth;
        final int clippedHeight;
        if (params.invertDimensions) {
            Assert.checkState(width == bitmap.getHeight());
            Assert.checkState(height == bitmap.getWidth());
            clippedWidth = (int) (height * heightPercent);
            clippedHeight = width;
        } else {
            Assert.checkState(width == bitmap.getWidth());
            Assert.checkState(height == bitmap.getHeight());
            clippedWidth = width;
            clippedHeight = (int) (height * heightPercent);
        }

        int offsetTop = (bitmap.getHeight() - clippedHeight) / 2;
        int offsetLeft = (bitmap.getWidth() - clippedWidth) / 2;
        width = clippedWidth;
        height = clippedHeight;

        Bitmap clippedBitmap =
                Bitmap.createBitmap(bitmap, offsetLeft, offsetTop, clippedWidth, clippedHeight);
        clippedBitmap = BitmapResizer.resizeForEnrichedCalling(clippedBitmap, params.rotation);
        // EXIF data can take a big chunk of the file size and we've already manually rotated our image,
        // so remove all of the exif data.
        exifInterface.clearExif();
        exifInterface.writeExif(clippedBitmap, outputStream);

        clippedBitmap.recycle();
        bitmap.recycle();
    }

    @AutoValue
    abstract static class Result {

        public static Builder builder() {
            return new AutoValue_ImagePersistWorker_Result.Builder();
        }

        @NonNull
        abstract Uri getUri();

        abstract int getWidth();

        abstract int getHeight();

        @AutoValue.Builder
        abstract static class Builder {
            abstract Builder setUri(@NonNull Uri uri);

            abstract Builder setWidth(int width);

            abstract Builder setHeight(int height);

            abstract Result build();
        }
    }
}
