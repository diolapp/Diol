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

package app.diol.dialer.app.calllog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.diol.R;
import app.diol.dialer.app.alert.AlertManager;

/**
 * Alert manager controls modal view to show message in call log. When modal view is shown, regular
 * call log will be hidden.
 */
public class CallLogModalAlertManager implements AlertManager {

    private final Listener listener;
    private final ViewGroup parent;
    private final ViewGroup container;
    private final LayoutInflater inflater;
    public CallLogModalAlertManager(LayoutInflater inflater, ViewGroup parent, Listener listener) {
        this.inflater = inflater;
        this.parent = parent;
        this.listener = listener;
        container = (ViewGroup) parent.findViewById(R.id.modal_message_container);
    }

    @Override
    public View inflate(int layoutId) {
        return inflater.inflate(layoutId, parent, false);
    }

    @Override
    public void add(View view) {
        if (contains(view)) {
            return;
        }
        container.addView(view);
        listener.onShowModalAlert(true);
    }

    @Override
    public void clear() {
        container.removeAllViews();
        listener.onShowModalAlert(false);
    }

    public boolean isEmpty() {
        return container.getChildCount() == 0;
    }

    public boolean contains(View view) {
        return container.indexOfChild(view) != -1;
    }

    interface Listener {
        void onShowModalAlert(boolean show);
    }
}
