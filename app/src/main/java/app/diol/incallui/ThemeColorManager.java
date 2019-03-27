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
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import app.diol.R;
import app.diol.contacts.common.util.MaterialColorMapUtils;
import app.diol.contacts.common.util.MaterialColorMapUtils.MaterialPalette;
import app.diol.incallui.call.DialerCall;

/**
 * Calculates the background color for the in call window. The background color is based on the SIM
 * and spam status.
 */
public class ThemeColorManager {
    private final MaterialColorMapUtils colorMap;
    @ColorInt
    private int primaryColor;
    @ColorInt
    private int secondaryColor;
    @ColorInt
    private int backgroundColorTop;
    @ColorInt
    private int backgroundColorMiddle;
    @ColorInt
    private int backgroundColorBottom;
    @ColorInt
    private int backgroundColorSolid;

    /**
     * If there is no actual call currently in the call list, this will be used as a fallback to
     * determine the theme color for InCallUI.
     */
    @Nullable
    private PhoneAccountHandle pendingPhoneAccountHandle;

    public ThemeColorManager(MaterialColorMapUtils colorMap) {
        this.colorMap = colorMap;
    }

    @ColorInt
    private static int applyAlpha(@ColorInt int color, @ColorInt int sourceColorWithAlpha) {
        return ColorUtils.setAlphaComponent(color, Color.alpha(sourceColorWithAlpha));
    }

    public void setPendingPhoneAccountHandle(@Nullable PhoneAccountHandle pendingPhoneAccountHandle) {
        this.pendingPhoneAccountHandle = pendingPhoneAccountHandle;
    }

    public void onForegroundCallChanged(Context context, @Nullable DialerCall newForegroundCall) {
        if (newForegroundCall == null) {
            updateThemeColors(context, getHighlightColor(context, pendingPhoneAccountHandle), false);
        } else {
            updateThemeColors(
                    context,
                    getHighlightColor(context, newForegroundCall.getAccountHandle()),
                    newForegroundCall.isSpam());
        }
    }

    private void updateThemeColors(Context context, @ColorInt int highlightColor, boolean isSpam) {
        MaterialPalette palette;
        if (isSpam) {
            palette =
                    colorMap.calculatePrimaryAndSecondaryColor(R.color.incall_call_spam_background_color);
            backgroundColorTop = context.getColor(R.color.incall_background_gradient_spam_top);
            backgroundColorMiddle = context.getColor(R.color.incall_background_gradient_spam_middle);
            backgroundColorBottom = context.getColor(R.color.incall_background_gradient_spam_bottom);
            backgroundColorSolid = context.getColor(R.color.incall_background_multiwindow_spam);
        } else {
            palette = colorMap.calculatePrimaryAndSecondaryColor(highlightColor);
            backgroundColorTop = context.getColor(R.color.incall_background_gradient_top);
            backgroundColorMiddle = context.getColor(R.color.incall_background_gradient_middle);
            backgroundColorBottom = context.getColor(R.color.incall_background_gradient_bottom);
            backgroundColorSolid = context.getColor(R.color.incall_background_multiwindow);
            if (highlightColor != PhoneAccount.NO_HIGHLIGHT_COLOR) {
                // The default background gradient has a subtle alpha. We grab that alpha and apply it to
                // the phone account color.
                backgroundColorTop = applyAlpha(palette.mPrimaryColor, backgroundColorTop);
                backgroundColorMiddle = applyAlpha(palette.mPrimaryColor, backgroundColorMiddle);
                backgroundColorBottom = applyAlpha(palette.mPrimaryColor, backgroundColorBottom);
                backgroundColorSolid = applyAlpha(palette.mPrimaryColor, backgroundColorSolid);
            }
        }

        primaryColor = palette.mPrimaryColor;
        secondaryColor = palette.mSecondaryColor;
    }

    @ColorInt
    private int getHighlightColor(Context context, @Nullable PhoneAccountHandle handle) {
        if (handle != null) {
            PhoneAccount account = context.getSystemService(TelecomManager.class).getPhoneAccount(handle);
            if (account != null) {
                return account.getHighlightColor();
            }
        }
        return PhoneAccount.NO_HIGHLIGHT_COLOR;
    }

    @ColorInt
    public int getPrimaryColor() {
        return primaryColor;
    }

    @ColorInt
    public int getSecondaryColor() {
        return secondaryColor;
    }

    @ColorInt
    public int getBackgroundColorTop() {
        return backgroundColorTop;
    }

    @ColorInt
    public int getBackgroundColorMiddle() {
        return backgroundColorMiddle;
    }

    @ColorInt
    public int getBackgroundColorBottom() {
        return backgroundColorBottom;
    }

    @ColorInt
    public int getBackgroundColorSolid() {
        return backgroundColorSolid;
    }
}
