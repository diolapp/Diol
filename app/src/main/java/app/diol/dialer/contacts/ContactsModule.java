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

package app.diol.dialer.contacts;

import android.content.Context;
import android.os.UserManager;

import app.diol.dialer.contacts.displaypreference.ContactDisplayPreferences;
import app.diol.dialer.contacts.displaypreference.ContactDisplayPreferencesImpl;
import app.diol.dialer.contacts.displaypreference.ContactDisplayPreferencesStub;
import app.diol.dialer.contacts.hiresphoto.HighResolutionPhotoRequester;
import app.diol.dialer.contacts.hiresphoto.HighResolutionPhotoRequesterImpl;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import dagger.Binds;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Module for standard {@link ContactsComponent}
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public abstract class ContactsModule {
    @Provides
    public static ContactDisplayPreferences provideContactDisplayPreferences(
            @ApplicationContext Context appContext,
            Lazy<ContactDisplayPreferencesImpl> impl,
            ContactDisplayPreferencesStub stub) {
        if (appContext.getSystemService(UserManager.class).isUserUnlocked()) {
            return impl.get();
        }
        return stub;
    }

    @Binds
    public abstract HighResolutionPhotoRequester toHighResolutionPhotoRequesterImpl(
            HighResolutionPhotoRequesterImpl impl);
}
