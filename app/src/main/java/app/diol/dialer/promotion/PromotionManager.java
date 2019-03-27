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

import com.google.common.collect.ImmutableList;

import java.util.Optional;

import javax.inject.Inject;

import app.diol.dialer.promotion.Promotion.PromotionType;

/**
 * A class to manage all promotion cards/bottom sheet.
 *
 * <p>Only one promotion with highest priority will be shown at a time no matter type. So if there
 * are one card and one bottom sheet promotion, either one will be shown instead of both.
 */
public final class PromotionManager {

    /**
     * Promotion priority order list. Promotions with higher priority must be added first.
     */
    private ImmutableList<Promotion> priorityPromotionList;

    @Inject
    public PromotionManager(ImmutableList<Promotion> priorityPromotionList) {
        this.priorityPromotionList = priorityPromotionList;
    }

    /**
     * Returns promotion should show with highest priority. {@link Optional#empty()} if no promotion
     * should be shown with given {@link PromotionType}.
     *
     * <p>e.g. if FooPromotion(card, high priority) and BarPromotion(bottom sheet, low priority) are
     * both enabled, getHighestPriorityPromotion(CARD) returns Optional.of(FooPromotion) but
     * getHighestPriorityPromotion(BOTTOM_SHEET) returns {@link Optional#empty()}.
     *
     * <p>Currently it only supports promotion in call log tab.
     *
     * <p>TODO(wangqi): add support for other tabs.
     */
    public Optional<Promotion> getHighestPriorityPromotion(@PromotionType int type) {
        for (Promotion promotion : priorityPromotionList) {
            if (promotion.isEligibleToBeShown()) {
                if (promotion.getType() == type) {
                    return Optional.of(promotion);
                } else {
                    // Returns empty promotion since it's not the type looking for and only one promotion
                    // should be shown at a time.
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}
