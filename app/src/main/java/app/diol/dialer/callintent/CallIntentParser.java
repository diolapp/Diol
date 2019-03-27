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

package app.diol.dialer.callintent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.protos.ProtoParsers;

/**
 * Parses data for a call extra to get any dialer specific app data.
 */
public class CallIntentParser {


    private CallIntentParser() {
    }

    @Nullable
    public static CallSpecificAppData getCallSpecificAppData(@Nullable Bundle extras) {
        if (extras == null) {
            return null;
        }

        if (!extras.containsKey(Constants.EXTRA_CALL_SPECIFIC_APP_DATA)) {
            return null;
        }

        if (extras.getByteArray(Constants.EXTRA_CALL_SPECIFIC_APP_DATA) == null) {
            LogUtil.i(
                    "CallIntentParser.getCallSpecificAppData",
                    "unexpected null byte array for call specific app data proto");
            return null;
        }

        return ProtoParsers.getTrusted(
                extras, Constants.EXTRA_CALL_SPECIFIC_APP_DATA, CallSpecificAppData.getDefaultInstance());
    }

    public static void putCallSpecificAppData(
            @NonNull Bundle extras, @NonNull CallSpecificAppData callSpecificAppData) {
        ProtoParsers.put(extras, Constants.EXTRA_CALL_SPECIFIC_APP_DATA, callSpecificAppData);
    }
}
