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

package app.diol.voicemail.impl;

import android.content.Context;
import android.support.v4.os.BuildCompat;

import javax.inject.Singleton;

import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import app.diol.voicemail.VoicemailClient;
import app.diol.voicemail.VoicemailPermissionHelper;
import app.diol.voicemail.stub.StubVoicemailClient;
import dagger.Module;
import dagger.Provides;

/**
 * This module provides an instance of the voicemail client.
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public final class VoicemailModule {

    private VoicemailModule() {
    }

    @Provides
    @Singleton
    static VoicemailClient provideVoicemailClient(@ApplicationContext Context context) {
        if (!BuildCompat.isAtLeastO()) {
            VvmLog.i("VoicemailModule.provideVoicemailClient", "SDK below O");
            return new StubVoicemailClient();
        }

        if (!VoicemailPermissionHelper.hasPermissions(context)) {
            VvmLog.i("VoicemailModule.provideVoicemailClient",
                    "missing permissions " + VoicemailPermissionHelper.getMissingPermissions(context));
            return new StubVoicemailClient();
        }

        VvmLog.i("VoicemailModule.provideVoicemailClient", "providing VoicemailClientImpl");
        return new VoicemailClientImpl();
    }
}
