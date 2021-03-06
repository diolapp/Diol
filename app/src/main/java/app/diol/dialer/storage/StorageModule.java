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
package app.diol.dialer.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import javax.inject.Singleton;

import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import dagger.Module;
import dagger.Provides;

/**
 * Module for the storage component.
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public class StorageModule {

    @Provides
    @Singleton
    @Unencrypted
    static SharedPreferences provideUnencryptedSharedPrefs(@ApplicationContext Context appContext) {
        // #createDeviceProtectedStorageContext returns a new context each time, so we cache the shared
        // preferences object in order to avoid accessing disk for every operation.
        Context deviceProtectedContext = ContextCompat.createDeviceProtectedStorageContext(appContext);

        // ContextCompat.createDeviceProtectedStorageContext(context) returns null on pre-N, thus fall
        // back to regular default shared preference for pre-N devices since devices protected context
        // is not available.
        return PreferenceManager.getDefaultSharedPreferences(
                deviceProtectedContext != null ? deviceProtectedContext : appContext);
    }
}
