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

package app.diol.incallui.answer.impl.classifier;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.MotionEvent;

import java.util.concurrent.TimeUnit;

/**
 * A classifier which looks at the proximity sensor during the gesture. It calculates the percentage
 * the proximity sensor showing the near state during the whole gesture
 */
class ProximityClassifier extends GestureClassifier {
    private long gestureStartTimeNano;
    private long nearStartTimeNano;
    private long nearDuration;
    private boolean near;
    private float averageNear;

    public ProximityClassifier(ClassifierData classifierData) {
    }

    @Override
    public String getTag() {
        return "PROX";
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            update(event.values[0] < event.sensor.getMaximumRange(), event.timestamp);
        }
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            gestureStartTimeNano = TimeUnit.MILLISECONDS.toNanos(event.getEventTime());
            nearStartTimeNano = TimeUnit.MILLISECONDS.toNanos(event.getEventTime());
            nearDuration = 0;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            update(near, TimeUnit.MILLISECONDS.toNanos(event.getEventTime()));
            long duration = TimeUnit.MILLISECONDS.toNanos(event.getEventTime()) - gestureStartTimeNano;

            if (duration == 0) {
                averageNear = near ? 1.0f : 0.0f;
            } else {
                averageNear = (float) nearDuration / (float) duration;
            }
        }
    }

    /**
     * @param near          is the sensor showing the near state right now
     * @param timestampNano time of this event in nanoseconds
     */
    private void update(boolean near, long timestampNano) {
        // This if is necessary because MotionEvents and SensorEvents do not come in
        // chronological order
        if (timestampNano > nearStartTimeNano) {
            // if the state before was near then add the difference of the current time and
            // mNearStartTimeNano to mNearDuration.
            if (this.near) {
                nearDuration += timestampNano - nearStartTimeNano;
            }

            // if the new state is near, set mNearStartTimeNano equal to this moment.
            if (near) {
                nearStartTimeNano = timestampNano;
            }
        }
        this.near = near;
    }

    @Override
    public float getFalseTouchEvaluation() {
        return ProximityEvaluator.evaluate(averageNear);
    }
}
