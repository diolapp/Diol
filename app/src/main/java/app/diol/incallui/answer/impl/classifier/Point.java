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

class Point {
    public float x;
    public float y;
    public long timeOffsetNano;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
        this.timeOffsetNano = 0;
    }

    public Point(float x, float y, long timeOffsetNano) {
        this.x = x;
        this.y = y;
        this.timeOffsetNano = timeOffsetNano;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Point)) {
            return false;
        }
        Point otherPoint = ((Point) other);
        return x == otherPoint.x && y == otherPoint.y;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    public float dist(Point a) {
        return (float) Math.hypot(a.x - x, a.y - y);
    }

    /**
     * Calculates the cross product of vec(this, a) and vec(this, b) where vec(x,y) is the vector from
     * point x to point y
     */
    public float crossProduct(Point a, Point b) {
        return (a.x - x) * (b.y - y) - (a.y - y) * (b.x - x);
    }

    /**
     * Calculates the dot product of vec(this, a) and vec(this, b) where vec(x,y) is the vector from
     * point x to point y
     */
    public float dotProduct(Point a, Point b) {
        return (a.x - x) * (b.x - x) + (a.y - y) * (b.y - y);
    }

    /**
     * Calculates the angle in radians created by points (a, this, b). If any two of these points are
     * the same, the method will return 0.0f
     *
     * @return the angle in radians
     */
    public float getAngle(Point a, Point b) {
        float dist1 = dist(a);
        float dist2 = dist(b);

        if (dist1 == 0.0f || dist2 == 0.0f) {
            return 0.0f;
        }

        float crossProduct = crossProduct(a, b);
        float dotProduct = dotProduct(a, b);
        float cos = Math.min(1.0f, Math.max(-1.0f, dotProduct / dist1 / dist2));
        float angle = (float) Math.acos(cos);
        if (crossProduct < 0.0) {
            angle = 2.0f * (float) Math.PI - angle;
        }
        return angle;
    }
}
