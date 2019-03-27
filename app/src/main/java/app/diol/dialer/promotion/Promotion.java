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

package app.diol.dialer.promotion;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface for promotion bottom sheet.
 */
public interface Promotion {

    /**
     * Returns {@link PromotionType} for this promotion.
     */
    @PromotionType
    int getType();

    /**
     * Returns if this promotion should be shown. This usually means the promotion is enabled and not
     * dismissed yet.
     */
    boolean isEligibleToBeShown();

    /**
     * Called when this promotion is first time viewed by user.
     */
    default void onViewed() {
    }

    /**
     * Dismisses this promotion. This is called when user acknowledged the promotion.
     */
    void dismiss();

    /**
     * Returns title text of the promotion.
     */
    CharSequence getTitle();

    /**
     * Returns details text of the promotion.
     */
    CharSequence getDetails();

    /**
     * Returns resource id of the icon for the promotion.
     */
    @DrawableRes
    int getIconRes();

    /**
     * Type of promotion, which means promotion should be shown as a card in {@link
     * RecyclerView} or {@link
     * android.support.design.bottomsheet.BottomSheetBehavior}.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PromotionType.CARD, PromotionType.BOTTOM_SHEET})
    @interface PromotionType {
        /**
         * Shown as card in call log or voicemail tab.
         */
        int CARD = 1;

        /**
         * Shown as bottom sheet.
         */
        int BOTTOM_SHEET = 2;
    }
}
