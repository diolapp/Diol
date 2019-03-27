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

package app.diol.dialer.enrichedcall.simulator;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import app.diol.R;
import app.diol.dialer.common.Assert;

/**
 * Adapter for the RecyclerView in {@link EnrichedCallSimulatorActivity}.
 */
class SessionsAdapter extends RecyclerView.Adapter<SessionViewHolder> {

    /**
     * List of the string representation of all in-memory sessions
     */
    private List<String> sessionStrings;

    void setSessionStrings(@NonNull List<String> sessionStrings) {
        this.sessionStrings = Assert.isNotNull(sessionStrings);
    }

    @Override
    public SessionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new SessionViewHolder(inflater.inflate(R.layout.session_view_holder, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(SessionViewHolder viewHolder, int i) {
        viewHolder.updateSession(sessionStrings.get(i));
    }

    @Override
    public int getItemCount() {
        return sessionStrings.size();
    }
}
