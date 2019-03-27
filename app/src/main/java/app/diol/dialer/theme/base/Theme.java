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

package app.diol.dialer.theme.base;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface for theme.
 */
public interface Theme {

    int UNKNOWN = 0;
    int LIGHT = 1;
    int DARK = 2;
    int LIGHT_M2 = 3;

    @Type
    int getTheme();

    @StyleRes
    int getApplicationThemeRes();

    Context getThemedContext(Context context);

    LayoutInflater getThemedLayoutInflator(LayoutInflater inflater);

    @ColorInt
    int getColorIcon();

    @ColorInt
    int getColorIconSecondary();

    @ColorInt
    int getColorPrimary();

    @ColorInt
    int getColorPrimaryDark();

    @ColorInt
    int getColorAccent();

    @ColorInt
    int getTextColorSecondary();

    @ColorInt
    int getTextColorPrimary();

    @ColorInt
    int getColorTextOnUnthemedDarkBackground();

    @ColorInt
    int getColorIconOnUnthemedDarkBackground();

    /**
     * IntDef for the different themes Dialer supports.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UNKNOWN, LIGHT, DARK, LIGHT_M2})
    @interface Type {
    }
}
