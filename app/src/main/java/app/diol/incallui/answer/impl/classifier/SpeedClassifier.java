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

/**
 * A classifier that looks at the speed of the stroke. It calculates the speed of a stroke in inches
 * per second.
 */
class SpeedClassifier extends StrokeClassifier {

    public SpeedClassifier(ClassifierData classifierData) {
    }

    @Override
    public String getTag() {
        return "SPD";
    }

    @Override
    public float getFalseTouchEvaluation(Stroke stroke) {
        float duration = stroke.getDurationSeconds();
        if (duration == 0.0f) {
            return SpeedEvaluator.evaluate(0.0f);
        }
        return SpeedEvaluator.evaluate(stroke.getTotalLength() / duration);
    }
}
