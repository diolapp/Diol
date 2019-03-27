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

package app.diol.dialer.calllog.observer;

import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.MainThread;
import android.support.annotation.VisibleForTesting;

import javax.inject.Inject;

import app.diol.dialer.calllog.notifier.RefreshAnnotatedCallLogNotifier;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.ThreadUtil;

/**
 * Mark the annotated call log as dirty and notify that a refresh is in order when the content
 * changes.
 */
public final class MarkDirtyObserver extends ContentObserver {

    private final RefreshAnnotatedCallLogNotifier refreshAnnotatedCallLogNotifier;

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Inject
    public MarkDirtyObserver(RefreshAnnotatedCallLogNotifier refreshAnnotatedCallLogNotifier) {
        super(ThreadUtil.getUiThreadHandler());
        this.refreshAnnotatedCallLogNotifier = refreshAnnotatedCallLogNotifier;
    }

    @MainThread
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Assert.isMainThread();
        LogUtil.i(
                "MarkDirtyObserver.onChange", "Uri:%s, SelfChange:%b", String.valueOf(uri), selfChange);

        refreshAnnotatedCallLogNotifier.markDirtyAndNotify();
    }
}
