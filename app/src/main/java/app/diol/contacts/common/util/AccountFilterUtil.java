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

package app.diol.contacts.common.util;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import app.diol.R;
import app.diol.contacts.common.list.ContactListFilter;

/**
 * Utility class for account filter manipulation.
 */
public class AccountFilterUtil {

    /**
     * Similar to {@link #updateAccountFilterTitleForPeople(View, ContactListFilter, boolean,
     * boolean)}, but for Phone UI.
     */
    public static boolean updateAccountFilterTitleForPhone(
            View filterContainer, ContactListFilter filter, boolean showTitleForAllAccounts) {
        return updateAccountFilterTitle(filterContainer, filter, showTitleForAllAccounts, true);
    }

    private static boolean updateAccountFilterTitle(
            View filterContainer,
            ContactListFilter filter,
            boolean showTitleForAllAccounts,
            boolean forPhone) {
        final Context context = filterContainer.getContext();
        final TextView headerTextView =
                (TextView) filterContainer.findViewById(R.id.account_filter_header);

        boolean textWasSet = false;
        if (filter != null) {
            if (forPhone) {
                if (filter.filterType == ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS) {
                    if (showTitleForAllAccounts) {
                        headerTextView.setText(R.string.list_filter_phones);
                        textWasSet = true;
                    }
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT) {
                    headerTextView.setText(
                            context.getString(R.string.listAllContactsInAccount, filter.accountName));
                    textWasSet = true;
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_CUSTOM) {
                    headerTextView.setText(R.string.listCustomView);
                    textWasSet = true;
                }
            } else {
                if (filter.filterType == ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS) {
                    if (showTitleForAllAccounts) {
                        headerTextView.setText(R.string.list_filter_all_accounts);
                        textWasSet = true;
                    }
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT) {
                    headerTextView.setText(
                            context.getString(R.string.listAllContactsInAccount, filter.accountName));
                    textWasSet = true;
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_CUSTOM) {
                    headerTextView.setText(R.string.listCustomView);
                    textWasSet = true;
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
                    headerTextView.setText(R.string.listSingleContact);
                    textWasSet = true;
                }
            }
        }
        return textWasSet;
    }
}
