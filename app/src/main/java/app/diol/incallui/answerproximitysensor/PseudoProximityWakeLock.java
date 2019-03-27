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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;

import app.diol.dialer.common.LogUtil;

/**
 * A fake PROXIMITY_SCREEN_OFF_WAKE_LOCK implemented by the app. It will use {@link
 * PseudoScreenState} to fake a black screen when the proximity sensor is near.
 */
public class PseudoProximityWakeLock implements AnswerProximityWakeLock, SensorEventListener {

    private final Context context;
    private final PseudoScreenState pseudoScreenState;
    private final Sensor proximitySensor;

    @Nullable
    private ScreenOnListener listener;
    private boolean isHeld;

    public PseudoProximityWakeLock(Context context, PseudoScreenState pseudoScreenState) {
        this.context = context;
        this.pseudoScreenState = pseudoScreenState;
        pseudoScreenState.setOn(true);
        proximitySensor =
                context.getSystemService(SensorManager.class).getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    public void acquire() {
        isHeld = true;
        context
                .getSystemService(SensorManager.class)
                .registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void release() {
        isHeld = false;
        context.getSystemService(SensorManager.class).unregisterListener(this);
        pseudoScreenState.setOn(true);
    }

    @Override
    public boolean isHeld() {
        return isHeld;
    }

    @Override
    public void setScreenOnListener(ScreenOnListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        boolean near = sensorEvent.values[0] < sensorEvent.sensor.getMaximumRange();
        LogUtil.i("AnswerProximitySensor.PseudoProximityWakeLock.onSensorChanged", "near: " + near);
        pseudoScreenState.setOn(!near);
        if (!near && listener != null) {
            listener.onScreenOn();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
