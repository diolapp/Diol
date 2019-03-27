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

import android.util.ArrayMap;
import android.view.MotionEvent;

import java.util.Map;

/**
 * A classifier which looks at the speed and distance between successive points of a Stroke. It
 * looks at two consecutive speeds between two points and calculates the ratio between them. The
 * final result is the maximum of these values. It does the same for distances. If some speed or
 * distance is equal to zero then the ratio between this and the next part is not calculated. To the
 * duration of each part there is added one nanosecond so that it is always possible to calculate
 * the speed of a part.
 */
class AccelerationClassifier extends StrokeClassifier {
    private final Map<Stroke, Data> strokeMap = new ArrayMap<>();

    public AccelerationClassifier(ClassifierData classifierData) {
        this.classifierData = classifierData;
    }

    @Override
    public String getTag() {
        return "ACC";
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            strokeMap.clear();
        }

        for (int i = 0; i < event.getPointerCount(); i++) {
            Stroke stroke = classifierData.getStroke(event.getPointerId(i));
            Point point = stroke.getPoints().get(stroke.getPoints().size() - 1);
            if (strokeMap.get(stroke) == null) {
                strokeMap.put(stroke, new Data(point));
            } else {
                strokeMap.get(stroke).addPoint(point);
            }
        }
    }

    @Override
    public float getFalseTouchEvaluation(Stroke stroke) {
        Data data = strokeMap.get(stroke);
        return 2 * SpeedRatioEvaluator.evaluate(data.maxSpeedRatio);
    }

    private static class Data {

        static final float MILLIS_TO_NANOS = 1e6f;

        Point previousPoint;
        float previousSpeed = 0;
        float maxSpeedRatio = 0;

        public Data(Point point) {
            previousPoint = point;
        }

        public void addPoint(Point point) {
            float distance = previousPoint.dist(point);
            float duration = (float) (point.timeOffsetNano - previousPoint.timeOffsetNano + 1);
            float speed = distance / duration;

            if (duration > 20 * MILLIS_TO_NANOS || duration < 5 * MILLIS_TO_NANOS) {
                // reject this segment and ensure we won't use data about it in the next round.
                previousSpeed = 0;
                previousPoint = point;
                return;
            }
            if (previousSpeed != 0.0f) {
                maxSpeedRatio = Math.max(maxSpeedRatio, speed / previousSpeed);
            }

            previousSpeed = speed;
            previousPoint = point;
        }
    }
}
