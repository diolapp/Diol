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

package app.diol.incallui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.annotation.IntDef;
import android.view.OrientationEventListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.diol.dialer.common.LogUtil;

/**
 * This class listens to Orientation events and overrides onOrientationChanged which gets invoked
 * when an orientation change occurs. When that happens, we notify InCallUI registrants of the
 * change.
 */
public class InCallOrientationEventListener extends OrientationEventListener {

    public static final int SCREEN_ORIENTATION_0 = 0;
    public static final int SCREEN_ORIENTATION_90 = 90;
    public static final int SCREEN_ORIENTATION_180 = 180;
    public static final int SCREEN_ORIENTATION_270 = 270;
    public static final int SCREEN_ORIENTATION_360 = 360;
    // We use SCREEN_ORIENTATION_USER so that reverse-portrait is not allowed.
    public static final int ACTIVITY_PREFERENCE_ALLOW_ROTATION = ActivityInfo.SCREEN_ORIENTATION_USER;
    public static final int ACTIVITY_PREFERENCE_DISALLOW_ROTATION =
            ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
    /**
     * This is to identify dead zones where we won't notify others of orientation changed. Say for e.g
     * our threshold is x degrees. We will only notify UI when our current rotation is within x
     * degrees right or left of the screen orientation angles. If it's not within those ranges, we
     * return SCREEN_ORIENTATION_UNKNOWN and ignore it.
     */
    public static final int SCREEN_ORIENTATION_UNKNOWN = -1;
    // Rotation threshold is 10 degrees. So if the rotation angle is within 10 degrees of any of
    // the above angles, we will notify orientation changed.
    private static final int ROTATION_THRESHOLD = 10;
    /**
     * Cache the current rotation of the device.
     */
    @ScreenOrientation
    private static int currentOrientation = SCREEN_ORIENTATION_0;
    private boolean enabled = false;

    public InCallOrientationEventListener(Context context) {
        super(context);
    }

    private static boolean isWithinRange(int value, int begin, int end) {
        return value >= begin && value < end;
    }

    private static boolean isWithinThreshold(int value, int center, int threshold) {
        return isWithinRange(value, center - threshold, center + threshold);
    }

    private static boolean isInLeftRange(int value, int center, int threshold) {
        return isWithinRange(value, center - threshold, center);
    }

    private static boolean isInRightRange(int value, int center, int threshold) {
        return isWithinRange(value, center, center + threshold);
    }

    @ScreenOrientation
    public static int getCurrentOrientation() {
        return currentOrientation;
    }

    /**
     * Handles changes in device orientation. Notifies InCallPresenter of orientation changes.
     *
     * <p>Note that this API receives sensor rotation in degrees as a param and we convert that to one
     * of our screen orientation constants - (one of: {@link #SCREEN_ORIENTATION_0}, {@link
     * #SCREEN_ORIENTATION_90}, {@link #SCREEN_ORIENTATION_180}, {@link #SCREEN_ORIENTATION_270}).
     *
     * @param rotation The new device sensor rotation in degrees
     */
    @Override
    public void onOrientationChanged(int rotation) {
        if (rotation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }

        final int orientation = toScreenOrientation(rotation);

        if (orientation != SCREEN_ORIENTATION_UNKNOWN && currentOrientation != orientation) {
            LogUtil.i(
                    "InCallOrientationEventListener.onOrientationChanged",
                    "orientation: %d -> %d",
                    currentOrientation,
                    orientation);
            currentOrientation = orientation;
            InCallPresenter.getInstance().onDeviceOrientationChange(currentOrientation);
        }
    }

    /**
     * Enables the OrientationEventListener and optionally notifies listeners of the current
     * orientation.
     *
     * @param notifyDeviceOrientationChange Whether to notify listeners that the device orientation is
     *                                      changed.
     */
    public void enable(boolean notifyDeviceOrientationChange) {
        if (enabled) {
            Log.v(this, "enable: Orientation listener is already enabled. Ignoring...");
            return;
        }

        super.enable();
        enabled = true;
        if (notifyDeviceOrientationChange) {
            InCallPresenter.getInstance().onDeviceOrientationChange(currentOrientation);
        }
    }

    /**
     * Enables the OrientationEventListener.
     */
    @Override
    public void enable() {
        enable(false /* notifyDeviceOrientationChange */);
    }

    /**
     * Disables the OrientationEventListener.
     */
    @Override
    public void disable() {
        if (!enabled) {
            Log.v(this, "enable: Orientation listener is already disabled. Ignoring...");
            return;
        }

        enabled = false;
        super.disable();
    }

    /**
     * Returns true the OrientationEventListener is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Converts sensor rotation in degrees to screen orientation constants.
     *
     * @param rotation sensor rotation angle in degrees
     * @return Screen orientation angle in degrees (0, 90, 180, 270). Returns -1 for degrees not
     * within threshold to identify zones where orientation change should not be trigerred.
     */
    @ScreenOrientation
    private int toScreenOrientation(int rotation) {
        // Sensor orientation 90 is equivalent to screen orientation 270 and vice versa. This
        // function returns the screen orientation. So we convert sensor rotation 90 to 270 and
        // vice versa here.
        if (isInLeftRange(rotation, SCREEN_ORIENTATION_360, ROTATION_THRESHOLD)
                || isInRightRange(rotation, SCREEN_ORIENTATION_0, ROTATION_THRESHOLD)) {
            return SCREEN_ORIENTATION_0;
        } else if (isWithinThreshold(rotation, SCREEN_ORIENTATION_90, ROTATION_THRESHOLD)) {
            return SCREEN_ORIENTATION_270;
        } else if (isWithinThreshold(rotation, SCREEN_ORIENTATION_180, ROTATION_THRESHOLD)) {
            return SCREEN_ORIENTATION_180;
        } else if (isWithinThreshold(rotation, SCREEN_ORIENTATION_270, ROTATION_THRESHOLD)) {
            return SCREEN_ORIENTATION_90;
        }
        return SCREEN_ORIENTATION_UNKNOWN;
    }

    /**
     * Screen orientation angles one of 0, 90, 180, 270, 360 in degrees.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SCREEN_ORIENTATION_0,
            SCREEN_ORIENTATION_90,
            SCREEN_ORIENTATION_180,
            SCREEN_ORIENTATION_270,
            SCREEN_ORIENTATION_360,
            SCREEN_ORIENTATION_UNKNOWN
    })
    public @interface ScreenOrientation {
    }
}
