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

package app.diol.dialer.calllog.ui;

import android.content.Context;
import android.provider.CallLog.Calls;
import android.support.v4.content.CursorLoader;

import app.diol.dialer.calllog.database.contract.AnnotatedCallLogContract.AnnotatedCallLog;

/**
 * Cursor loader for {@link AnnotatedCallLog}.
 */
final class AnnotatedCallLogCursorLoader extends CursorLoader {

    AnnotatedCallLogCursorLoader(Context context) {
        super(
                context,
                AnnotatedCallLog.CONTENT_URI,
                /* projection = */ null,
                /* selection = */ AnnotatedCallLog.CALL_TYPE + " != ?",
                /* selectionArgs = */ new String[]{Integer.toString(Calls.VOICEMAIL_TYPE)},
                /* sortOrder = */ AnnotatedCallLog.TIMESTAMP + " DESC");
    }
}
