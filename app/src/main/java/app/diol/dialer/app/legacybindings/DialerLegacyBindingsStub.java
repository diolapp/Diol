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

package app.diol.dialer.app.legacybindings;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import app.diol.dialer.app.calllog.CallLogAdapter;
import app.diol.dialer.app.calllog.calllogcache.CallLogCache;
import app.diol.dialer.app.contactinfo.ContactInfoCache;
import app.diol.dialer.app.voicemail.VoicemailPlaybackPresenter;
import app.diol.dialer.blocking.FilteredNumberAsyncQueryHandler;

/**
 * Default implementation for dialer legacy bindings.
 */
public class DialerLegacyBindingsStub implements DialerLegacyBindings {

    @Override
    public CallLogAdapter newCallLogAdapter(
            Activity activity,
            ViewGroup alertContainer,
            CallLogAdapter.CallFetcher callFetcher,
            CallLogAdapter.MultiSelectRemoveView multiSelectRemoveView,
            CallLogAdapter.OnActionModeStateChangedListener actionModeStateChangedListener,
            CallLogCache callLogCache,
            ContactInfoCache contactInfoCache,
            VoicemailPlaybackPresenter voicemailPlaybackPresenter,
            @NonNull FilteredNumberAsyncQueryHandler filteredNumberAsyncQueryHandler,
            int activityType) {
        return new CallLogAdapter(
                activity,
                alertContainer,
                callFetcher,
                multiSelectRemoveView,
                actionModeStateChangedListener,
                callLogCache,
                contactInfoCache,
                voicemailPlaybackPresenter,
                filteredNumberAsyncQueryHandler,
                activityType);
    }
}
