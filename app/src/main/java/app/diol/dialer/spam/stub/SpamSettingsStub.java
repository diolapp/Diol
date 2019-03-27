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

package app.diol.dialer.spam.stub;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import app.diol.dialer.spam.SpamSettings;

/**
 * Default implementation of SpamSettings.
 */
public class SpamSettingsStub implements SpamSettings {

    @Inject
    public SpamSettingsStub() {
    }

    @Override
    public boolean isSpamEnabled() {
        return false;
    }

    @Override
    public boolean isSpamNotificationEnabled() {
        return false;
    }

    @Override
    public boolean isSpamBlockingEnabledByFlag() {
        return false;
    }

    @Override
    public boolean isSpamBlockingControlledByCarrier() {
        return false;
    }

    @Override
    public boolean isSpamBlockingEnabled() {
        return false;
    }

    @Override
    public boolean isSpamBlockingEnabledByUser() {
        return false;
    }

    @Override
    public boolean isDialogEnabledForSpamNotification() {
        return false;
    }

    @Override
    public boolean isDialogReportSpamCheckedByDefault() {
        return false;
    }

    @Override
    public int percentOfSpamNotificationsToShow() {
        return 0;
    }

    @Override
    public int percentOfNonSpamNotificationsToShow() {
        return 0;
    }

    @Override
    public void modifySpamBlockingSetting(boolean enabled, ModifySettingListener listener) {
        listener.onComplete(false);
    }

    @Override
    public Intent getSpamBlockingSettingIntent(Context context) {
        return new Intent();
    }
}
