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

package app.diol.contacts.common.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import app.diol.contacts.common.model.AccountTypeManager;
import app.diol.contacts.common.model.account.AccountWithDataSet;

/**
 * Manages {@link ContactListFilter}. All methods must be called from UI thread.
 */
public abstract class ContactListFilterController {

    // singleton to cache the filter controller
    private static ContactListFilterControllerImpl sFilterController = null;

    public static ContactListFilterController getInstance(Context context) {
        // We may need to synchronize this in the future if background task will call this.
        if (sFilterController == null) {
            sFilterController = new ContactListFilterControllerImpl(context);
        }
        return sFilterController;
    }

    public abstract void addListener(ContactListFilterListener listener);

    public abstract void removeListener(ContactListFilterListener listener);

    /**
     * Return the currently-active filter.
     */
    public abstract ContactListFilter getFilter();

    /**
     * @param filter     the filter
     * @param persistent True when the given filter should be saved soon. False when the filter should
     *                   not be saved. The latter case may happen when some Intent requires a certain type of UI
     *                   (e.g. single contact) temporarily.
     */
    public abstract void setContactListFilter(ContactListFilter filter, boolean persistent);

    public abstract void selectCustomFilter();

    /**
     * Checks if the current filter is valid and reset the filter if not. It may happen when an
     * account is removed while the filter points to the account with {@link
     * ContactListFilter#FILTER_TYPE_ACCOUNT} type, for example. It may also happen if the current
     * filter is {@link ContactListFilter#FILTER_TYPE_SINGLE_CONTACT}, in which case, we should switch
     * to the last saved filter in {@link SharedPreferences}.
     */
    public abstract void checkFilterValidity(boolean notifyListeners);

    public interface ContactListFilterListener {

        void onContactListFilterChanged();
    }
}

/**
 * Stores the {@link ContactListFilter} selected by the user and saves it to {@link
 * SharedPreferences} if necessary.
 */
class ContactListFilterControllerImpl extends ContactListFilterController {

    private final Context mAppContext;
    private final List<ContactListFilterListener> mListeners =
            new ArrayList<ContactListFilterListener>();
    private ContactListFilter mFilter;

    public ContactListFilterControllerImpl(Context context) {
        mAppContext = context.getApplicationContext();
        mFilter = ContactListFilter.restoreDefaultPreferences(getSharedPreferences());
        checkFilterValidity(true /* notify listeners */);
    }

    @Override
    public void addListener(ContactListFilterListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeListener(ContactListFilterListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public ContactListFilter getFilter() {
        return mFilter;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

    @Override
    public void setContactListFilter(ContactListFilter filter, boolean persistent) {
        setContactListFilter(filter, persistent, true);
    }

    private void setContactListFilter(
            ContactListFilter filter, boolean persistent, boolean notifyListeners) {
        if (!filter.equals(mFilter)) {
            mFilter = filter;
            if (persistent) {
                ContactListFilter.storeToPreferences(getSharedPreferences(), mFilter);
            }
            if (notifyListeners && !mListeners.isEmpty()) {
                notifyContactListFilterChanged();
            }
        }
    }

    @Override
    public void selectCustomFilter() {
        setContactListFilter(
                ContactListFilter.createFilterWithType(ContactListFilter.FILTER_TYPE_CUSTOM), true);
    }

    private void notifyContactListFilterChanged() {
        for (ContactListFilterListener listener : mListeners) {
            listener.onContactListFilterChanged();
        }
    }

    @Override
    public void checkFilterValidity(boolean notifyListeners) {
        if (mFilter == null) {
            return;
        }

        switch (mFilter.filterType) {
            case ContactListFilter.FILTER_TYPE_SINGLE_CONTACT:
                setContactListFilter(
                        ContactListFilter.restoreDefaultPreferences(getSharedPreferences()),
                        false,
                        notifyListeners);
                break;
            case ContactListFilter.FILTER_TYPE_ACCOUNT:
                if (!filterAccountExists()) {
                    // The current account filter points to invalid account. Use "all" filter
                    // instead.
                    setContactListFilter(
                            ContactListFilter.createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS),
                            true,
                            notifyListeners);
                }
        }
    }

    /**
     * @return true if the Account for the current filter exists.
     */
    private boolean filterAccountExists() {
        final AccountTypeManager accountTypeManager = AccountTypeManager.getInstance(mAppContext);
        final AccountWithDataSet filterAccount =
                new AccountWithDataSet(mFilter.accountName, mFilter.accountType, mFilter.dataSet);
        return accountTypeManager.contains(filterAccount, false);
    }
}
