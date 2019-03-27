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

package app.diol.dialer.callcomposer.camera.camerafocus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

import java.util.List;

/**
 * Pie menu item.
 */
public class PieItem {

    // Gray out the view when disabled
    private static final float ENABLED_ALPHA = 1;
    private static final float DISABLED_ALPHA = (float) 0.3;
    private Drawable drawable;
    private int level;
    private float center;
    private float start;
    private float sweep;
    private float animate;
    private int inner;
    private int outer;
    private boolean selected;
    private boolean enabled;
    private List<PieItem> items;
    private Path path;
    private OnClickListener onClickListener;
    private float alpha;
    public PieItem(Drawable drawable, int level) {
        this.drawable = drawable;
        this.level = level;
        setAlpha(1f);
        enabled = true;
        setAnimationAngle(getAnimationAngle());
        start = -1;
        center = -1;
    }

    public boolean hasItems() {
        return items != null;
    }

    public List<PieItem> getItems() {
        return items;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path p) {
        path = p;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        drawable.setAlpha((int) (255 * alpha));
    }

    private float getAnimationAngle() {
        return animate;
    }

    public void setAnimationAngle(float a) {
        animate = a;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (this.enabled) {
            setAlpha(ENABLED_ALPHA);
        } else {
            setAlpha(DISABLED_ALPHA);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean s) {
        selected = s;
    }

    public int getLevel() {
        return level;
    }

    public void setGeometry(float st, float sw, int inside, int outside) {
        start = st;
        sweep = sw;
        inner = inside;
        outer = outside;
    }

    public float getCenter() {
        return center;
    }

    public float getStart() {
        return start;
    }

    public float getStartAngle() {
        return start + animate;
    }

    public float getSweep() {
        return sweep;
    }

    public int getInnerRadius() {
        return inner;
    }

    public int getOuterRadius() {
        return outer;
    }

    public void setOnClickListener(OnClickListener listener) {
        onClickListener = listener;
    }

    public void performClick() {
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    public int getIntrinsicWidth() {
        return drawable.getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        return drawable.getIntrinsicHeight();
    }

    public void setBounds(int left, int top, int right, int bottom) {
        drawable.setBounds(left, top, right, bottom);
    }

    public void draw(Canvas canvas) {
        drawable.draw(canvas);
    }

    public void setImageResource(Context context, int resId) {
        Drawable d = context.getResources().getDrawable(resId).mutate();
        d.setBounds(drawable.getBounds());
        drawable = d;
        setAlpha(alpha);
    }

    /**
     * Listener to detect pie item clicks.
     */
    public interface OnClickListener {
        void onClick(PieItem item);
    }
}
