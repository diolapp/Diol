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

package app.diol.incallui.baseui;

import android.os.Bundle;

/**
 * Base class for Presenters.
 */
public abstract class Presenter<U extends Ui> {

    private U ui;

    /**
     * Called after the UI view has been created. That is when fragment.onViewCreated() is called.
     *
     * @param ui The Ui implementation that is now ready to be used.
     */
    public void onUiReady(U ui) {
        this.ui = ui;
    }

    /**
     * Called when the UI view is destroyed in Fragment.onDestroyView().
     */
    public final void onUiDestroy(U ui) {
        onUiUnready(ui);
        this.ui = null;
    }

    /**
     * To be overriden by Presenter implementations. Called when the fragment is being destroyed but
     * before ui is set to null.
     */
    public void onUiUnready(U ui) {
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    public U getUi() {
        return ui;
    }
}
