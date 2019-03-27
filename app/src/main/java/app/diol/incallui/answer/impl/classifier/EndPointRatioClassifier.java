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
 * A classifier which looks at the ratio between the total length covered by the stroke and the
 * distance between the first and last point from this stroke.
 */
class EndPointRatioClassifier extends StrokeClassifier {
    public EndPointRatioClassifier(ClassifierData classifierData) {
        this.classifierData = classifierData;
    }

    @Override
    public String getTag() {
        return "END_RTIO";
    }

    @Override
    public float getFalseTouchEvaluation(Stroke stroke) {
        float ratio;
        if (stroke.getTotalLength() == 0.0f) {
            ratio = 1.0f;
        } else {
            ratio = stroke.getEndPointLength() / stroke.getTotalLength();
        }
        return EndPointRatioEvaluator.evaluate(ratio);
    }
}
