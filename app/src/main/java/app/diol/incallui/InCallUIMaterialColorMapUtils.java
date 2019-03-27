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

package app.diol.incallui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.telecom.PhoneAccount;

import app.diol.R;
import app.diol.contacts.common.util.MaterialColorMapUtils;
import app.diol.dialer.theme.base.ThemeComponent;

public class InCallUIMaterialColorMapUtils extends MaterialColorMapUtils {

    private final TypedArray primaryColors;
    private final TypedArray secondaryColors;
    private final Resources resources;
    private final Context context;

    public InCallUIMaterialColorMapUtils(Context context) {
        super(context.getResources());
        this.resources = context.getResources();
        this.context = context;
        primaryColors = resources.obtainTypedArray(R.array.background_colors);
        secondaryColors = resources.obtainTypedArray(R.array.background_colors_dark);
    }

    /**
     * {@link Resources#getColor(int) used for compatibility
     */
    @SuppressWarnings("deprecation")
    public static MaterialPalette getDefaultPrimaryAndSecondaryColors(Context context) {
        final int primaryColor = ThemeComponent.get(context).theme().getColorPrimary();
        final int secondaryColor = ThemeComponent.get(context).theme().getColorPrimaryDark();
        return new MaterialPalette(primaryColor, secondaryColor);
    }

    /**
     * Currently the InCallUI color will only vary by SIM color which is a list of colors defined in
     * the background_colors array, so first search the list for the matching color and fall back to
     * the closest matching color if an exact match does not exist.
     */
    @Override
    public MaterialPalette calculatePrimaryAndSecondaryColor(int color) {
        if (color == PhoneAccount.NO_HIGHLIGHT_COLOR) {
            return getDefaultPrimaryAndSecondaryColors(context);
        }

        for (int i = 0; i < primaryColors.length(); i++) {
            if (primaryColors.getColor(i, 0) == color) {
                return new MaterialPalette(primaryColors.getColor(i, 0), secondaryColors.getColor(i, 0));
            }
        }

        // The color isn't in the list, so use the superclass to find an approximate color.
        return super.calculatePrimaryAndSecondaryColor(color);
    }
}
