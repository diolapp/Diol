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

import android.content.Context;
import android.widget.Toast;

import app.diol.R;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.precall.PreCallAction;
import app.diol.dialer.precall.PreCallCoordinator;
import app.diol.dialer.telecom.TelecomUtil;

/**
 * Aborts call and show a toast if phone permissions are missing.
 */
public class PermissionCheckAction implements PreCallAction {

    @Override
    public boolean requiresUi(Context context, CallIntentBuilder builder) {
        return !TelecomUtil.hasCallPhonePermission(context);
    }

    @Override
    public void runWithoutUi(Context context, CallIntentBuilder builder) {
    }

    @Override
    public void runWithUi(PreCallCoordinator coordinator) {
        if (!requiresUi(coordinator.getActivity(), coordinator.getBuilder())) {
            return;
        }
        Toast.makeText(
                coordinator.getActivity(),
                coordinator
                        .getActivity()
                        .getString(R.string.pre_call_permission_check_no_phone_permission),
                Toast.LENGTH_LONG)
                .show();
        coordinator.abortCall();
    }

    @Override
    public void onDiscard() {
    }
}
