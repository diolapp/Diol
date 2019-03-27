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

package app.diol.dialer.phonenumbercache;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The query for the call log table.
 */
public final class CallLogQuery {

    public static final int ID = 0;
    public static final int NUMBER = 1;
    public static final int DATE = 2;
    public static final int DURATION = 3;
    public static final int CALL_TYPE = 4;
    public static final int COUNTRY_ISO = 5;
    public static final int VOICEMAIL_URI = 6;
    public static final int GEOCODED_LOCATION = 7;
    public static final int CACHED_NAME = 8;
    public static final int CACHED_NUMBER_TYPE = 9;
    public static final int CACHED_NUMBER_LABEL = 10;
    public static final int CACHED_LOOKUP_URI = 11;
    public static final int CACHED_MATCHED_NUMBER = 12;
    public static final int CACHED_NORMALIZED_NUMBER = 13;
    public static final int CACHED_PHOTO_ID = 14;
    public static final int CACHED_FORMATTED_NUMBER = 15;
    public static final int IS_READ = 16;
    public static final int NUMBER_PRESENTATION = 17;
    public static final int ACCOUNT_COMPONENT_NAME = 18;
    public static final int ACCOUNT_ID = 19;
    public static final int FEATURES = 20;
    public static final int DATA_USAGE = 21;
    public static final int TRANSCRIPTION = 22;
    public static final int CACHED_PHOTO_URI = 23;
    public static final int POST_DIAL_DIGITS = 24;
    public static final int VIA_NUMBER = 25;

    @RequiresApi(VERSION_CODES.O)
    public static final int TRANSCRIPTION_STATE = 26;

    private static final String[] PROJECTION_N =
            new String[]{
                    Calls._ID, // 0
                    Calls.NUMBER, // 1
                    Calls.DATE, // 2
                    Calls.DURATION, // 3
                    Calls.TYPE, // 4
                    Calls.COUNTRY_ISO, // 5
                    Calls.VOICEMAIL_URI, // 6
                    Calls.GEOCODED_LOCATION, // 7
                    Calls.CACHED_NAME, // 8
                    Calls.CACHED_NUMBER_TYPE, // 9
                    Calls.CACHED_NUMBER_LABEL, // 10
                    Calls.CACHED_LOOKUP_URI, // 11
                    Calls.CACHED_MATCHED_NUMBER, // 12
                    Calls.CACHED_NORMALIZED_NUMBER, // 13
                    Calls.CACHED_PHOTO_ID, // 14
                    Calls.CACHED_FORMATTED_NUMBER, // 15
                    Calls.IS_READ, // 16
                    Calls.NUMBER_PRESENTATION, // 17
                    Calls.PHONE_ACCOUNT_COMPONENT_NAME, // 18
                    Calls.PHONE_ACCOUNT_ID, // 19
                    Calls.FEATURES, // 20
                    Calls.DATA_USAGE, // 21
                    Calls.TRANSCRIPTION, // 22
                    Calls.CACHED_PHOTO_URI, // 23
                    CallLog.Calls.POST_DIAL_DIGITS, // 24
                    CallLog.Calls.VIA_NUMBER // 25
            };

    private static final String[] PROJECTION_O;

    // TODO(mdooley): remove when this becomes a public api
    // Copied from android.provider.CallLog.Calls
    private static final String TRANSCRIPTION_STATE_COLUMN = "transcription_state";

    static {
        List<String> projectionList = new ArrayList<>(Arrays.asList(PROJECTION_N));
        projectionList.add(TRANSCRIPTION_STATE_COLUMN);
        PROJECTION_O = projectionList.toArray(new String[projectionList.size()]);
    }

    @NonNull
    public static String[] getProjection() {
        return VERSION.SDK_INT >= VERSION_CODES.O ? PROJECTION_O : PROJECTION_N;
    }
}
