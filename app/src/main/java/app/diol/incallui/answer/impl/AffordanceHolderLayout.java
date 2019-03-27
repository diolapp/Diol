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

package app.diol.incallui.answer.impl;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import app.diol.incallui.answer.impl.affordance.SwipeButtonHelper;
import app.diol.incallui.answer.impl.affordance.SwipeButtonHelper.Callback;
import app.diol.incallui.answer.impl.affordance.SwipeButtonView;
import app.diol.incallui.util.AccessibilityUtil;

/**
 * Layout that delegates touches to its SwipeButtonHelper
 */
public class AffordanceHolderLayout extends FrameLayout {

    private SwipeButtonHelper affordanceHelper;

    private Callback affordanceCallback;

    public AffordanceHolderLayout(Context context) {
        this(context, null);
    }

    public AffordanceHolderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AffordanceHolderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        affordanceHelper =
                new SwipeButtonHelper(
                        new Callback() {
                            @Override
                            public void onAnimationToSideStarted(
                                    boolean rightPage, float translation, float vel) {
                                if (affordanceCallback != null) {
                                    affordanceCallback.onAnimationToSideStarted(rightPage, translation, vel);
                                }
                            }

                            @Override
                            public void onAnimationToSideEnded(boolean rightPage) {
                                if (affordanceCallback != null) {
                                    affordanceCallback.onAnimationToSideEnded(rightPage);
                                }
                            }

                            @Override
                            public float getMaxTranslationDistance() {
                                if (affordanceCallback != null) {
                                    return affordanceCallback.getMaxTranslationDistance();
                                }
                                return 0;
                            }

                            @Override
                            public void onSwipingStarted(boolean rightIcon) {
                                if (affordanceCallback != null) {
                                    affordanceCallback.onSwipingStarted(rightIcon);
                                }
                            }

                            @Override
                            public void onSwipingAborted() {
                                if (affordanceCallback != null) {
                                    affordanceCallback.onSwipingAborted();
                                }
                            }

                            @Override
                            public void onIconClicked(boolean rightIcon) {
                                if (affordanceCallback != null) {
                                    affordanceCallback.onIconClicked(rightIcon);
                                }
                            }

                            @Nullable
                            @Override
                            public SwipeButtonView getLeftIcon() {
                                if (affordanceCallback != null) {
                                    return affordanceCallback.getLeftIcon();
                                }
                                return null;
                            }

                            @Nullable
                            @Override
                            public SwipeButtonView getRightIcon() {
                                if (affordanceCallback != null) {
                                    return affordanceCallback.getRightIcon();
                                }
                                return null;
                            }

                            @Nullable
                            @Override
                            public View getLeftPreview() {
                                if (affordanceCallback != null) {
                                    return affordanceCallback.getLeftPreview();
                                }
                                return null;
                            }

                            @Nullable
                            @Override
                            public View getRightPreview() {
                                if (affordanceCallback != null) {
                                    affordanceCallback.getRightPreview();
                                }
                                return null;
                            }

                            @Override
                            public float getAffordanceFalsingFactor() {
                                if (affordanceCallback != null) {
                                    return affordanceCallback.getAffordanceFalsingFactor();
                                }
                                return 1.0f;
                            }
                        },
                        context);
    }

    public void setAffordanceCallback(@Nullable Callback callback) {
        affordanceCallback = callback;
        affordanceHelper.init();
    }

    public void startHintAnimation(boolean rightIcon, @Nullable Runnable onFinishListener) {
        affordanceHelper.startHintAnimation(rightIcon, onFinishListener);
    }

    public void animateHideLeftRightIcon() {
        affordanceHelper.animateHideLeftRightIcon();
    }

    public void reset(boolean animate) {
        affordanceHelper.reset(animate);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (AccessibilityUtil.isTouchExplorationEnabled(getContext())) {
            return false;
        }
        return affordanceHelper.onTouchEvent(event) || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return affordanceHelper.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        affordanceHelper.onConfigurationChanged();
    }
}
