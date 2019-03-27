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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * ViewHolder for headers in {@link SpeedDialFragment}.
 */
public class RemoveViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final View removeViewContent;

    RemoveViewHolder(View view) {
        super(view);
        removeViewContent = view;
    }

    void show() {
        removeViewContent.setVisibility(View.VISIBLE);
        removeViewContent.setAlpha(0);
        removeViewContent.animate().alpha(1).start();
    }

    void hide() {
        removeViewContent.setVisibility(View.INVISIBLE);
        removeViewContent.setAlpha(1);
        removeViewContent.animate().alpha(0).start();
    }

    @Override
    public void onClick(View v) {
        // Not clickable
    }
}
