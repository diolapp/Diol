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

import app.diol.dialer.inject.HasRootComponent;
import app.diol.dialer.inject.IncludeInDialerRoot;
import dagger.Subcomponent;

/**
 * Dagger component for storage.
 */
@Subcomponent
public abstract class StorageComponent {

    public static StorageComponent get(Context context) {
        return ((StorageComponent.HasComponent)
                ((HasRootComponent) context.getApplicationContext()).component())
                .storageComponent();
    }

    /**
     * Returns unencrypted default shared preferences. This method should not be used for private
     * data.
     *
     * <p>These shared prefs are available even when the device is in FBE mode and are generally the
     * ones that should be used, because Dialer needs to function while in FBE mode.
     */
    @Unencrypted
    public abstract SharedPreferences unencryptedSharedPrefs();

    /**
     * Used to refer to the root application component.
     */
    @IncludeInDialerRoot
    public interface HasComponent {
        StorageComponent storageComponent();
    }
}
