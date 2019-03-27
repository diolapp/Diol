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

package app.diol.incallui.incall.impl;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import app.diol.dialer.common.Assert;
import app.diol.incallui.incall.impl.MappedButtonConfig.MappingInfo;
import app.diol.incallui.incall.protocol.InCallButtonIds;

/**
 * Determines where logical buttons should be placed in the {@link InCallFragment} based on the
 * provided mapping.
 *
 * <p>The button placement returned by a call to {@link #getButtonPlacement(int, Set)} is created as
 * follows: one button is placed at each UI slot, using the provided mapping to resolve conflicts.
 * Any allowed buttons that were not chosen for their desired slot are filled in at the end of the
 * list until it becomes the proper size.
 */
@Immutable
final class ButtonChooser {

    private final MappedButtonConfig config;

    public ButtonChooser(@NonNull MappedButtonConfig config) {
        this.config = Assert.isNotNull(config);
    }

    /**
     * Returns the buttons that should be shown in the {@link InCallFragment}, ordered appropriately.
     *
     * @param numUiButtons    the number of ui buttons available.
     * @param allowedButtons  the {@link InCallButtonIds} that can be shown.
     * @param disabledButtons the {@link InCallButtonIds} that can be shown but in disabled stats.
     * @return an immutable list whose size is at most {@code numUiButtons}, containing the buttons to
     * show.
     */
    @NonNull
    public List<Integer> getButtonPlacement(
            int numUiButtons,
            @NonNull Set<Integer> allowedButtons,
            @NonNull Set<Integer> disabledButtons) {
        Assert.isNotNull(allowedButtons);
        Assert.checkArgument(numUiButtons >= 0);

        if (numUiButtons == 0 || allowedButtons.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> placedButtons = new ArrayList<>();
        List<Integer> conflicts = new ArrayList<>();
        placeButtonsInSlots(numUiButtons, allowedButtons, placedButtons, conflicts);
        placeConflictsInOpenSlots(
                numUiButtons, allowedButtons, disabledButtons, placedButtons, conflicts);
        return Collections.unmodifiableList(placedButtons);
    }

    private void placeButtonsInSlots(
            int numUiButtons,
            @NonNull Set<Integer> allowedButtons,
            @NonNull List<Integer> placedButtons,
            @NonNull List<Integer> conflicts) {
        List<Integer> configuredSlots = config.getOrderedMappedSlots();
        for (int i = 0; i < configuredSlots.size() && placedButtons.size() < numUiButtons; ++i) {
            int slotNumber = configuredSlots.get(i);
            List<Integer> potentialButtons = config.getButtonsForSlot(slotNumber);
            Collections.sort(potentialButtons, config.getSlotComparator());
            for (int j = 0; j < potentialButtons.size(); ++j) {
                if (allowedButtons.contains(potentialButtons.get(j))) {
                    placedButtons.add(potentialButtons.get(j));
                    conflicts.addAll(potentialButtons.subList(j + 1, potentialButtons.size()));
                    break;
                }
            }
        }
    }

    private void placeConflictsInOpenSlots(
            int numUiButtons,
            @NonNull Set<Integer> allowedButtons,
            @NonNull Set<Integer> disabledButtons,
            @NonNull List<Integer> placedButtons,
            @NonNull List<Integer> conflicts) {
        Collections.sort(conflicts, config.getConflictComparator());
        for (Integer conflict : conflicts) {
            if (placedButtons.size() >= numUiButtons) {
                return;
            }

            // If the conflict button is allowed but disabled, don't place it since it probably will
            // move when it's enabled.
            if (!allowedButtons.contains(conflict) || disabledButtons.contains(conflict)) {
                continue;
            }

            if (isMutuallyExclusiveButtonAvailable(
                    config.lookupMappingInfo(conflict).getMutuallyExclusiveButton(), allowedButtons)) {
                continue;
            }
            placedButtons.add(conflict);
        }
    }

    private boolean isMutuallyExclusiveButtonAvailable(
            int mutuallyExclusiveButton, @NonNull Set<Integer> allowedButtons) {
        if (mutuallyExclusiveButton == MappingInfo.NO_MUTUALLY_EXCLUSIVE_BUTTON_SET) {
            return false;
        }
        if (allowedButtons.contains(mutuallyExclusiveButton)) {
            return true;
        }
        return false;
    }
}
