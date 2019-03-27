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
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.theme.base.ThemeComponent;

/**
 * Toolbar widget for Dialer.
 */
public class DialerToolbar extends Toolbar {

    private final TextView title;
    private final BidiTextView subtitle;

    public DialerToolbar(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        inflate(context, R.layout.dialer_toolbar, this);
        title = (TextView) findViewById(R.id.title);
        subtitle = (BidiTextView) findViewById(R.id.subtitle);

        setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
        setBackgroundColor(ThemeComponent.get(context).theme().getColorPrimary());
        setNavigationIcon(R.drawable.quantum_ic_close_white_24);
        setNavigationContentDescription(R.string.toolbar_close);
        setNavigationOnClickListener(v -> ((AppCompatActivity) context).finish());
        setPaddingRelative(
                getPaddingStart(),
                getPaddingTop(),
                getResources().getDimensionPixelSize(R.dimen.toolbar_end_padding),
                getPaddingBottom());
    }

    @Override
    public void setTitle(@StringRes int id) {
        setTitle(getResources().getString(id));
    }

    @Override
    public void setTitle(CharSequence charSequence) {
        title.setText(charSequence);
    }

    @Override
    public void setSubtitle(@StringRes int id) {
        setSubtitle(getResources().getString(id));
    }

    @Override
    public void setSubtitle(CharSequence charSequence) {
        if (charSequence != null) {
            subtitle.setText(charSequence);
            subtitle.setVisibility(VISIBLE);
        }
    }
}
