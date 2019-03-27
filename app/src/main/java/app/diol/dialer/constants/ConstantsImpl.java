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

package app.diol.dialer.constants;

import android.content.Context;
import android.support.annotation.NonNull;

import app.diol.dialer.proguard.UsedByReflection;

/**
 * Provider config values for AOSP Dialer.
 */
@UsedByReflection(value = "Constants.java")
public class ConstantsImpl extends Constants {

    @Override
    @NonNull
    public String getFilteredNumberProviderAuthority() {
        return "app.diol.dialer.blocking.filterednumberprovider";
    }

    @Override
    @NonNull
    public String getFileProviderAuthority() {
        return "app.diol.dialer.files";
    }

    @NonNull
    @Override
    public String getAnnotatedCallLogProviderAuthority() {
        return "app.diol.dialer.annotatedcalllog";
    }

    @NonNull
    @Override
    public String getPhoneLookupHistoryProviderAuthority() {
        return "app.diol.dialer.phonelookuphistory";
    }

    @NonNull
    @Override
    public String getPreferredSimFallbackProviderAuthority() {
        return "app.diol.dialer.preferredsimfallback";
    }

    @Override
    public String getUserAgent(Context context) {
        return null;
    }

    @NonNull
    @Override
    public String getSettingsActivity() {
        return "app.diol.dialer.app.settings.DialerSettingsActivity";
    }
}
