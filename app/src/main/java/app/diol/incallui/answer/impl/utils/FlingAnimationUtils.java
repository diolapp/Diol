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

package app.diol.incallui.answer.impl.utils;

import android.animation.Animator;
import android.content.Context;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

/**
 * Utility class to calculate general fling animation when the finger is released.
 */
public class FlingAnimationUtils {

    private static final float LINEAR_OUT_SLOW_IN_X2 = 0.35f;
    private static final float LINEAR_OUT_FASTER_IN_X2 = 0.5f;
    private static final float LINEAR_OUT_FASTER_IN_Y2_MIN = 0.4f;
    private static final float LINEAR_OUT_FASTER_IN_Y2_MAX = 0.5f;
    private static final float MIN_VELOCITY_DP_PER_SECOND = 250;
    private static final float HIGH_VELOCITY_DP_PER_SECOND = 3000;

    /**
     * Crazy math. http://en.wikipedia.org/wiki/B%C3%A9zier_curve
     */
    private static final float LINEAR_OUT_SLOW_IN_START_GRADIENT = 1.0f / LINEAR_OUT_SLOW_IN_X2;

    private Interpolator linearOutSlowIn;

    private float minVelocityPxPerSecond;
    private float maxLengthSeconds;
    private float highVelocityPxPerSecond;

    private AnimatorProperties animatorProperties = new AnimatorProperties();

    public FlingAnimationUtils(Context ctx, float maxLengthSeconds) {
        this.maxLengthSeconds = maxLengthSeconds;
        linearOutSlowIn = new PathInterpolator(0, 0, LINEAR_OUT_SLOW_IN_X2, 1);
        minVelocityPxPerSecond =
                MIN_VELOCITY_DP_PER_SECOND * ctx.getResources().getDisplayMetrics().density;
        highVelocityPxPerSecond =
                HIGH_VELOCITY_DP_PER_SECOND * ctx.getResources().getDisplayMetrics().density;
    }

    /**
     * Applies the interpolator and length to the animator, such that the fling animation is
     * consistent with the finger motion.
     *
     * @param animator  the animator to apply
     * @param currValue the current value
     * @param endValue  the end value of the animator
     * @param velocity  the current velocity of the motion
     */
    public void apply(Animator animator, float currValue, float endValue, float velocity) {
        apply(animator, currValue, endValue, velocity, Math.abs(endValue - currValue));
    }

    /**
     * Applies the interpolator and length to the animator, such that the fling animation is
     * consistent with the finger motion.
     *
     * @param animator  the animator to apply
     * @param currValue the current value
     * @param endValue  the end value of the animator
     * @param velocity  the current velocity of the motion
     */
    public void apply(
            ViewPropertyAnimator animator, float currValue, float endValue, float velocity) {
        apply(animator, currValue, endValue, velocity, Math.abs(endValue - currValue));
    }

    /**
     * Applies the interpolator and length to the animator, such that the fling animation is
     * consistent with the finger motion.
     *
     * @param animator    the animator to apply
     * @param currValue   the current value
     * @param endValue    the end value of the animator
     * @param velocity    the current velocity of the motion
     * @param maxDistance the maximum distance for this interaction; the maximum animation length gets
     *                    multiplied by the ratio between the actual distance and this value
     */
    public void apply(
            Animator animator, float currValue, float endValue, float velocity, float maxDistance) {
        AnimatorProperties properties = getProperties(currValue, endValue, velocity, maxDistance);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    /**
     * Applies the interpolator and length to the animator, such that the fling animation is
     * consistent with the finger motion.
     *
     * @param animator    the animator to apply
     * @param currValue   the current value
     * @param endValue    the end value of the animator
     * @param velocity    the current velocity of the motion
     * @param maxDistance the maximum distance for this interaction; the maximum animation length gets
     *                    multiplied by the ratio between the actual distance and this value
     */
    public void apply(
            ViewPropertyAnimator animator,
            float currValue,
            float endValue,
            float velocity,
            float maxDistance) {
        AnimatorProperties properties = getProperties(currValue, endValue, velocity, maxDistance);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    private AnimatorProperties getProperties(
            float currValue, float endValue, float velocity, float maxDistance) {
        float maxLengthSeconds =
                (float) (this.maxLengthSeconds * Math.sqrt(Math.abs(endValue - currValue) / maxDistance));
        float diff = Math.abs(endValue - currValue);
        float velAbs = Math.abs(velocity);
        float durationSeconds = LINEAR_OUT_SLOW_IN_START_GRADIENT * diff / velAbs;
        if (durationSeconds <= maxLengthSeconds) {
            animatorProperties.interpolator = linearOutSlowIn;
        } else if (velAbs >= minVelocityPxPerSecond) {

            // Cross fade between fast-out-slow-in and linear interpolator with current velocity.
            durationSeconds = maxLengthSeconds;
            VelocityInterpolator velocityInterpolator =
                    new VelocityInterpolator(durationSeconds, velAbs, diff);
            animatorProperties.interpolator =
                    new InterpolatorInterpolator(velocityInterpolator, linearOutSlowIn, linearOutSlowIn);
        } else {

            // Just use a normal interpolator which doesn't take the velocity into account.
            durationSeconds = maxLengthSeconds;
            animatorProperties.interpolator = Interpolators.FAST_OUT_SLOW_IN;
        }
        animatorProperties.duration = (long) (durationSeconds * 1000);
        return animatorProperties;
    }

    /**
     * Applies the interpolator and length to the animator, such that the fling animation is
     * consistent with the finger motion for the case when the animation is making something
     * disappear.
     *
     * @param animator    the animator to apply
     * @param currValue   the current value
     * @param endValue    the end value of the animator
     * @param velocity    the current velocity of the motion
     * @param maxDistance the maximum distance for this interaction; the maximum animation length gets
     *                    multiplied by the ratio between the actual distance and this value
     */
    public void applyDismissing(
            Animator animator, float currValue, float endValue, float velocity, float maxDistance) {
        AnimatorProperties properties =
                getDismissingProperties(currValue, endValue, velocity, maxDistance);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    /**
     * Applies the interpolator and length to the animator, such that the fling animation is
     * consistent with the finger motion for the case when the animation is making something
     * disappear.
     *
     * @param animator    the animator to apply
     * @param currValue   the current value
     * @param endValue    the end value of the animator
     * @param velocity    the current velocity of the motion
     * @param maxDistance the maximum distance for this interaction; the maximum animation length gets
     *                    multiplied by the ratio between the actual distance and this value
     */
    public void applyDismissing(
            ViewPropertyAnimator animator,
            float currValue,
            float endValue,
            float velocity,
            float maxDistance) {
        AnimatorProperties properties =
                getDismissingProperties(currValue, endValue, velocity, maxDistance);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    private AnimatorProperties getDismissingProperties(
            float currValue, float endValue, float velocity, float maxDistance) {
        float maxLengthSeconds =
                (float)
                        (this.maxLengthSeconds * Math.pow(Math.abs(endValue - currValue) / maxDistance, 0.5f));
        float diff = Math.abs(endValue - currValue);
        float velAbs = Math.abs(velocity);
        float y2 = calculateLinearOutFasterInY2(velAbs);

        float startGradient = y2 / LINEAR_OUT_FASTER_IN_X2;
        Interpolator mLinearOutFasterIn = new PathInterpolator(0, 0, LINEAR_OUT_FASTER_IN_X2, y2);
        float durationSeconds = startGradient * diff / velAbs;
        if (durationSeconds <= maxLengthSeconds) {
            animatorProperties.interpolator = mLinearOutFasterIn;
        } else if (velAbs >= minVelocityPxPerSecond) {

            // Cross fade between linear-out-faster-in and linear interpolator with current
            // velocity.
            durationSeconds = maxLengthSeconds;
            VelocityInterpolator velocityInterpolator =
                    new VelocityInterpolator(durationSeconds, velAbs, diff);
            InterpolatorInterpolator superInterpolator =
                    new InterpolatorInterpolator(velocityInterpolator, mLinearOutFasterIn, linearOutSlowIn);
            animatorProperties.interpolator = superInterpolator;
        } else {

            // Just use a normal interpolator which doesn't take the velocity into account.
            durationSeconds = maxLengthSeconds;
            animatorProperties.interpolator = Interpolators.FAST_OUT_LINEAR_IN;
        }
        animatorProperties.duration = (long) (durationSeconds * 1000);
        return animatorProperties;
    }

    /**
     * Calculates the y2 control point for a linear-out-faster-in path interpolator depending on the
     * velocity. The faster the velocity, the more "linear" the interpolator gets.
     *
     * @param velocity the velocity of the gesture.
     * @return the y2 control point for a cubic bezier path interpolator
     */
    private float calculateLinearOutFasterInY2(float velocity) {
        float t =
                (velocity - minVelocityPxPerSecond) / (highVelocityPxPerSecond - minVelocityPxPerSecond);
        t = Math.max(0, Math.min(1, t));
        return (1 - t) * LINEAR_OUT_FASTER_IN_Y2_MIN + t * LINEAR_OUT_FASTER_IN_Y2_MAX;
    }

    /**
     * @return the minimum velocity a gesture needs to have to be considered a fling
     */
    public float getMinVelocityPxPerSecond() {
        return minVelocityPxPerSecond;
    }

    /**
     * An interpolator which interpolates two interpolators with an interpolator.
     */
    private static final class InterpolatorInterpolator implements Interpolator {

        private Interpolator interpolator1;
        private Interpolator interpolator2;
        private Interpolator crossfader;

        InterpolatorInterpolator(
                Interpolator interpolator1, Interpolator interpolator2, Interpolator crossfader) {
            this.interpolator1 = interpolator1;
            this.interpolator2 = interpolator2;
            this.crossfader = crossfader;
        }

        @Override
        public float getInterpolation(float input) {
            float t = crossfader.getInterpolation(input);
            return (1 - t) * interpolator1.getInterpolation(input)
                    + t * interpolator2.getInterpolation(input);
        }
    }

    /**
     * An interpolator which interpolates with a fixed velocity.
     */
    private static final class VelocityInterpolator implements Interpolator {

        private float durationSeconds;
        private float velocity;
        private float diff;

        private VelocityInterpolator(float durationSeconds, float velocity, float diff) {
            this.durationSeconds = durationSeconds;
            this.velocity = velocity;
            this.diff = diff;
        }

        @Override
        public float getInterpolation(float input) {
            float time = input * durationSeconds;
            return time * velocity / diff;
        }
    }

    private static class AnimatorProperties {

        Interpolator interpolator;
        long duration;
    }
}
