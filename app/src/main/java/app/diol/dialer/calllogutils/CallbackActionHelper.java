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

package app.diol.dialer.calllogutils;

import android.content.Context;
import android.provider.CallLog.Calls;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.diol.dialer.duo.DuoComponent;

/**
 * Helper class to determine the callback action associated with a call in the call log.
 */
public class CallbackActionHelper {

    /**
     * Returns the {@link CallbackAction} that can be associated with a call.
     *
     * @param number                    The phone number in column {@link android.provider.CallLog.Calls#NUMBER}.
     * @param features                  Value of features in column {@link android.provider.CallLog.Calls#FEATURES}.
     * @param phoneAccountComponentName Account name in column {@link
     *                                  android.provider.CallLog.Calls#PHONE_ACCOUNT_COMPONENT_NAME}.
     * @return One of the values in {@link CallbackAction}
     */
    public static @CallbackAction
    int getCallbackAction(
            Context context, String number, int features, String phoneAccountComponentName) {
        return getCallbackAction(number, features, isDuoCall(context, phoneAccountComponentName));
    }

    /**
     * Returns the {@link CallbackAction} that can be associated with a call.
     *
     * @param number    The phone number in column {@link android.provider.CallLog.Calls#NUMBER}.
     * @param features  Value of features in column {@link android.provider.CallLog.Calls#FEATURES}.
     * @param isDuoCall Whether the call is a Duo call.
     * @return One of the values in {@link CallbackAction}
     */
    public static @CallbackAction
    int getCallbackAction(
            String number, int features, boolean isDuoCall) {
        if (TextUtils.isEmpty(number)) {
            return CallbackAction.NONE;
        }
        if (isDuoCall) {
            return CallbackAction.DUO;
        }

        boolean isVideoCall = (features & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO;
        if (isVideoCall) {
            return CallbackAction.IMS_VIDEO;
        }

        return CallbackAction.VOICE;
    }

    private static boolean isDuoCall(Context context, String phoneAccountComponentName) {
        return DuoComponent.get(context).getDuo().isDuoAccount(phoneAccountComponentName);
    }

    /**
     * Specifies the action a user can take to make a callback.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CallbackAction.NONE, CallbackAction.IMS_VIDEO, CallbackAction.DUO, CallbackAction.VOICE})
    public @interface CallbackAction {
        int NONE = 0;
        int IMS_VIDEO = 1;
        int DUO = 2;
        int VOICE = 3;
    }
}
