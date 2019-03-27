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
 * An abstract class for classifiers which classify each stroke separately.
 */
abstract class StrokeClassifier extends Classifier {

    /**
     * @param stroke the stroke for which the evaluation will be calculated
     * @return a non-negative value which is used to determine whether this a false touch; the bigger
     * the value the greater the chance that this a false touch
     */
    public abstract float getFalseTouchEvaluation(Stroke stroke);
}
