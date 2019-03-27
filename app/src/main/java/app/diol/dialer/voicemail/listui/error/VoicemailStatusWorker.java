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

package app.diol.dialer.voicemail.listui.error;

import android.content.Context;
import android.database.Cursor;
import android.provider.VoicemailContract.Status;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.telecom.TelecomUtil;
import app.diol.dialer.voicemailstatus.VoicemailStatusQuery;
import app.diol.voicemail.VoicemailComponent;

/**
 * Worker for {@link app.diol.dialer.common.concurrent.DialerExecutors} to fetch voicemail status
 */
public class VoicemailStatusWorker implements Worker<Context, List<VoicemailStatus>> {

    @Nullable
    @Override
    public List<VoicemailStatus> doInBackground(@Nullable Context context) throws Throwable {
        List<VoicemailStatus> statuses = new ArrayList<>();
        if (!TelecomUtil.hasReadWriteVoicemailPermissions(context)) {
            return statuses;
        }
        StringBuilder where = new StringBuilder();
        java.util.List<String> selectionArgs = new ArrayList<>();

        VoicemailComponent.get(context)
                .getVoicemailClient()
                .appendOmtpVoicemailStatusSelectionClause(context, where, selectionArgs);

        try (Cursor cursor =
                     context
                             .getContentResolver()
                             .query(
                                     Status.CONTENT_URI,
                                     VoicemailStatusQuery.getProjection(),
                                     where.toString(),
                                     selectionArgs.toArray(new String[selectionArgs.size()]),
                                     null)) {
            if (cursor == null) {
                return statuses;
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                statuses.add(new VoicemailStatus(context, cursor));
            }
        }

        return statuses;
    }
}
