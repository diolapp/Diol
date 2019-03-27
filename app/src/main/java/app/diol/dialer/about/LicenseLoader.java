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
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

/**
 * {@link AsyncTaskLoader} to load the list of licenses for the license menu activity.
 */
final class LicenseLoader extends AsyncTaskLoader<List<License>> {

    private List<License> licenses;

    LicenseLoader(Context context) {
        // This must only pass the application context to avoid leaking a pointer to the Activity.
        super(context.getApplicationContext());
    }

    @Override
    public List<License> loadInBackground() {
        return Licenses.getLicenses(getContext());
    }

    @Override
    public void deliverResult(List<License> licenses) {
        this.licenses = licenses;
        super.deliverResult(licenses);
    }

    @Override
    protected void onStartLoading() {
        if (licenses != null) {
            deliverResult(licenses);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
