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
 * Given an initial backoff delay, D, a base multiplier, B, and a total number of backoffs, N, this
 * class returns values in the exponential sequence, D, D*B, D*B^2, ... D*B^(N-1), ...
 *
 * <p>Example usage:
 *
 * <pre>
 *   long initialDelayMillis = 1000;
 *   double multiplier = 1.2;
 *   int backoffs = 10;
 *   ExponentialBackoff backoff = new ExponentialBackoff(initialDelayMillis, multiplier, backoffs);
 *   while (backoff.isInRange()) {
 *     ...
 *     sleep(backoff.getNextBackoff());
 *   }
 * </pre>
 *
 * <p>Note: the base multiplier can be calculated using {@code ExponentialBaseCalculator}
 */
public final class ExponentialBackoff {
    public final long initialDelayMillis;
    public final double baseMultiplier;
    public final int maximumBackoffs;
    private double nextBackoff;
    private int backoffCount;

    /**
     * Setup an exponential backoff with an initial delay, a base multiplier and a maximum number of
     * backoff steps.
     *
     * @throws IllegalArgumentException for negative argument values
     */
    public ExponentialBackoff(long initialDelayMillis, double baseMultiplier, int maximumBackoffs) {
        Assert.checkArgument(initialDelayMillis > 0);
        Assert.checkArgument(baseMultiplier > 0);
        Assert.checkArgument(maximumBackoffs > 0);
        this.initialDelayMillis = initialDelayMillis;
        this.baseMultiplier = baseMultiplier;
        this.maximumBackoffs = maximumBackoffs;
        reset();
    }

    /**
     * @return the next backoff time in the exponential sequence. Specifically, if D is the initial
     * delay, B is the base multiplier and N is the total number of backoffs, then the return
     * values will be: D, D*B, D*B^2, ... D*B^(N-1), ...
     */
    public long getNextBackoff() {
        long backoff = Math.round(nextBackoff);
        backoffCount++;
        nextBackoff *= baseMultiplier;
        return backoff;
    }

    /**
     * @return the number of times getNextBackoff() has been called
     */
    public int getBackoffCount() {
        return backoffCount;
    }

    /**
     * @return {@code true} if getNextBackoff() has been called less than the maximumBackoffs value
     * specified in the constructor.
     */
    public boolean isInRange() {
        return backoffCount < maximumBackoffs;
    }

    /**
     * Reset the sequence of backoff values so the next call to getNextBackoff() will return the
     * initial delay and getBackoffCount() will return 0
     */
    public void reset() {
        nextBackoff = initialDelayMillis;
        backoffCount = 0;
    }
}
