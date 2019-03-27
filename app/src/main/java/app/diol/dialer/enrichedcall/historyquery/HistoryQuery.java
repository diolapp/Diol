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
package app.diol.dialer.enrichedcall.historyquery;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import app.diol.dialer.common.LogUtil;

/**
 * Data object representing the pieces of information required to query for historical enriched call
 * data.
 */
@AutoValue
public abstract class HistoryQuery {

    @NonNull
    public static HistoryQuery create(@NonNull String number, long callStartTime, long callEndTime) {
        return new AutoValue_HistoryQuery(number, callStartTime, callEndTime);
    }

    public abstract String getNumber();

    public abstract long getCallStartTimestamp();

    public abstract long getCallEndTimestamp();

    @Override
    public String toString() {
        return String.format(
                "HistoryQuery{number: %s, callStartTimestamp: %d, callEndTimestamp: %d}",
                LogUtil.sanitizePhoneNumber(getNumber()), getCallStartTimestamp(), getCallEndTimestamp());
    }
}
