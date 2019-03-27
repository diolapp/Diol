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

package app.diol.dialer.util;

import android.graphics.Point;

/**
 * Singleton class to keep track of where the user last touched the screen.
 *
 * <p>Used to pass on to the InCallUI for animation.
 */
public class TouchPointManager {

    public static final String TOUCH_POINT = "touchPoint";

    private static TouchPointManager instance = new TouchPointManager();

    private Point point = new Point();

    /**
     * Private constructor. Instance should only be acquired through getRunningInstance().
     */
    private TouchPointManager() {
    }

    public static TouchPointManager getInstance() {
        return instance;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(int x, int y) {
        point.set(x, y);
    }

    /**
     * When a point is initialized, its value is (0,0). Since it is highly unlikely a user will touch
     * at that exact point, if the point in TouchPointManager is (0,0), it is safe to assume that the
     * TouchPointManager has not yet collected a touch.
     *
     * @return True if there is a valid point saved. Define a valid point as any point that is not
     * (0,0).
     */
    public boolean hasValidPoint() {
        return point.x != 0 || point.y != 0;
    }
}
