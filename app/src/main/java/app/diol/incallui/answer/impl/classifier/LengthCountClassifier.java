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
 * A classifier which looks at the ratio between the length of the stroke and its number of points.
 * The number of points is subtracted by 2 because the UP event comes in with some delay and it
 * should not influence the ratio and also strokes which are long and have a small number of points
 * are punished more (these kind of strokes are usually bad ones and they tend to score well in
 * other classifiers).
 */
class LengthCountClassifier extends StrokeClassifier {
    public LengthCountClassifier(ClassifierData classifierData) {
    }

    @Override
    public String getTag() {
        return "LEN_CNT";
    }

    @Override
    public float getFalseTouchEvaluation(Stroke stroke) {
        return LengthCountEvaluator.evaluate(
                stroke.getTotalLength() / Math.max(1.0f, stroke.getCount() - 2));
    }
}
