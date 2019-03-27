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

package app.diol.dialer.common.concurrent;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Returns a {@link FutureCallback} which does nothing on success and crashes the application on
 * failure.
 *
 * <p>You generally shouldn't use this for futures which should be tied to UI, for those use {@link
 * UiListener}.
 *
 * <p>Can be safely used with {@link MoreExecutors#directExecutor()}
 */
public final class DefaultFutureCallback<T> implements FutureCallback<T> {

    @Override
    public void onSuccess(T unused) {
    }

    @Override
    public void onFailure(Throwable throwable) {
        ThreadUtil.getUiThreadHandler()
                .post(
                        () -> {
                            throw new RuntimeException(throwable);
                        });
    }
}
