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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.diol.R;
import app.diol.dialer.app.alert.AlertManager;
import app.diol.dialer.common.Assert;

/**
 * Manages "alerts" to be shown at the top of an call log to gain the user's attention.
 */
public class CallLogAlertManager implements AlertManager {

    private final CallLogAdapter adapter;
    private final View view;
    private final LayoutInflater inflater;
    private final ViewGroup parent;
    private final ViewGroup container;

    public CallLogAlertManager(CallLogAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        this.adapter = adapter;
        this.inflater = inflater;
        this.parent = parent;
        view = inflater.inflate(R.layout.call_log_alert_item, parent, false);
        container = (ViewGroup) view.findViewById(R.id.container);
    }

    @Override
    public View inflate(int layoutId) {
        return inflater.inflate(layoutId, container, false);
    }

    public RecyclerView.ViewHolder createViewHolder(ViewGroup parent) {
        Assert.checkArgument(
                parent == this.parent,
                "createViewHolder should be called with the same parent in constructor");
        return new AlertViewHolder(view);
    }

    public boolean isEmpty() {
        return container.getChildCount() == 0;
    }

    public boolean contains(View view) {
        return container.indexOfChild(view) != -1;
    }

    @Override
    public void clear() {
        container.removeAllViews();
        adapter.notifyItemRemoved(CallLogAdapter.ALERT_POSITION);
    }

    @Override
    public void add(View view) {
        if (contains(view)) {
            return;
        }
        container.addView(view);
        if (container.getChildCount() == 1) {
            // Was empty before
            adapter.notifyItemInserted(CallLogAdapter.ALERT_POSITION);
        }
    }

    /**
     * Does nothing. The view this ViewHolder show is directly managed by {@link CallLogAlertManager}
     */
    private static class AlertViewHolder extends RecyclerView.ViewHolder {
        private AlertViewHolder(View view) {
            super(view);
        }
    }
}
