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

package app.diol.dialer.app.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import app.diol.R;
import app.diol.dialer.util.OrientationUtil;
import app.diol.dialer.widget.EmptyContentView;

/**
 * Empty content view to be shown when dialpad is visible.
 */
public class DialpadSearchEmptyContentView extends EmptyContentView {

    public DialpadSearchEmptyContentView(Context context) {
        super(context);
    }

    @Override
    protected void inflateLayout() {
        int orientation =
                OrientationUtil.isLandscape(getContext()) ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;

        setOrientation(orientation);

        final LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.empty_content_view_dialpad_search, this);
    }
}
