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

package app.diol.dialer.main.impl.bottomnav;

import android.Manifest;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.support.annotation.RequiresPermission;

import com.google.common.util.concurrent.ListenableFuture;

import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.common.concurrent.UiListener;
import app.diol.dialer.main.impl.bottomnav.BottomNavBar.TabIndex;

/**
 * Observes the call log and updates the badge count to show the number of unread missed calls.
 *
 * <p>Used only when the new call log fragment is enabled.
 */
public final class MissedCallCountObserver extends ContentObserver {
    private final Context appContext;
    private final BottomNavBar bottomNavBar;
    private final UiListener<Integer> uiListener;

    public MissedCallCountObserver(
            Context appContext, BottomNavBar bottomNavBar, UiListener<Integer> uiListener) {
        super(null);
        this.appContext = appContext;
        this.bottomNavBar = bottomNavBar;
        this.uiListener = uiListener;
    }

    @RequiresPermission(Manifest.permission.READ_CALL_LOG)
    @Override
    public void onChange(boolean selfChange) {
        ListenableFuture<Integer> countFuture =
                DialerExecutorComponent.get(appContext)
                        .backgroundExecutor()
                        .submit(
                                () -> {
                                    try (Cursor cursor =
                                                 appContext
                                                         .getContentResolver()
                                                         .query(
                                                                 Calls.CONTENT_URI,
                                                                 new String[]{Calls._ID},
                                                                 "("
                                                                         + Calls.IS_READ
                                                                         + " = ? OR "
                                                                         + Calls.IS_READ
                                                                         + " IS NULL) AND "
                                                                         + Calls.TYPE
                                                                         + " = ?",
                                                                 new String[]{"0", Integer.toString(Calls.MISSED_TYPE)},
                                                                 /* sortOrder= */ null)) {
                                        return cursor == null ? 0 : cursor.getCount();
                                    }
                                });
        uiListener.listen(
                appContext,
                countFuture,
                count -> bottomNavBar.setNotificationCount(TabIndex.CALL_LOG, count == null ? 0 : count),
                throwable -> {
                    throw new RuntimeException(throwable);
                });
    }
}
