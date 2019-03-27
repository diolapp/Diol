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

class SpeedRatioEvaluator {
    public static float evaluate(float value) {
        float evaluation = 0.0f;
        if (value == 0) {
            return 0;
        }
        if (value <= 1.0) {
            evaluation++;
        }
        if (value <= 0.5) {
            evaluation++;
        }
        if (value > 9.0) {
            evaluation++;
        }
        if (value > 18.0) {
            evaluation++;
        }
        return evaluation;
    }
}
