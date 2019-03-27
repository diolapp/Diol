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

package app.diol.contacts.common.compat;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.telecom.Call;

/**
 * Compatibility utilities for android.telecom.Call
 */
public class CallCompat {

    public static boolean canPullExternalCall(@NonNull android.telecom.Call call) {
        return VERSION.SDK_INT >= VERSION_CODES.N_MR1
                && ((call.getDetails().getCallCapabilities() & Details.CAPABILITY_CAN_PULL_CALL)
                == Details.CAPABILITY_CAN_PULL_CALL);
    }

    /**
     * android.telecom.Call.Details
     */
    public static class Details {

        public static final int PROPERTY_IS_EXTERNAL_CALL = Call.Details.PROPERTY_IS_EXTERNAL_CALL;
        public static final int PROPERTY_ENTERPRISE_CALL = Call.Details.PROPERTY_ENTERPRISE_CALL;
        public static final int CAPABILITY_CAN_PULL_CALL = Call.Details.CAPABILITY_CAN_PULL_CALL;
        public static final int CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO =
                Call.Details.CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO;

        public static final String EXTRA_ANSWERING_DROPS_FOREGROUND_CALL =
                "android.telecom.extra.ANSWERING_DROPS_FG_CALL";
    }
}

