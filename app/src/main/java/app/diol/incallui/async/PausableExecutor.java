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

package app.diol.incallui.async;

import java.util.concurrent.Executor;

/**
 * Executor that can be used to easily synchronize testing and production code. Production code
 * should call {@link #milestone()} at points in the code where the state of the system is worthy of
 * testing. In a test scenario, this method will pause execution until the test acknowledges the
 * milestone through the use of {@link #ackMilestoneForTesting()}.
 */
public interface PausableExecutor extends Executor {

    /**
     * Method called from asynchronous production code to inform this executor that it has reached a
     * point that puts the system into a state worth testing. TestableExecutors intended for use in a
     * testing environment should cause the calling thread to block. In the production environment
     * this should be a no-op.
     */
    void milestone();

    /**
     * Method called from the test code to inform this executor that the state of the production
     * system at the current milestone has been sufficiently tested. Every milestone must be
     * acknowledged.
     */
    void ackMilestoneForTesting();

    /**
     * Method called from the test code to inform this executor that the tests are finished with all
     * milestones. Future calls to {@link #milestone()} or {@link #awaitMilestoneForTesting()} should
     * return immediately.
     */
    void ackAllMilestonesForTesting();

    /**
     * Method called from the test code to block until a milestone has been reached in the production
     * code.
     */
    void awaitMilestoneForTesting() throws InterruptedException;
}
