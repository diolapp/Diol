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

import android.hardware.SensorEvent;
import android.view.MotionEvent;

/**
 * An abstract class for classifiers for touch and sensor events.
 */
abstract class Classifier {

    /**
     * Contains all the information about touch events from which the classifier can query
     */
    protected ClassifierData classifierData;

    /**
     * Informs the classifier that a new touch event has occurred
     */
    public void onTouchEvent(MotionEvent event) {
    }

    /**
     * Informs the classifier that a sensor change occurred
     */
    public void onSensorChanged(SensorEvent event) {
    }

    public abstract String getTag();
}
