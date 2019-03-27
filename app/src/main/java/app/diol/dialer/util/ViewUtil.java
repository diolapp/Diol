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

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Paint;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.TextView;

import java.util.Locale;

/**
 * Provides static functions to work with views
 */
public class ViewUtil {

    private ViewUtil() {
    }

    /**
     * Returns the width as specified in the LayoutParams
     *
     * @throws IllegalStateException Thrown if the view's width is unknown before a layout pass s
     */
    public static int getConstantPreLayoutWidth(View view) {
        // We haven't been layed out yet, so get the size from the LayoutParams
        final ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p.width < 0) {
            throw new IllegalStateException(
                    "Expecting view's width to be a constant rather " + "than a result of the layout pass");
        }
        return p.width;
    }

    /**
     * Returns a boolean indicating whether or not the view's layout direction is RTL
     *
     * @param view - A valid view
     * @return True if the view's layout direction is RTL
     */
    public static boolean isViewLayoutRtl(View view) {
        return view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static boolean isRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
    }

    public static void resizeText(TextView textView, int originalTextSize, int minTextSize) {
        final Paint paint = textView.getPaint();
        final int width = textView.getWidth();
        if (width == 0) {
            return;
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
        float ratio = width / paint.measureText(textView.getText().toString());
        if (ratio <= 1.0f) {
            textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, Math.max(minTextSize, originalTextSize * ratio));
        }
    }

    /**
     * Runs a piece of code just before the next draw, after layout and measurement
     */
    public static void doOnPreDraw(
            @NonNull final View view, final boolean drawNextFrame, final Runnable runnable) {
        view.getViewTreeObserver()
                .addOnPreDrawListener(
                        new OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                view.getViewTreeObserver().removeOnPreDrawListener(this);
                                runnable.run();
                                return drawNextFrame;
                            }
                        });
    }

    public static void doOnPreDraw(
            @NonNull final View view, final boolean drawNextFrame, final ViewRunnable runnable) {
        view.getViewTreeObserver()
                .addOnPreDrawListener(
                        new OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                view.getViewTreeObserver().removeOnPreDrawListener(this);
                                runnable.run(view);
                                return drawNextFrame;
                            }
                        });
    }

    public static void doOnGlobalLayout(@NonNull final View view, final ViewRunnable runnable) {
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                runnable.run(view);
                            }
                        });
    }

    /**
     * Returns {@code true} if animations should be disabled.
     *
     * <p>Animations should be disabled if {@link
     * android.provider.Settings.Global#ANIMATOR_DURATION_SCALE} is set to 0 through system settings
     * or the device is in power save mode.
     */
    public static boolean areAnimationsDisabled(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        PowerManager powerManager = context.getSystemService(PowerManager.class);
        return Settings.Global.getFloat(contentResolver, Global.ANIMATOR_DURATION_SCALE, 1.0f) == 0
                || powerManager.isPowerSaveMode();
    }

    /**
     * Similar to {@link Runnable} but takes a View parameter to operate on
     */
    public interface ViewRunnable {
        void run(@NonNull View view);
    }
}
