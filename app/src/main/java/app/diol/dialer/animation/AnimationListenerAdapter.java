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

package app.diol.dialer.animation;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * Provides empty implementations of the methods in {@link AnimationListener} for convenience
 * reasons.
 */
public class AnimationListenerAdapter implements AnimationListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationStart(Animation animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationEnd(Animation animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
