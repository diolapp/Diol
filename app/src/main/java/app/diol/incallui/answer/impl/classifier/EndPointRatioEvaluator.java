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

class EndPointRatioEvaluator {
    public static float evaluate(float value) {
        float evaluation = 0.0f;
        if (value < 0.85) {
            evaluation++;
        }
        if (value < 0.75) {
            evaluation++;
        }
        if (value < 0.65) {
            evaluation++;
        }
        if (value < 0.55) {
            evaluation++;
        }
        if (value < 0.45) {
            evaluation++;
        }
        if (value < 0.35) {
            evaluation++;
        }
        return evaluation;
    }
}
