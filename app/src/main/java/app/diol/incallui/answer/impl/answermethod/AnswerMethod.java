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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import app.diol.dialer.common.FragmentUtils;

/**
 * A fragment that can be used to answer/reject calls.
 */
public abstract class AnswerMethod extends Fragment {

    public abstract void setHintText(@Nullable CharSequence hintText);

    public abstract void setShowIncomingWillDisconnect(boolean incomingWillDisconnect);

    public void setContactPhoto(@Nullable Drawable contactPhoto) {
        // default implementation does nothing. Only some AnswerMethods show a photo
    }

    protected AnswerMethodHolder getParent() {
        return FragmentUtils.getParentUnsafe(this, AnswerMethodHolder.class);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentUtils.checkParent(this, AnswerMethodHolder.class);
    }
}
