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

package app.diol.dialer.feedback;

import android.content.Context;
import android.support.annotation.NonNull;

import app.diol.dialer.inject.HasRootComponent;
import app.diol.incallui.call.CallList;
import dagger.Subcomponent;

/**
 * Subcomponent that can be used to access the feedback implementation.
 */
@Subcomponent
public abstract class FeedbackComponent {
    public static FeedbackComponent get(Context context) {
        return ((HasComponent) ((HasRootComponent) context.getApplicationContext()).component())
                .feedbackComponent();
    }

    @NonNull
    public abstract CallList.Listener getCallFeedbackListener();

    @NonNull
    public abstract FeedbackSender getCallFeedbackSender();

    /**
     * Used to refer to the root application component.
     */
    public interface HasComponent {
        FeedbackComponent feedbackComponent();
    }
}
