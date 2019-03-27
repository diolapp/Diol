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

package app.diol.dialer.main.impl.toolbar;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Useful callback for {@link SearchBarView} listeners.
 */
public interface SearchBarListener {

    /**
     * Called when the user clicks on the search bar.
     */
    void onSearchBarClicked();

    /**
     * Called when the search query updates.
     */
    void onSearchQueryUpdated(String query);

    /**
     * Called when the back button is clicked in the search bar.
     */
    void onSearchBackButtonClicked();

    /**
     * Called when the voice search button is clicked.
     */
    void onVoiceButtonClicked(VoiceSearchResultCallback voiceSearchResultCallback);

    /**
     * Called when a toolbar menu item is clicked.
     */
    boolean onMenuItemClicked(MenuItem menuItem);

    /**
     * Called when {@link Activity#onPause()} is called.
     */
    void onActivityPause();

    /**
     * Called when {@link AppCompatActivity#onUserLeaveHint()} is called.
     */
    void onUserLeaveHint();

    /**
     * Called when the user places a call from search (regular or dialpad).
     */
    void onCallPlacedFromSearch();

    /**
     * Called when a permission is about to be requested.
     */
    void requestingPermission();

    /**
     * Interface for returning voice results to the search bar.
     */
    interface VoiceSearchResultCallback {

        /**
         * Sets the voice results in the search bar and expands the search UI.
         */
        void setResult(String result);
    }
}
