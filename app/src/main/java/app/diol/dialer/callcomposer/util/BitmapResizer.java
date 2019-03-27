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

package app.diol.dialer.callcomposer.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.VisibleForTesting;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Utility class for resizing images before sending them as enriched call attachments.
 */
public final class BitmapResizer {
    @VisibleForTesting
    static final int MAX_OUTPUT_RESOLUTION = 640;

    /**
     * Returns a bitmap that is a resized version of the parameter image. The image will only be
     * resized down and sized to be appropriate for an enriched call.
     *
     * @param image    to be resized
     * @param rotation degrees to rotate the image clockwise
     * @return resized image
     */
    public static Bitmap resizeForEnrichedCalling(Bitmap image, int rotation) {
        Assert.isWorkerThread();

        int width = image.getWidth();
        int height = image.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        LogUtil.i(
                "BitmapResizer.resizeForEnrichedCalling", "starting height: %d, width: %d", height, width);

        if (width <= MAX_OUTPUT_RESOLUTION && height <= MAX_OUTPUT_RESOLUTION) {
            LogUtil.i("BitmapResizer.resizeForEnrichedCalling", "no resizing needed");
            return Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
        }

        float ratio = 1;
        if (width > height) {
            // landscape
            ratio = MAX_OUTPUT_RESOLUTION / (float) width;
        } else {
            // portrait & square
            ratio = MAX_OUTPUT_RESOLUTION / (float) height;
        }

        LogUtil.i(
                "BitmapResizer.resizeForEnrichedCalling",
                "ending height: %f, width: %f",
                height * ratio,
                width * ratio);

        matrix.postScale(ratio, ratio);
        return Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
    }
}
