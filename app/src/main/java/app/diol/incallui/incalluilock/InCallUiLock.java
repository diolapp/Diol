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

package app.diol.incallui.incalluilock;

/**
 * Prevents the {@link app.diol.incallui.InCallActivity} from auto-finishing where there are no
 * calls left. Acquired through {@link
 * app.diol.incallui.InCallPresenter#acquireInCallUiLock(String)}. Example: when a dialog is
 * still being displayed to the user the InCallActivity should not disappear abruptly when the call
 * ends, this lock should be held to keep the activity alive until it is dismissed.
 */
public interface InCallUiLock {

    void release();
}
