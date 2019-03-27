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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import app.diol.dialer.common.LogUtil;
import app.diol.incallui.util.AccessibilityUtil;

/**
 * Creates the appropriate {@link AnswerMethod} for the circumstances.
 */
public class AnswerMethodFactory {
    private static boolean shouldUseTwoButtonMethodForTesting;

    @NonNull
    public static AnswerMethod createAnswerMethod(@NonNull AppCompatActivity activity) {
        if (needTwoButton(activity)) {
            return new TwoButtonMethod();
        } else {
            return new FlingUpDownMethod();
        }
    }

    public static boolean needsReplacement(@Nullable Fragment answerMethod) {
        //noinspection SimplifiableIfStatement
        if (answerMethod == null) {
            return true;
        }
        // If we have already started showing TwoButtonMethod, we should keep showing TwoButtonMethod.
        // Otherwise check if we need to change to TwoButtonMethod
        return !(answerMethod instanceof TwoButtonMethod) && needTwoButton((AppCompatActivity) answerMethod.getActivity());
    }

    @VisibleForTesting
    public static void setShouldUseTwoButtonMethodForTesting(boolean shouldUse) {
        shouldUseTwoButtonMethodForTesting = shouldUse;
    }

    private static boolean needTwoButton(@NonNull AppCompatActivity activity) {
        if (shouldUseTwoButtonMethodForTesting) {
            LogUtil.i("AnswerMethodFactory.needTwoButton", "enabled for testing");
            return true;
        }

        return AccessibilityUtil.isTouchExplorationEnabled(activity) || activity.isInMultiWindowMode();
    }
}
