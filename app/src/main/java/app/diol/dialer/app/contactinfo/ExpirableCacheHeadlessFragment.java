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

package app.diol.dialer.app.contactinfo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import app.diol.dialer.phonenumbercache.ContactInfo;
import app.diol.dialer.util.ExpirableCache;

/**
 * Fragment without any UI whose purpose is to retain an instance of {@link ExpirableCache} across
 * configuration change through the use of {@link #setRetainInstance(boolean)}. This is done as
 * opposed to implementing {@link android.os.Parcelable} as it is a less widespread change.
 */
public class ExpirableCacheHeadlessFragment extends Fragment {

    private static final String FRAGMENT_TAG = "ExpirableCacheHeadlessFragment";
    private static final int CONTACT_INFO_CACHE_SIZE = 100;

    private ExpirableCache<NumberWithCountryIso, ContactInfo> retainedCache;

    @NonNull
    public static ExpirableCacheHeadlessFragment attach(@NonNull AppCompatActivity parentActivity) {
        return attach(parentActivity.getSupportFragmentManager());
    }

    @NonNull
    private static ExpirableCacheHeadlessFragment attach(FragmentManager fragmentManager) {
        ExpirableCacheHeadlessFragment fragment =
                (ExpirableCacheHeadlessFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new ExpirableCacheHeadlessFragment();
            // Allowing state loss since in rare cases this is called after activity's state is saved and
            // it's fine if the cache is lost.
            fragmentManager.beginTransaction().add(fragment, FRAGMENT_TAG).commitNowAllowingStateLoss();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retainedCache = ExpirableCache.create(CONTACT_INFO_CACHE_SIZE);
        setRetainInstance(true);
    }

    public ExpirableCache<NumberWithCountryIso, ContactInfo> getRetainedCache() {
        return retainedCache;
    }
}
