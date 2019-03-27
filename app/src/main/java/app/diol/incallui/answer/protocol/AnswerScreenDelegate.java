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

package app.diol.incallui.answer.protocol;

import android.support.annotation.FloatRange;

import app.diol.incallui.incalluilock.InCallUiLock;

/**
 * Callbacks implemented by the container app for this module.
 */
public interface AnswerScreenDelegate {

    void onAnswerScreenUnready();

    void onRejectCallWithMessage(String message);

    void onAnswer(boolean answerVideoAsAudio);

    void onReject();

    void onSpeakEasyCall();

    void onAnswerAndReleaseCall();

    void onAnswerAndReleaseButtonEnabled();

    void onAnswerAndReleaseButtonDisabled();

    /**
     * Sets the window background color based on foreground call's theme and the given progress. This
     * is called from the answer UI to animate the accept and reject action.
     *
     * <p>When the user is rejecting we animate the background color to a mostly transparent gray. The
     * end effect is that the home screen shows through.
     *
     * @param progress float from -1 to 1. -1 is fully rejected, 1 is fully accepted, and 0 is neutral
     */
    void updateWindowBackgroundColor(@FloatRange(from = -1f, to = 1.0f) float progress);

    /**
     * Returns true if any answer/reject action timed out.
     */
    boolean isActionTimeout();

    InCallUiLock acquireInCallUiLock(String tag);
}
