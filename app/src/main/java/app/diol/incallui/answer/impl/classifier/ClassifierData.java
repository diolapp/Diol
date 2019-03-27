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

import android.util.SparseArray;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Contains data which is used to classify interaction sequences on the lockscreen. It does, for
 * example, provide information on the current touch state.
 */
class ClassifierData {
    private final float dpi;
    private final float screenHeight;
    private SparseArray<Stroke> currentStrokes = new SparseArray<>();
    private ArrayList<Stroke> endingStrokes = new ArrayList<>();

    public ClassifierData(float dpi, float screenHeight) {
        this.dpi = dpi;
        this.screenHeight = screenHeight / dpi;
    }

    public void update(MotionEvent event) {
        endingStrokes.clear();
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            currentStrokes.clear();
        }

        for (int i = 0; i < event.getPointerCount(); i++) {
            int id = event.getPointerId(i);
            if (currentStrokes.get(id) == null) {
                // TODO (keyboardr): See if there's a way to use event.getEventTimeNanos() instead
                currentStrokes.put(
                        id, new Stroke(TimeUnit.MILLISECONDS.toNanos(event.getEventTime()), dpi));
            }
            currentStrokes
                    .get(id)
                    .addPoint(
                            event.getX(i), event.getY(i), TimeUnit.MILLISECONDS.toNanos(event.getEventTime()));

            if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL
                    || (action == MotionEvent.ACTION_POINTER_UP && i == event.getActionIndex())) {
                endingStrokes.add(getStroke(id));
            }
        }
    }

    void cleanUp(MotionEvent event) {
        endingStrokes.clear();
        int action = event.getActionMasked();
        for (int i = 0; i < event.getPointerCount(); i++) {
            int id = event.getPointerId(i);
            if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL
                    || (action == MotionEvent.ACTION_POINTER_UP && i == event.getActionIndex())) {
                currentStrokes.remove(id);
            }
        }
    }

    /**
     * @return the list of Strokes which are ending in the recently added MotionEvent
     */
    public ArrayList<Stroke> getEndingStrokes() {
        return endingStrokes;
    }

    /**
     * @param id the id from MotionEvent
     * @return the Stroke assigned to the id
     */
    public Stroke getStroke(int id) {
        return currentStrokes.get(id);
    }

    /**
     * @return the height of the screen in inches
     */
    public float getScreenHeight() {
        return screenHeight;
    }
}
