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

package app.diol.dialer.precall.impl;

import com.google.common.collect.ImmutableList;

import javax.inject.Singleton;

import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import app.diol.dialer.precall.PreCall;
import app.diol.dialer.precall.PreCallAction;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for {@link PreCall}.
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public abstract class PreCallModule {

    private PreCallModule() {
    }

    @Provides
    public static ImmutableList<PreCallAction> provideActions(
            DuoAction duoAction, CallingAccountSelector callingAccountSelector) {
        return ImmutableList.of(
                new PermissionCheckAction(),
                new MalformedNumberRectifier(
                        ImmutableList.of(new UkRegionPrefixInInternationalFormatHandler())),
                callingAccountSelector,
                duoAction,
                new AssistedDialAction());
    }

    @Binds
    @Singleton
    public abstract PreCall to(PreCallImpl impl);
}
