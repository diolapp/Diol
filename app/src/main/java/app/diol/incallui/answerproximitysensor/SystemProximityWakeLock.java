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

package app.diol.incallui.answerproximitysensor;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.view.Display;

import app.diol.dialer.common.LogUtil;

/**
 * The normal PROXIMITY_SCREEN_OFF_WAKE_LOCK provided by the OS.
 */
public class SystemProximityWakeLock implements AnswerProximityWakeLock, DisplayListener {

    private static final String TAG = "SystemProximityWakeLock";

    private final Context context;
    private final PowerManager.WakeLock wakeLock;

    @Nullable
    private ScreenOnListener listener;

    public SystemProximityWakeLock(Context context) {
        this.context = context;
        wakeLock =
                context
                        .getSystemService(PowerManager.class)
                        .newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
    }

    private static boolean isDefaultDisplayOn(Context context) {
        Display display =
                context.getSystemService(DisplayManager.class).getDisplay(Display.DEFAULT_DISPLAY);
        return display.getState() != Display.STATE_OFF;
    }

    @Override
    public void acquire() {
        wakeLock.acquire();
        context.getSystemService(DisplayManager.class).registerDisplayListener(this, null);
    }

    @Override
    public void release() {
        wakeLock.release();
        context.getSystemService(DisplayManager.class).unregisterDisplayListener(this);
    }

    @Override
    public boolean isHeld() {
        return wakeLock.isHeld();
    }

    @Override
    public void setScreenOnListener(ScreenOnListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDisplayAdded(int displayId) {
    }

    @Override
    public void onDisplayRemoved(int displayId) {
    }

    @Override
    public void onDisplayChanged(int displayId) {
        if (displayId == Display.DEFAULT_DISPLAY) {
            if (isDefaultDisplayOn(context)) {
                LogUtil.i("SystemProximityWakeLock.onDisplayChanged", "display turned on");
                if (listener != null) {
                    listener.onScreenOn();
                }
            }
        }
    }
}
