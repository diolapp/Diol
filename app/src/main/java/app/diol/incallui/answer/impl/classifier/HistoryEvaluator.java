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

import android.os.SystemClock;

import java.util.ArrayList;

/**
 * Holds the evaluations for ended strokes and gestures. These values are decreased through time.
 */
class HistoryEvaluator {
    private static final float INTERVAL = 50.0f;
    private static final float HISTORY_FACTOR = 0.9f;
    private static final float EPSILON = 1e-5f;

    private final ArrayList<Data> strokes = new ArrayList<>();
    private final ArrayList<Data> gestureWeights = new ArrayList<>();
    private long lastUpdate;

    public HistoryEvaluator() {
        lastUpdate = SystemClock.elapsedRealtime();
    }

    public void addStroke(float evaluation) {
        decayValue();
        strokes.add(new Data(evaluation));
    }

    public void addGesture(float evaluation) {
        decayValue();
        gestureWeights.add(new Data(evaluation));
    }

    /**
     * Calculates the weighted average of strokes and adds to it the weighted average of gestures
     */
    public float getEvaluation() {
        return weightedAverage(strokes) + weightedAverage(gestureWeights);
    }

    private float weightedAverage(ArrayList<Data> list) {
        float sumValue = 0.0f;
        float sumWeight = 0.0f;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Data data = list.get(i);
            sumValue += data.evaluation * data.weight;
            sumWeight += data.weight;
        }

        if (sumWeight == 0.0f) {
            return 0.0f;
        }

        return sumValue / sumWeight;
    }

    private void decayValue() {
        long time = SystemClock.elapsedRealtime();

        if (time <= lastUpdate) {
            return;
        }

        // All weights are multiplied by HISTORY_FACTOR after each INTERVAL milliseconds.
        float factor = (float) Math.pow(HISTORY_FACTOR, (time - lastUpdate) / INTERVAL);

        decayValue(strokes, factor);
        decayValue(gestureWeights, factor);
        lastUpdate = time;
    }

    private void decayValue(ArrayList<Data> list, float factor) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            list.get(i).weight *= factor;
        }

        // Removing evaluations with such small weights that they do not matter anymore
        while (!list.isEmpty() && isZero(list.get(0).weight)) {
            list.remove(0);
        }
    }

    private boolean isZero(float x) {
        return x <= EPSILON && x >= -EPSILON;
    }

    /**
     * For each stroke it holds its initial value and the current weight. Initially the weight is set
     * to 1.0
     */
    private static class Data {
        public float evaluation;
        public float weight;

        public Data(float evaluation) {
            this.evaluation = evaluation;
            weight = 1.0f;
        }
    }
}
