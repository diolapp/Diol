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

package app.diol.dialer.searchfragment.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import app.diol.R;

/**
 * ViewHolder for header rows in {@link NewSearchFragment}.
 */
final class HeaderViewHolder extends RecyclerView.ViewHolder {

    private final TextView header;

    HeaderViewHolder(View view) {
        super(view);
        header = view.findViewById(R.id.header);
    }

    public void setHeader(String header) {
        this.header.setText(header);
    }
}
