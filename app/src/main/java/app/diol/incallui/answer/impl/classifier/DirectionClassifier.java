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
 * A classifier which looks at the general direction of a stroke and evaluates it depending on the
 * type of action that takes place.
 */
public class DirectionClassifier extends StrokeClassifier {
    public DirectionClassifier(ClassifierData classifierData) {
    }

    @Override
    public String getTag() {
        return "DIR";
    }

    @Override
    public float getFalseTouchEvaluation(Stroke stroke) {
        Point firstPoint = stroke.getPoints().get(0);
        Point lastPoint = stroke.getPoints().get(stroke.getPoints().size() - 1);
        return DirectionEvaluator.evaluate(lastPoint.x - firstPoint.x, lastPoint.y - firstPoint.y);
    }
}
