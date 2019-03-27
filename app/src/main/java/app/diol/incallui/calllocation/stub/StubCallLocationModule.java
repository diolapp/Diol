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

package app.diol.incallui.calllocation.stub;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import app.diol.dialer.common.Assert;
import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import app.diol.incallui.calllocation.CallLocation;
import dagger.Binds;
import dagger.Module;

/**
 * This module provides an instance of call location.
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public abstract class StubCallLocationModule {

    @Binds
    public abstract CallLocation bindCallLocation(StubCallLocation callLocation);

    static class StubCallLocation implements CallLocation {
        @Inject
        StubCallLocation() {
        }

        @Override
        public boolean canGetLocation(@NonNull Context context) {
            return false;
        }

        @Override
        @NonNull
        public Fragment getLocationFragment(@NonNull Context context) {
            throw Assert.createUnsupportedOperationFailException();
        }

        @Override
        public void close() {
        }
    }
}
