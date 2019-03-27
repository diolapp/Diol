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

import java.util.ArrayList;

/**
 * Contains data about a stroke (a single trace, all the events from a given id from the
 * DOWN/POINTER_DOWN event till the UP/POINTER_UP/CANCEL event.)
 */
class Stroke {

    private static final float NANOS_TO_SECONDS = 1e9f;
    private final float dpi;
    private ArrayList<Point> points = new ArrayList<>();
    private long startTimeNano;
    private long endTimeNano;
    private float length;

    public Stroke(long eventTimeNano, float dpi) {
        this.dpi = dpi;
        startTimeNano = endTimeNano = eventTimeNano;
    }

    public void addPoint(float x, float y, long eventTimeNano) {
        endTimeNano = eventTimeNano;
        Point point = new Point(x / dpi, y / dpi, eventTimeNano - startTimeNano);
        if (!points.isEmpty()) {
            length += points.get(points.size() - 1).dist(point);
        }
        points.add(point);
    }

    public int getCount() {
        return points.size();
    }

    public float getTotalLength() {
        return length;
    }

    public float getEndPointLength() {
        return points.get(0).dist(points.get(points.size() - 1));
    }

    public long getDurationNanos() {
        return endTimeNano - startTimeNano;
    }

    public float getDurationSeconds() {
        return (float) getDurationNanos() / NANOS_TO_SECONDS;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }
}
