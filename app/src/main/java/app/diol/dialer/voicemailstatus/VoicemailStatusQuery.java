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

package app.diol.dialer.voicemailstatus;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.VoicemailContract.Status;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The query for the call voicemail status table.
 */
public class VoicemailStatusQuery {

    // TODO(maxwelb): Column indices should be removed in favor of Cursor#getColumnIndex
    public static final int SOURCE_PACKAGE_INDEX = 0;
    public static final int SETTINGS_URI_INDEX = 1;
    public static final int VOICEMAIL_ACCESS_URI_INDEX = 2;
    public static final int CONFIGURATION_STATE_INDEX = 3;
    public static final int DATA_CHANNEL_STATE_INDEX = 4;
    public static final int NOTIFICATION_CHANNEL_STATE_INDEX = 5;
    public static final int QUOTA_OCCUPIED_INDEX = 6;
    public static final int QUOTA_TOTAL_INDEX = 7;

    @RequiresApi(VERSION_CODES.N_MR1)
    // The PHONE_ACCOUNT columns were added in M, but aren't queryable until N MR1
    public static final int PHONE_ACCOUNT_COMPONENT_NAME = 8;

    @RequiresApi(VERSION_CODES.N_MR1)
    public static final int PHONE_ACCOUNT_ID = 9;

    @RequiresApi(VERSION_CODES.N_MR1)
    public static final int SOURCE_TYPE_INDEX = 10;

    @RequiresApi(VERSION_CODES.N)
    private static final String[] PROJECTION_N =
            new String[]{
                    Status.SOURCE_PACKAGE, // 0
                    Status.SETTINGS_URI, // 1
                    Status.VOICEMAIL_ACCESS_URI, // 2
                    Status.CONFIGURATION_STATE, // 3
                    Status.DATA_CHANNEL_STATE, // 4
                    Status.NOTIFICATION_CHANNEL_STATE, // 5
                    Status.QUOTA_OCCUPIED, // 6
                    Status.QUOTA_TOTAL // 7
            };

    @RequiresApi(VERSION_CODES.N_MR1)
    private static final String[] PROJECTION_NMR1;

    static {
        List<String> projectionList = new ArrayList<>(Arrays.asList(PROJECTION_N));
        projectionList.add(Status.PHONE_ACCOUNT_COMPONENT_NAME); // 8
        projectionList.add(Status.PHONE_ACCOUNT_ID); // 9
        projectionList.add(Status.SOURCE_TYPE); // 10
        PROJECTION_NMR1 = projectionList.toArray(new String[projectionList.size()]);
    }

    public static String[] getProjection() {
        return VERSION.SDK_INT >= VERSION_CODES.N_MR1 ? PROJECTION_NMR1 : PROJECTION_N;
    }
}
