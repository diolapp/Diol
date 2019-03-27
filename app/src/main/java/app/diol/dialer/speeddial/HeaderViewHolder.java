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

package app.diol.dialer.speeddial;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import app.diol.R;

/**
 * ViewHolder for headers in {@link SpeedDialFragment}.
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final SpeedDialHeaderListener listener;
    private final TextView headerText;
    private final Button addButton;

    public HeaderViewHolder(View view, SpeedDialHeaderListener listener) {
        super(view);
        this.listener = listener;
        headerText = view.findViewById(R.id.speed_dial_header_text);
        addButton = view.findViewById(R.id.speed_dial_add_button);
        addButton.setOnClickListener(this);
    }

    public void setHeaderText(@StringRes int header) {
        headerText.setText(header);
    }

    public void showAddButton(boolean show) {
        addButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        listener.onAddFavoriteClicked();
    }

    /**
     * Listener/Callback for {@link HeaderViewHolder} parents.
     */
    public interface SpeedDialHeaderListener {

        /**
         * Called when the user wants to add a contact to their favorites.
         */
        void onAddFavoriteClicked();
    }
}
