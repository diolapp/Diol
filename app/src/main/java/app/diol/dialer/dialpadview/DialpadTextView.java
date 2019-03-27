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

package app.diol.dialer.dialpadview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * This is a custom text view intended for rendering text on the dialpad. TextView has built-in
 * top/bottom padding to help account for ascenders/descenders.
 *
 * <p>Since vertical space is at a premium on the dialpad, particularly if the font size is scaled
 * to a larger default, for the dialpad we use this class to more precisely render characters
 * according to the precise amount of space they need.
 */
public class DialpadTextView extends AppCompatTextView {

    private Rect textBounds = new Rect();
    private String textStr;

    public DialpadTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Draw the text to fit within the height/width which have been specified during measurement.
     */
    @Override
    public void draw(Canvas canvas) {
        Paint paint = getPaint();

        // Without this, the draw does not respect the style's specified text color.
        paint.setColor(getCurrentTextColor());

        // The text bounds values are relative and can be negative,, so rather than specifying a
        // standard origin such as 0, 0, we need to use negative of the left/top bounds.
        // For example, the bounds may be: Left: 11, Right: 37, Top: -77, Bottom: 0
        canvas.drawText(textStr, -textBounds.left, -textBounds.top, paint);
    }

    /**
     * Calculate the pixel-accurate bounds of the text when rendered, and use that to specify the
     * height and width.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        textStr = getText().toString();
        getPaint().getTextBounds(textStr, 0, textStr.length(), textBounds);

        int width = resolveSize(textBounds.width(), widthMeasureSpec);
        int height = resolveSize(textBounds.height(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
