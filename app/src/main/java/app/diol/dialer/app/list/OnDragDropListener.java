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

package app.diol.dialer.app.list;

/**
 * Classes that want to receive callbacks in response to drag events should implement this
 * interface.
 */
public interface OnDragDropListener {

    /**
     * Called when a drag is started.
     *
     * @param x    X-coordinate of the drag event
     * @param y    Y-coordinate of the drag event
     * @param view The contact tile which the drag was started on
     */
    void onDragStarted(int x, int y, PhoneFavoriteSquareTileView view);

    /**
     * Called when a drag is in progress and the user moves the dragged contact to a location.
     *
     * @param x    X-coordinate of the drag event
     * @param y    Y-coordinate of the drag event
     * @param view Contact tile in the ListView which is currently being displaced by the dragged
     *             contact
     */
    void onDragHovered(int x, int y, PhoneFavoriteSquareTileView view);

    /**
     * Called when a drag is completed (whether by dropping it somewhere or simply by dragging the
     * contact off the screen)
     *
     * @param x X-coordinate of the drag event
     * @param y Y-coordinate of the drag event
     */
    void onDragFinished(int x, int y);

    /**
     * Called when a contact has been dropped on the remove view, indicating that the user wants to
     * remove this contact.
     */
    void onDroppedOnRemove();
}
