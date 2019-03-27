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

package app.diol.incallui.answerproximitysensor;

import android.util.ArraySet;

import java.util.Set;

/**
 * Stores a fake screen on/off state for the {@link InCallActivity}. If InCallActivity see the state
 * is off, it will draw a black view over the activity pretending the screen is off.
 *
 * <p>If the screen is already touched when the screen is turned on, the OS behavior is sending a
 * new DOWN event once the point started moving and then behave as a normal gesture. To prevent
 * accidental answer/rejects, touches that started when the screen is off should be ignored.
 *
 * <p>a bug on certain devices with N-DR1, if the screen is already touched when the screen is
 * turned on, a "DOWN MOVE UP" will be sent for each movement before the touch is actually released.
 * These events is hard to discern from other normal events, and keeping the screen on reduces its'
 * probability.
 */
public class PseudoScreenState {

    private final Set<StateChangedListener> listeners = new ArraySet<>();
    private boolean on = true;

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean value) {
        if (on != value) {
            on = value;
            for (StateChangedListener listener : listeners) {
                listener.onPseudoScreenStateChanged(on);
            }
        }
    }

    public void addListener(StateChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StateChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies when the on state has changed.
     */
    public interface StateChangedListener {
        void onPseudoScreenStateChanged(boolean isOn);
    }
}
