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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import app.diol.dialer.common.Assert;

/**
 * Since {@link FloatingActionButton} is possibly the worst widget supported by the framework, we
 * need this class to work around several of it's bugs.
 *
 * <p>Current fixes:
 *
 * <ul>
 * <li>Being able to trigger click events twice.
 * <li>Banning setVisibility since 9 times out of 10, it just causes bad state.
 * </ul>
 * <p>
 * Planned fixes:
 *
 * <ul>
 * <li>Animating on first show/hide
 * <li>Being able to call show/hide rapidly and being in the proper state
 * <li>Having a proper 48x48 touch target in mini mode
 * </ul>
 */
public class DialerFloatingActionButton extends FloatingActionButton {

    public DialerFloatingActionButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public void show() {
        super.show();
        setClickable(true);
    }

    @Override
    public void show(@Nullable OnVisibilityChangedListener onVisibilityChangedListener) {
        super.show(onVisibilityChangedListener);
        setClickable(true);
    }

    @Override
    public void hide() {
        super.hide();
        setClickable(false);
    }

    @Override
    public void hide(@Nullable OnVisibilityChangedListener onVisibilityChangedListener) {
        super.hide(onVisibilityChangedListener);
        setClickable(false);
    }

    @Override
    public void setVisibility(int i) {
        throw Assert.createUnsupportedOperationFailException(
                "Do not call setVisibility, call show/hide instead");
    }
}
