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

package app.diol.incallui;

import android.support.annotation.FloatRange;

import app.diol.incallui.answer.protocol.AnswerScreenDelegate;
import app.diol.incallui.incalluilock.InCallUiLock;

/**
 * Stub implementation of the answer screen delegate. Used to keep the answer fragment visible when
 * no call exists.
 */
public class AnswerScreenPresenterStub implements AnswerScreenDelegate {
    @Override
    public void onAnswerScreenUnready() {
    }

    @Override
    public void onRejectCallWithMessage(String message) {
    }

    @Override
    public void onAnswer(boolean answerVideoAsAudio) {
    }

    @Override
    public void onReject() {
    }

    @Override
    public void onSpeakEasyCall() {
    }

    @Override
    public void onAnswerAndReleaseCall() {
    }

    @Override
    public void onAnswerAndReleaseButtonEnabled() {
    }

    @Override
    public void onAnswerAndReleaseButtonDisabled() {
    }

    @Override
    public void updateWindowBackgroundColor(@FloatRange(from = -1f, to = 1.0f) float progress) {
    }

    @Override
    public boolean isActionTimeout() {
        return false;
    }

    @Override
    public InCallUiLock acquireInCallUiLock(String tag) {
        return InCallPresenter.getInstance().acquireInCallUiLock(tag);
    }
}
