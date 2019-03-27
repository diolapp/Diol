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

package app.diol.dialer.about;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import app.diol.R;

/**
 * The fragment for information about the Phone App
 */
public class AboutPhoneFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_phone_fragment);

        // We set the intent here, instead of in XML, to avoid specifying a target package, which
        // differs between AOSP and the GoogleDialer.
        Intent openSourceActivity =
                new Intent(getActivity().getApplicationContext(), LicenseMenuActivity.class);
        findPreference(getString(R.string.open_source_licenses_key)).setIntent(openSourceActivity);
        populateBuildVersion();
    }

    private void populateBuildVersion() {
        Preference buildVersion = findPreference(getResources().getString(R.string.build_version_key));
        String versionName = getVersionName();
        if (!TextUtils.isEmpty(versionName)) {
            buildVersion.setSummary(versionName);
        }
    }

    private String getVersionName() {
        Context context = getContext();
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
