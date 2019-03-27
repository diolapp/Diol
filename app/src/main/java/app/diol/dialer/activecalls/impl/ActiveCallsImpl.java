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

package app.diol.dialer.activecalls.impl;

import android.support.annotation.MainThread;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import app.diol.dialer.activecalls.ActiveCallInfo;
import app.diol.dialer.activecalls.ActiveCalls;
import app.diol.dialer.common.Assert;

/**
 * Implementation of {@link ActiveCalls}
 */
public class ActiveCallsImpl implements ActiveCalls {

    ImmutableList<ActiveCallInfo> activeCalls = ImmutableList.of();

    @Inject
    ActiveCallsImpl() {
    }

    @Override
    public ImmutableList<ActiveCallInfo> getActiveCalls() {
        return activeCalls;
    }

    @Override
    @MainThread
    public void setActiveCalls(ImmutableList<ActiveCallInfo> activeCalls) {
        Assert.isMainThread();
        this.activeCalls = Assert.isNotNull(activeCalls);
    }
}
