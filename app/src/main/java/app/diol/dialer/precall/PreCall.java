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

package app.diol.dialer.precall;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.util.DialerUtils;

/**
 * Interface to prepare a {@link CallIntentBuilder} before placing the call with telecom.
 */
public interface PreCall {

    static Intent getIntent(Context context, CallIntentBuilder builder) {
        return PreCallComponent.get(context).getPreCall().buildIntent(context, builder);
    }

    static void start(Context context, CallIntentBuilder builder) {
        DialerUtils.startActivityWithErrorToast(context, getIntent(context, builder));
    }

    /**
     * @return a intent when started as activity, will perform the pre-call actions and then place a
     * call. TODO(twyen): if all actions do not require an UI, return a intent that will place the
     * call directly instead.
     */
    @NonNull
    @MainThread
    Intent buildIntent(Context context, CallIntentBuilder builder);
}
