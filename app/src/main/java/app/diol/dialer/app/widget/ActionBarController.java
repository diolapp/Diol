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

package app.diol.dialer.app.widget;

import android.animation.ValueAnimator;
import android.os.Bundle;

import app.diol.dialer.animation.AnimUtils.AnimationCallback;
import app.diol.dialer.common.LogUtil;

/**
 * Controls the various animated properties of the actionBar: showing/hiding, fading/revealing, and
 * collapsing/expanding, and assigns suitable properties to the actionBar based on the current state
 * of the UI.
 */
public class ActionBarController {

    private static final String KEY_IS_SLID_UP = "key_actionbar_is_slid_up";
    private static final String KEY_IS_FADED_OUT = "key_actionbar_is_faded_out";
    private static final String KEY_IS_EXPANDED = "key_actionbar_is_expanded";

    private ActivityUi activityUi;
    private SearchEditTextLayout searchBox;

    private boolean isActionBarSlidUp;
    private ValueAnimator animator;
    private final AnimationCallback fadeOutCallback =
            new AnimationCallback() {
                @Override
                public void onAnimationEnd() {
                    slideActionBar(true /* slideUp */, false /* animate */);
                }

                @Override
                public void onAnimationCancel() {
                    slideActionBar(true /* slideUp */, false /* animate */);
                }
            };

    public ActionBarController(ActivityUi activityUi, SearchEditTextLayout searchBox) {
        this.activityUi = activityUi;
        this.searchBox = searchBox;
    }

    /**
     * @return Whether or not the action bar is currently showing (both slid down and visible)
     */
    public boolean isActionBarShowing() {
        return !isActionBarSlidUp && !searchBox.isFadedOut();
    }

    /**
     * Called when the user has tapped on the collapsed search box, to start a new search query.
     */
    public void onSearchBoxTapped() {
        LogUtil.d("ActionBarController.onSearchBoxTapped", "isInSearchUi " + activityUi.isInSearchUi());
        if (!activityUi.isInSearchUi()) {
            searchBox.expand(true /* animate */, true /* requestFocus */);
        }
    }

    /**
     * Called when search UI has been exited for some reason.
     */
    public void onSearchUiExited() {
        LogUtil.d(
                "ActionBarController.onSearchUIExited",
                "isExpanded: %b, isFadedOut %b",
                searchBox.isExpanded(),
                searchBox.isFadedOut());
        if (searchBox.isExpanded()) {
            searchBox.collapse(true /* animate */);
        }
        if (searchBox.isFadedOut()) {
            searchBox.fadeIn();
        }

        slideActionBar(false /* slideUp */, false /* animate */);
    }

    /**
     * Called to indicate that the user is trying to hide the dialpad. Should be called before any
     * state changes have actually occurred.
     */
    public void onDialpadDown() {
        LogUtil.d(
                "ActionBarController.onDialpadDown",
                "isInSearchUi: %b, hasSearchQuery: %b, isFadedOut: %b, isExpanded: %b",
                activityUi.isInSearchUi(),
                activityUi.hasSearchQuery(),
                searchBox.isFadedOut(),
                searchBox.isExpanded());
        if (activityUi.isInSearchUi()) {
            if (searchBox.isFadedOut()) {
                searchBox.setVisible(true);
            }
            if (!searchBox.isExpanded()) {
                searchBox.expand(false /* animate */, false /* requestFocus */);
            }
            slideActionBar(false /* slideUp */, true /* animate */);
        }
    }

    /**
     * Called to indicate that the user is trying to show the dialpad. Should be called before any
     * state changes have actually occurred.
     */
    public void onDialpadUp() {
        LogUtil.d("ActionBarController.onDialpadUp", "isInSearchUi " + activityUi.isInSearchUi());
        if (activityUi.isInSearchUi()) {
            slideActionBar(true /* slideUp */, true /* animate */);
        } else {
            // From the lists fragment
            searchBox.fadeOut(fadeOutCallback);
        }
    }

    public void slideActionBar(boolean slideUp, boolean animate) {
        LogUtil.d("ActionBarController.slidingActionBar", "up: %b, animate: %b", slideUp, animate);

        if (animator != null && animator.isRunning()) {
            animator.cancel();
            animator.removeAllUpdateListeners();
        }
        if (animate) {
            animator = slideUp ? ValueAnimator.ofFloat(0, 1) : ValueAnimator.ofFloat(1, 0);
            animator.addUpdateListener(
                    animation -> {
                        final float value = (float) animation.getAnimatedValue();
                        setHideOffset((int) (activityUi.getActionBarHeight() * value));
                    });
            animator.start();
        } else {
            setHideOffset(slideUp ? activityUi.getActionBarHeight() : 0);
        }
        isActionBarSlidUp = slideUp;
    }

    public void setAlpha(float alphaValue) {
        searchBox.animate().alpha(alphaValue).start();
    }

    private void setHideOffset(int offset) {
        activityUi.setActionBarHideOffset(offset);
    }

    /**
     * Saves the current state of the action bar into a provided {@link Bundle}
     */
    public void saveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_IS_SLID_UP, isActionBarSlidUp);
        outState.putBoolean(KEY_IS_FADED_OUT, searchBox.isFadedOut());
        outState.putBoolean(KEY_IS_EXPANDED, searchBox.isExpanded());
    }

    /**
     * Restores the action bar state from a provided {@link Bundle}.
     */
    public void restoreInstanceState(Bundle inState) {
        isActionBarSlidUp = inState.getBoolean(KEY_IS_SLID_UP);

        final boolean isSearchBoxFadedOut = inState.getBoolean(KEY_IS_FADED_OUT);
        if (isSearchBoxFadedOut) {
            if (!searchBox.isFadedOut()) {
                searchBox.setVisible(false);
            }
        } else if (searchBox.isFadedOut()) {
            searchBox.setVisible(true);
        }

        final boolean isSearchBoxExpanded = inState.getBoolean(KEY_IS_EXPANDED);
        if (isSearchBoxExpanded) {
            if (!searchBox.isExpanded()) {
                searchBox.expand(false, false);
            }
        } else if (searchBox.isExpanded()) {
            searchBox.collapse(false);
        }
    }

    /**
     * This should be called after onCreateOptionsMenu has been called, when the actionbar has been
     * laid out and actually has a height.
     */
    public void restoreActionBarOffset() {
        slideActionBar(isActionBarSlidUp /* slideUp */, false /* animate */);
    }

    public interface ActivityUi {

        boolean isInSearchUi();

        boolean hasSearchQuery();

        int getActionBarHeight();

        void setActionBarHideOffset(int offset);
    }
}
