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

package app.diol.dialer.main.impl.bottomnav;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.theme.base.ThemeComponent;

/**
 * Navigation item in a bottom nav.
 */
final class BottomNavItem extends LinearLayout {

    private ImageView image;
    private TextView text;
    private TextView notificationBadge;

    public BottomNavItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        image = findViewById(R.id.bottom_nav_item_image);
        text = findViewById(R.id.bottom_nav_item_text);
        notificationBadge = findViewById(R.id.notification_badge);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        int colorId =
                selected
                        ? ThemeComponent.get(getContext()).theme().getColorPrimary()
                        : ThemeComponent.get(getContext()).theme().getTextColorSecondary();
        image.setImageTintList(ColorStateList.valueOf(colorId));
        text.setTextColor(colorId);
    }

    void setup(@StringRes int stringRes, @DrawableRes int drawableRes) {
        text.setText(stringRes);
        image.setImageResource(drawableRes);
    }

    void setNotificationCount(int count) {
        Assert.checkArgument(count >= 0, "Invalid count: " + count);
        if (count == 0) {
            notificationBadge.setVisibility(View.INVISIBLE);
        } else {
            String countString = String.format(Integer.toString(count));

            boolean use99PlusCount =
                    ConfigProviderComponent.get(getContext())
                            .getConfigProvider()
                            .getBoolean("use_99_plus", false);
            boolean use9Plus = !use99PlusCount;

            if (use99PlusCount && count > 99) {
                countString = getContext().getString(R.string.bottom_nav_count_99_plus);
            } else if (use9Plus && count > 9) {
                countString = getContext().getString(R.string.bottom_nav_count_9_plus);
            }
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(countString);

            @Px int margin;
            if (countString.length() == 1) {
                margin = getContext().getResources().getDimensionPixelSize(R.dimen.badge_margin_length_1);
            } else if (countString.length() == 2) {
                margin = getContext().getResources().getDimensionPixelSize(R.dimen.badge_margin_length_2);
            } else {
                margin = getContext().getResources().getDimensionPixelSize(R.dimen.badge_margin_length_3);
            }

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) image.getLayoutParams();
            params.setMarginStart(margin);
            params.setMarginEnd(margin);
            image.setLayoutParams(params);
        }
    }
}
