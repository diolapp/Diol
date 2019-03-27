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

import android.view.MotionEvent;

/**
 * A classifier which looks at the total number of traces in the whole gesture.
 */
class PointerCountClassifier extends GestureClassifier {
    private int count;

    public PointerCountClassifier(ClassifierData classifierData) {
        count = 0;
    }

    @Override
    public String getTag() {
        return "PTR_CNT";
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            count = 1;
        }

        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            ++count;
        }
    }

    @Override
    public float getFalseTouchEvaluation() {
        return PointerCountEvaluator.evaluate(count);
    }
}
