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

package app.diol.contacts.common.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Custom {@link ImageView} that improves layouting performance.
 *
 * <p>This improves the performance by not passing requestLayout() to its parent, taking advantage
 * of knowing that image size won't change once set.
 */
public class LayoutSuppressingImageView extends AppCompatImageView {

    public LayoutSuppressingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void requestLayout() {
        forceLayout();
    }
}
