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

package app.diol.dialer.glidephotomanager;

import javax.inject.Singleton;

import app.diol.dialer.glidephotomanager.impl.GlidePhotoManagerImpl;
import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import dagger.Binds;
import dagger.Module;

/**
 * Module for {@link GlidePhotoManagerComponent}
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public abstract class GlidePhotoManagerModule {
    @Binds
    @Singleton
    public abstract GlidePhotoManager bindGlidePhotoManager(GlidePhotoManagerImpl glidePhotoManager);
}
