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

package app.diol.dialer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import app.diol.dialer.common.LogUtil;

/**
 * Provides utilities for bitmaps and drawables.
 */
public class DrawableConverter {

    private DrawableConverter() {
    }

    /**
     * Converts the provided drawable to a bitmap using the drawable's intrinsic width and height.
     */
    @Nullable
    public static Bitmap drawableToBitmap(@Nullable Drawable drawable) {
        return drawableToBitmap(drawable, 0, 0);
    }

    /**
     * Converts the provided drawable to a bitmap with the specified width and height.
     *
     * <p>If both width and height are 0, the drawable's intrinsic width and height are used (but in
     * that case {@link #drawableToBitmap(Drawable)} should be used).
     */
    @Nullable
    public static Bitmap drawableToBitmap(@Nullable Drawable drawable, int width, int height) {
        if (drawable == null) {
            return null;
        }

        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            if (width > 0 || height > 0) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            } else if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                // Needed for drawables that are just a colour.
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            } else {
                bitmap =
                        Bitmap.createBitmap(
                                drawable.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight(),
                                Bitmap.Config.ARGB_8888);
            }

            LogUtil.i(
                    "DrawableConverter.drawableToBitmap",
                    "created bitmap with width: %d, height: %d",
                    bitmap.getWidth(),
                    bitmap.getHeight());

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }

    @Nullable
    public static Drawable getRoundedDrawable(
            @NonNull Context context, @Nullable Drawable photo, int width, int height) {
        Bitmap bitmap = drawableToBitmap(photo);
        if (bitmap != null) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            RoundedBitmapDrawable drawable =
                    RoundedBitmapDrawableFactory.create(context.getResources(), scaledBitmap);
            drawable.setAntiAlias(true);
            drawable.setCornerRadius(drawable.getIntrinsicHeight() / 2);
            return drawable;
        }
        return null;
    }
}
