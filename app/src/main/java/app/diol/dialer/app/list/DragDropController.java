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

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handles and combines drag events generated from multiple views, and then fires off
 * events to any OnDragDropListeners that have registered for callbacks.
 */
public class DragDropController {

    private final List<OnDragDropListener> onDragDropListeners = new ArrayList<OnDragDropListener>();
    private final DragItemContainer dragItemContainer;
    private final int[] locationOnScreen = new int[2];

    public DragDropController(DragItemContainer dragItemContainer) {
        this.dragItemContainer = dragItemContainer;
    }

    /**
     * @return True if the drag is started, false if the drag is cancelled for some reason.
     */
    boolean handleDragStarted(View v, int x, int y) {
        v.getLocationOnScreen(locationOnScreen);
        x = x + locationOnScreen[0];
        y = y + locationOnScreen[1];
        final PhoneFavoriteSquareTileView tileView = dragItemContainer.getViewForLocation(x, y);
        if (tileView == null) {
            return false;
        }
        for (int i = 0; i < onDragDropListeners.size(); i++) {
            onDragDropListeners.get(i).onDragStarted(x, y, tileView);
        }

        return true;
    }

    public void handleDragHovered(View v, int x, int y) {
        v.getLocationOnScreen(locationOnScreen);
        final int screenX = x + locationOnScreen[0];
        final int screenY = y + locationOnScreen[1];
        final PhoneFavoriteSquareTileView view = dragItemContainer.getViewForLocation(screenX, screenY);
        for (int i = 0; i < onDragDropListeners.size(); i++) {
            onDragDropListeners.get(i).onDragHovered(screenX, screenY, view);
        }
    }

    public void handleDragFinished(int x, int y, boolean isRemoveView) {
        if (isRemoveView) {
            for (int i = 0; i < onDragDropListeners.size(); i++) {
                onDragDropListeners.get(i).onDroppedOnRemove();
            }
        }

        for (int i = 0; i < onDragDropListeners.size(); i++) {
            onDragDropListeners.get(i).onDragFinished(x, y);
        }
    }

    public void addOnDragDropListener(OnDragDropListener listener) {
        if (!onDragDropListeners.contains(listener)) {
            onDragDropListeners.add(listener);
        }
    }

    public void removeOnDragDropListener(OnDragDropListener listener) {
        if (onDragDropListeners.contains(listener)) {
            onDragDropListeners.remove(listener);
        }
    }

    /**
     * Callback interface used to retrieve views based on the current touch coordinates of the drag
     * event. The {@link DragItemContainer} houses the draggable views that this {@link
     * DragDropController} controls.
     */
    public interface DragItemContainer {

        PhoneFavoriteSquareTileView getViewForLocation(int x, int y);
    }
}
