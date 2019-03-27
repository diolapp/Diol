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

package app.diol.contacts.common;

import android.content.Context;

import java.util.Iterator;
import java.util.List;

/**
 * Class used for collapsing data items into groups of similar items. The data items that should be
 * collapsible should implement the Collapsible interface. The class also contains a utility
 * function that takes an ArrayList of items and returns a list of the same items collapsed into
 * groups.
 */
public final class Collapser {

    /*
     * The Collapser uses an n^2 algorithm so we don't want it to run on
     * lists beyond a certain size. This specifies the maximum size to collapse.
     */
    private static final int MAX_LISTSIZE_TO_COLLAPSE = 20;

    /*
     * This utility class cannot be instantiated.
     */
    private Collapser() {
    }

    /**
     * Collapses a list of Collapsible items into a list of collapsed items. Items are collapsed if
     * {@link Collapsible#shouldCollapseWith(Object)} returns true, and are collapsed through the
     * {@Link Collapsible#collapseWith(Object)} function implemented by the data item.
     *
     * @param list List of Objects of type <T extends Collapsible<T>> to be collapsed.
     */
    public static <T extends Collapsible<T>> void collapseList(List<T> list, Context context) {

        int listSize = list.size();
        // The algorithm below is n^2 so don't run on long lists
        if (listSize > MAX_LISTSIZE_TO_COLLAPSE) {
            return;
        }

        for (int i = 0; i < listSize; i++) {
            T iItem = list.get(i);
            if (iItem != null) {
                for (int j = i + 1; j < listSize; j++) {
                    T jItem = list.get(j);
                    if (jItem != null) {
                        if (iItem.shouldCollapseWith(jItem, context)) {
                            iItem.collapseWith(jItem);
                            list.set(j, null);
                        } else if (jItem.shouldCollapseWith(iItem, context)) {
                            jItem.collapseWith(iItem);
                            list.set(i, null);
                            break;
                        }
                    }
                }
            }
        }

        // Remove the null items
        Iterator<T> itr = list.iterator();
        while (itr.hasNext()) {
            if (itr.next() == null) {
                itr.remove();
            }
        }
    }

    /*
     * Interface implemented by data types that can be collapsed into groups of similar data. This
     * can be used for example to collapse similar contact data items into a single item.
     */
    public interface Collapsible<T> {

        void collapseWith(T t);

        boolean shouldCollapseWith(T t, Context context);
    }
}
