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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives the system API broadcast
 * com.android.internal.telephony.ACTION_CARRIER_VVM_PACKAGE_INSTALLED. This
 * broadcast is only sent to the system dialer. A non-system dialer does not
 * need to respect the carrier VVM app.
 */
public class CarrierVvmPackageInstalledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);
        VvmLog.i("CarrierVvmPackageInstalledReceiver.onReceive", "package installed: " + packageName);
        VvmPackageInstallHandler.handlePackageInstalled(context);
    }
}
