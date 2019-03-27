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
import android.content.SharedPreferences;
import android.os.UserManager;
import android.preference.PreferenceManager;

import javax.inject.Inject;

import app.diol.R;
import app.diol.dialer.inject.ApplicationContext;

/**
 * Implementation of {@link ContactDisplayPreferences} backed by a {@link SharedPreferences}. Can
 * only be instantiated when the device is unlocked.
 */
public final class ContactDisplayPreferencesImpl implements ContactDisplayPreferences {

    private final Context appContext;
    private final SharedPreferences sharedPreferences;
    private final String displayOrderKey;
    private final String sortOrderKey;

    @Inject
    ContactDisplayPreferencesImpl(@ApplicationContext Context appContext) {
        this.appContext = appContext;
        // @Unencrypted preference would be a better choice, but Android Preference only supports the
        // default file. Stub should be used instead when device is locked.
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        displayOrderKey = appContext.getString(R.string.display_options_view_names_as_key);
        sortOrderKey = appContext.getString(R.string.display_options_sort_list_by_key);
    }

    @Override
    public DisplayOrder getDisplayOrder() {
        migrate();
        if (!sharedPreferences.contains(displayOrderKey)) {
            return DisplayOrder.PRIMARY;
        }
        return DisplayOrder.fromValue(appContext, sharedPreferences.getString(displayOrderKey, null));
    }

    @Override
    public void setDisplayOrder(DisplayOrder displayOrder) {
        sharedPreferences.edit().putString(displayOrderKey, displayOrder.getValue(appContext)).apply();
    }

    @Override
    public SortOrder getSortOrder() {
        migrate();
        if (!sharedPreferences.contains(sortOrderKey)) {
            return SortOrder.BY_PRIMARY;
        }
        return SortOrder.fromValue(appContext, sharedPreferences.getString(sortOrderKey, null));
    }

    @Override
    public void setSortOrder(SortOrder sortOrder) {
        sharedPreferences.edit().putString(sortOrderKey, sortOrder.getValue(appContext)).apply();
    }

    /**
     * Moves the stored values to the standard location.
     *
     * <p>Usually preferences are stored in {@code package.name_preferences.xml}. However the old
     * com.android.contacts.common.preference.ContactsPreferences stored it in {@code
     * package.name.xml} which is incompatible with the regular {@link android.preference.Preference}
     * widgets.
     */
    private void migrate() {
        if (!appContext.getSystemService(UserManager.class).isUserUnlocked()) {
            return;
        }
        SharedPreferences oldPreference =
                appContext.getSharedPreferences(appContext.getPackageName(), Context.MODE_PRIVATE);
        if (oldPreference.contains(displayOrderKey) || oldPreference.contains(sortOrderKey)) {
            sharedPreferences
                    .edit()
                    .putString(
                            displayOrderKey,
                            translateLegacyDisplayOrder(oldPreference.getInt(displayOrderKey, 1)))
                    .putString(sortOrderKey, translateLegacySortOrder(oldPreference.getInt(sortOrderKey, 1)))
                    .apply();
            oldPreference.edit().remove(displayOrderKey).remove(sortOrderKey).apply();
        }
    }

    private String translateLegacyDisplayOrder(int legacyValue) {
        switch (legacyValue) {
            case 2:
                return DisplayOrder.ALTERNATIVE.getValue(appContext);
            default:
                return DisplayOrder.PRIMARY.getValue(appContext);
        }
    }

    private String translateLegacySortOrder(int legacyValue) {
        switch (legacyValue) {
            case 2:
                return SortOrder.BY_ALTERNATIVE.getValue(appContext);
            default:
                return SortOrder.BY_PRIMARY.getValue(appContext);
        }
    }
}
