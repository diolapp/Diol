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

class DurationCountEvaluator {
    public static float evaluate(float value) {
        float evaluation = 0.0f;
        if (value < 0.0105) {
            evaluation++;
        }
        if (value < 0.00909) {
            evaluation++;
        }
        if (value < 0.00667) {
            evaluation++;
        }
        if (value > 0.0333) {
            evaluation++;
        }
        if (value > 0.0500) {
            evaluation++;
        }
        return evaluation;
    }
}
