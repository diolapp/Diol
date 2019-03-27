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

package app.diol.dialer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import app.diol.R;
import app.diol.dialer.util.ViewUtil;

/**
 * TextView which resizes dynamically with respect to text length.
 */
public class ResizingTextTextView extends AppCompatTextView {

    private final int originalTextSize;
    private final int minTextSize;

    public ResizingTextTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        originalTextSize = (int) getTextSize();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ResizingText);
        minTextSize =
                (int) a.getDimension(R.styleable.ResizingText_resizing_text_min_size, originalTextSize);
        a.recycle();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        ViewUtil.resizeText(this, originalTextSize, minTextSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ViewUtil.resizeText(this, originalTextSize, minTextSize);
    }
}
