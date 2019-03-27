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

package app.diol.incallui.answer.impl.answermethod;

import android.support.annotation.FloatRange;

/**
 * Defines callbacks {@link AnswerMethod AnswerMethods} may use to update their parent.
 */
public interface AnswerMethodHolder {

    /**
     * Update animation based on method progress.
     *
     * @param answerProgress float representing progress. -1 is fully declined, 1 is fully answered,
     *                       and 0 is neutral.
     */
    void onAnswerProgressUpdate(@FloatRange(from = -1f, to = 1f) float answerProgress);

    /**
     * Answer the current call.
     */
    void answerFromMethod();

    /**
     * Reject the current call.
     */
    void rejectFromMethod();

    /**
     * Set AnswerProgress to zero (not due to normal updates).
     */
    void resetAnswerProgress();

    /**
     * Check whether the current call is a video call.
     *
     * @return true iff the current call is a video call.
     */
    boolean isVideoCall();

    boolean isVideoUpgradeRequest();

    boolean isRttCall();
}
