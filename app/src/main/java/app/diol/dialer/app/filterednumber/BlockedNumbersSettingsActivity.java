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

package app.diol.dialer.app.filterednumber;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import app.diol.R;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.logging.ScreenEvent;

/**
 * TODO(calderwoodra): documentation
 */
public class BlockedNumbersSettingsActivity extends AppCompatActivity {

    private static final String TAG_BLOCKED_MANAGEMENT_FRAGMENT = "blocked_management";
    private static final String TAG_VIEW_NUMBERS_TO_IMPORT_FRAGMENT = "view_numbers_to_import";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blocked_numbers_activity);

        // If savedInstanceState != null, the Activity will automatically restore the last fragment.
        if (savedInstanceState == null) {
            showManagementUi();
        }
    }

    /**
     * Shows fragment with the list of currently blocked numbers and settings related to blocking.
     */
    public void showManagementUi() {
        BlockedNumbersFragment fragment =
                (BlockedNumbersFragment)
                        getFragmentManager().findFragmentByTag(TAG_BLOCKED_MANAGEMENT_FRAGMENT);
        if (fragment == null) {
            fragment = new BlockedNumbersFragment();
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.blocked_numbers_activity_container, fragment, TAG_BLOCKED_MANAGEMENT_FRAGMENT)
                .commit();

        Logger.get(this).logScreenView(ScreenEvent.Type.BLOCKED_NUMBER_MANAGEMENT, this);
    }

    /**
     * Shows fragment with UI to preview the numbers of contacts currently marked as send-to-voicemail
     * in Contacts. These numbers can be imported into Dialer's blocked number list.
     */
    public void showNumbersToImportPreviewUi() {
        ViewNumbersToImportFragment fragment =
                (ViewNumbersToImportFragment)
                        getFragmentManager().findFragmentByTag(TAG_VIEW_NUMBERS_TO_IMPORT_FRAGMENT);
        if (fragment == null) {
            fragment = new ViewNumbersToImportFragment();
        }

        getFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.blocked_numbers_activity_container, fragment, TAG_VIEW_NUMBERS_TO_IMPORT_FRAGMENT)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // TODO: Achieve back navigation without overriding onBackPressed.
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
