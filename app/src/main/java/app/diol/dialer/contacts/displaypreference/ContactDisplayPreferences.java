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

package app.diol.dialer.contacts.displaypreference;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import java.util.Arrays;

import app.diol.R;

/**
 * Handles name ordering of a contact (Given name first or family name first.)
 */
public interface ContactDisplayPreferences {

    DisplayOrder getDisplayOrder();

    void setDisplayOrder(DisplayOrder displayOrder);

    SortOrder getSortOrder();

    void setSortOrder(SortOrder sortOrder);

    /**
     * Selects display name based on {@link DisplayOrder}
     */
    default String getDisplayName(@Nullable String primaryName, @Nullable String alternativeName) {
        if (TextUtils.isEmpty(alternativeName)) {
            return primaryName;
        }
        switch (getDisplayOrder()) {
            case PRIMARY:
                return primaryName;
            case ALTERNATIVE:
                return alternativeName;
        }
        throw new AssertionError("exhaustive switch");
    }

    /**
     * Selects sort name based on {@link SortOrder}
     */
    default String getSortName(@Nullable String primaryName, @Nullable String alternativeName) {
        if (TextUtils.isEmpty(alternativeName)) {
            return primaryName;
        }
        switch (getSortOrder()) {
            case BY_PRIMARY:
                return primaryName;
            case BY_ALTERNATIVE:
                return alternativeName;
        }
        throw new AssertionError("exhaustive switch");
    }

    /**
     * Order when displaying the name;
     */
    enum DisplayOrder implements StringResEnum {

        /**
         * The default display order of a name. For western names it will be "Given Family". For
         * unstructured names like east asian this will be the only order.
         *
         * @see android.provider.ContactsContract.Contacts#DISPLAY_NAME_PRIMARY
         */
        PRIMARY(R.string.display_options_view_given_name_first_value),
        /**
         * The alternative display order of a name. For western names it will be "Family, Given". For
         * unstructured names like east asian this order will be ignored and treated as primary.
         *
         * @see android.provider.ContactsContract.Contacts#DISPLAY_NAME_ALTERNATIVE
         */
        ALTERNATIVE(R.string.display_options_view_family_name_first_value);

        @StringRes
        private final int value;

        DisplayOrder(@StringRes int value) {
            this.value = value;
        }

        static DisplayOrder fromValue(Context context, String value) {
            return StringResEnum.fromValue(context, DisplayOrder.values(), value);
        }

        @Override
        @StringRes
        public int getStringRes() {
            return value;
        }
    }

    /**
     * Order when sorting the name. In some conventions, names are displayed as given name first, but
     * sorted by family name.
     */
    enum SortOrder implements StringResEnum {
        /**
         * Sort by the default display order of a name. For western names it will be "Given Family". For
         * unstructured names like east asian this will be the only order.
         *
         * @see android.provider.ContactsContract.Contacts#DISPLAY_NAME_PRIMARY
         */
        BY_PRIMARY(R.string.display_options_sort_by_given_name_value),
        /**
         * Sort by the alternative display order of a name. For western names it will be "Family,
         * Given". For unstructured names like east asian this order will be ignored and treated as
         * primary.
         *
         * @see android.provider.ContactsContract.Contacts#DISPLAY_NAME_ALTERNATIVE
         */
        BY_ALTERNATIVE(R.string.display_options_sort_by_family_name_value);

        @StringRes
        private final int value;

        SortOrder(@StringRes int value) {
            this.value = value;
        }

        static SortOrder fromValue(Context context, String value) {
            return StringResEnum.fromValue(context, SortOrder.values(), value);
        }

        @Override
        @StringRes
        public int getStringRes() {
            return value;
        }
    }

    /**
     * A enum whose value is a String from a Android string resource which can only be resolved at run
     * time.
     */
    interface StringResEnum {

        static <T extends Enum<T> & StringResEnum> T fromValue(
                Context context, T[] values, String value) {
            return Arrays.stream(values)
                    .filter(enumValue -> TextUtils.equals(enumValue.getValue(context), value))
                    // MoreCollectors.onlyElement() is not available to android guava.
                    .reduce(
                            (a, b) -> {
                                throw new AssertionError("multiple result");
                            })
                    .get();
        }

        @StringRes
        int getStringRes();

        default String getValue(Context context) {
            return context.getString(getStringRes());
        }
    }
}
