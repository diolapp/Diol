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

package app.diol.incallui.call;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used to notify interested parties of incoming video related events.
 */
public class InCallVideoCallCallbackNotifier {

    /**
     * Singleton instance of this class.
     */
    private static InCallVideoCallCallbackNotifier instance = new InCallVideoCallCallbackNotifier();

    /**
     * ConcurrentHashMap constructor params: 8 is initial table size, 0.9f is load factor before
     * resizing, 1 means we only expect a single thread to access the map so make only a single shard
     */
    private final Set<SurfaceChangeListener> surfaceChangeListeners =
            Collections.newSetFromMap(new ConcurrentHashMap<SurfaceChangeListener, Boolean>(8, 0.9f, 1));

    /**
     * Private constructor. Instance should only be acquired through getRunningInstance().
     */
    private InCallVideoCallCallbackNotifier() {
    }

    /**
     * Static singleton accessor method.
     */
    public static InCallVideoCallCallbackNotifier getInstance() {
        return instance;
    }

    /**
     * Adds a new {@link SurfaceChangeListener}.
     *
     * @param listener The listener.
     */
    public void addSurfaceChangeListener(@NonNull SurfaceChangeListener listener) {
        Objects.requireNonNull(listener);
        surfaceChangeListeners.add(listener);
    }

    /**
     * Remove a {@link SurfaceChangeListener}.
     *
     * @param listener The listener.
     */
    public void removeSurfaceChangeListener(@Nullable SurfaceChangeListener listener) {
        if (listener != null) {
            surfaceChangeListeners.remove(listener);
        }
    }

    /**
     * Inform listeners of a change to peer dimensions.
     *
     * @param call   The call.
     * @param width  New peer width.
     * @param height New peer height.
     */
    public void peerDimensionsChanged(DialerCall call, int width, int height) {
        for (SurfaceChangeListener listener : surfaceChangeListeners) {
            listener.onUpdatePeerDimensions(call, width, height);
        }
    }

    /**
     * Inform listeners of a change to camera dimensions.
     *
     * @param call   The call.
     * @param width  The new camera video width.
     * @param height The new camera video height.
     */
    public void cameraDimensionsChanged(DialerCall call, int width, int height) {
        for (SurfaceChangeListener listener : surfaceChangeListeners) {
            listener.onCameraDimensionsChange(call, width, height);
        }
    }

    /**
     * Listener interface for any class that wants to be notified of changes to the video surfaces.
     */
    public interface SurfaceChangeListener {

        /**
         * Called when the peer video feed changes dimensions. This can occur when the peer rotates
         * their device, changing the aspect ratio of the video signal.
         *
         * @param call The call which experienced a peer video
         */
        void onUpdatePeerDimensions(DialerCall call, int width, int height);

        /**
         * Called when the local camera changes dimensions. This occurs when a change in camera occurs.
         *
         * @param call   The call which experienced the camera dimension change.
         * @param width  The new camera video width.
         * @param height The new camera video height.
         */
        void onCameraDimensionsChange(DialerCall call, int width, int height);
    }
}
