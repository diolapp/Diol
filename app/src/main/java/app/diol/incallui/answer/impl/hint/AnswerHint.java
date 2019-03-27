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

package app.diol.incallui.answer.impl.hint;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Interface to overlay a hint of how to answer the call.
 */
public interface AnswerHint {

    /**
     * Inflates the hint's layout into the container.
     *
     * <p>TODO(twyen): if the hint becomes more dependent on other UI elements of the AnswerFragment,
     * should put put and hintText into another data structure.
     */
    void onCreateView(LayoutInflater inflater, ViewGroup container, View puck, TextView hintText);

    /**
     * Called when the puck bounce animation begins.
     */
    void onBounceStart();

    /**
     * Called when the bounce animation has ended (transitioned into other animations). The hint
     * should reset itself.
     */
    void onBounceEnd();

    /**
     * Called when the call is accepted or rejected through user interaction.
     */
    void onAnswered();
}
