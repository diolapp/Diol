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

package app.diol.bubble;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Creates and manages a bubble window from information in a {@link BubbleInfo}. Before creating, be
 * sure to check whether bubbles may be shown using {@code Settings.canDrawOverlays(context)} and
 * request permission if necessary
 */
public interface Bubble {

    /**
     * Make the bubble visible. Will show a short entrance animation as it enters. If the bubble is
     * already showing this method does nothing.
     */
    void show();

    /**
     * Hide the bubble.
     */
    void hide();

    /**
     * Returns whether the bubble is currently visible
     */
    boolean isVisible();

    /**
     * Returns whether the bubble is currently dismissed
     */
    boolean isDismissed();

    /**
     * Set the info for this Bubble to display
     *
     * @param bubbleInfo the BubbleInfo to display in this Bubble.
     */
    void setBubbleInfo(@NonNull BubbleInfo bubbleInfo);

    /**
     * Update the state and behavior of actions.
     *
     * @param actions the new state of the bubble's actions
     */
    void updateActions(@NonNull List<BubbleInfo.Action> actions);

    /**
     * Update the avatar from photo.
     *
     * @param avatar the new photo avatar in the bubble's primary button
     */
    void updatePhotoAvatar(@NonNull Drawable avatar);

    /**
     * Update the avatar.
     *
     * @param avatar the new avatar in the bubble's primary button
     */
    void updateAvatar(@NonNull Drawable avatar);

    /**
     * Display text. The bubble's drawer is not expandable while text is showing, and the drawer will
     * be closed if already open.
     *
     * @param text the text to display to the user
     */
    void showText(@NonNull CharSequence text);

}
