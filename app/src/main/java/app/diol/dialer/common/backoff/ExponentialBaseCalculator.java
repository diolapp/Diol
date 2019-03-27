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

package app.diol.dialer.common.backoff;

import app.diol.dialer.common.Assert;

/**
 * Given an initial delay, D, a maximum total backoff time, T, and a maximum number of backoffs, N,
 * this class calculates a base multiplier, b, and a scaling factor, g, such that the initial
 * backoff is g*b = D and the sum of the scaled backoffs is T = g*b + g*b^2 + g*b^3 + ... g*b^N
 *
 * <p>T/D = (1 - b^N)/(1 - b) but this cannot be written as a simple equation for b in terms of T, N
 * and D so instead use Newton's method (https://en.wikipedia.org/wiki/Newton%27s_method) to find an
 * approximate value for b.
 *
 * <p>Example usage using the {@code ExponentialBackoff} would be:
 *
 * <pre>
 *   // Retry with exponential backoff for up to 2 minutes, with an initial delay of 100 millis
 *   // and a maximum of 10 retries
 *   long initialDelayMillis = 100;
 *   int maxTries = 10;
 *   double base = ExponentialBaseCalculator.findBase(
 *       initialDelayMillis,
 *       TimeUnit.MINUTES.toMillis(2),
 *       maxTries);
 *   ExponentialBackoff backoff = new ExponentialBackoff(initialDelayMillis, base, maxTries);
 *   while (backoff.isInRange()) {
 *     ...
 *     long delay = backoff.getNextBackoff();
 *     // Wait for the indicated time...
 *   }
 * </pre>
 */
public final class ExponentialBaseCalculator {
    private static final int MAX_STEPS = 1000;
    private static final double DEFAULT_TOLERANCE_MILLIS = 1;

    /**
     * Calculate an exponential backoff base multiplier such that the first backoff delay will be as
     * specified and the sum of the delays after doing the indicated maximum number of backoffs will
     * be as specified.
     *
     * @throws IllegalArgumentException if the initial delay is greater than the total backoff time
     * @throws IllegalArgumentException if the maximum number of backoffs is not greater than 1
     * @throws IllegalStateException    if it fails to find an acceptable base multiplier
     */
    public static double findBase(
            long initialDelayMillis, long totalBackoffTimeMillis, int maximumBackoffs) {
        Assert.checkArgument(initialDelayMillis < totalBackoffTimeMillis);
        Assert.checkArgument(maximumBackoffs > 1);
        long scaledTotalTime = Math.round(((double) totalBackoffTimeMillis) / initialDelayMillis);
        double scaledTolerance = DEFAULT_TOLERANCE_MILLIS / initialDelayMillis;
        return getBaseImpl(scaledTotalTime, maximumBackoffs, scaledTolerance);
    }

    /**
     * T/D = (1 - b^N)/(1 - b) but this cannot be written as a simple equation for b in terms of T, D
     * and N so instead we use Newtons method to find an approximate value for b.
     *
     * <p>Let f(b) = (1 - b^N)/(1 - b) - T/D then we want to find b* such that f(b*) = 0, or more
     * precisely |f(b*)| < tolerance
     *
     * <p>Using Newton's method we can interatively find b* as follows: b1 = b0 - f(b0)/f'(b0), where
     * b0 is the current best guess for b* and b1 is the next best guess.
     *
     * <p>f'(b) = (f(b) + T/D - N*b^(N - 1))/(1 - b)
     *
     * <p>so
     *
     * <p>b1 = b0 - f(b0)(1 - b0)/(f(b0) + T/D - N*b0^(N - 1))
     */
    private static double getBaseImpl(long t, int n, double tolerance) {
        double b0 = 2; // Initial guess for b*
        double b0n = Math.pow(b0, n);
        double fb0 = f(b0, t, b0n);
        if (Math.abs(fb0) < tolerance) {
            // Initial guess was pretty good
            return b0;
        }

        for (int i = 0; i < MAX_STEPS; i++) {
            double fpb0 = fp(b0, t, n, fb0, b0n);
            double b1 = b0 - fb0 / fpb0;
            double b1n = Math.pow(b1, n);
            double fb1 = f(b1, t, b1n);

            if (Math.abs(fb1) < tolerance) {
                // Found an acceptable value
                return b1;
            }

            b0 = b1;
            b0n = b1n;
            fb0 = fb1;
        }

        throw new IllegalStateException("Failed to find base. Too many iterations.");
    }

    // Evaluate f(b), the function we are trying to find the zero for.
    // Note: passing b^N as a parameter so it only has to be calculated once
    private static double f(double b, long t, double bn) {
        return (1 - bn) / (1 - b) - t;
    }

    // Evaluate f'(b), the derivative of the function we are trying to find the zero for.
    // Note: passing f(b) and b^N as parameters for efficiency
    private static double fp(double b, long t, int n, double fb, double bn) {
        return (fb + t - n * bn / b) / (1 - b);
    }
}
