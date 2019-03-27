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

package app.diol.dialer.common;

/**
 * Utility class for common math operations
 */
public class MathUtil {

    /**
     * Interpolates between two integer values based on percentage.
     *
     * @param begin   Begin value
     * @param end     End value
     * @param percent Percentage value, between 0 and 1
     * @return Interpolated result
     */
    public static int lerp(int begin, int end, float percent) {
        return (int) (begin * (1 - percent) + end * percent);
    }

    /**
     * Interpolates between two float values based on percentage.
     *
     * @param begin   Begin value
     * @param end     End value
     * @param percent Percentage value, between 0 and 1
     * @return Interpolated result
     */
    public static float lerp(float begin, float end, float percent) {
        return begin * (1 - percent) + end * percent;
    }

    /**
     * Clamps a value between two bounds inclusively.
     *
     * @param value Value to be clamped
     * @param min   Lower bound
     * @param max   Upper bound
     * @return Clamped value
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
