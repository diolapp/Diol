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

package app.diol.dialer.enrichedcall.stub;

import android.content.BroadcastReceiver.PendingResult;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import app.diol.dialer.calldetails.CallDetailsEntries;
import app.diol.dialer.calldetails.CallDetailsEntries.CallDetailsEntry;
import app.diol.dialer.common.Assert;
import app.diol.dialer.enrichedcall.EnrichedCallCapabilities;
import app.diol.dialer.enrichedcall.EnrichedCallManager;
import app.diol.dialer.enrichedcall.Session;
import app.diol.dialer.enrichedcall.historyquery.proto.HistoryResult;
import app.diol.dialer.enrichedcall.videoshare.VideoShareListener;
import app.diol.dialer.enrichedcall.videoshare.VideoShareSession;
import app.diol.dialer.multimedia.MultimediaData;

/**
 * Stub implementation of {@link EnrichedCallManager}.
 */
public final class EnrichedCallManagerStub implements EnrichedCallManager {

    @Override
    public void registerCapabilitiesListener(@NonNull CapabilitiesListener listener) {
    }

    @Override
    public void requestCapabilities(@NonNull String number) {
    }

    @Override
    public void unregisterCapabilitiesListener(@NonNull CapabilitiesListener listener) {
    }

    @Override
    public EnrichedCallCapabilities getCapabilities(@NonNull String number) {
        return null;
    }

    @Override
    public void clearCachedData() {
    }

    @Override
    public long startCallComposerSession(@NonNull String number) {
        return Session.NO_SESSION_ID;
    }

    @Override
    public void sendCallComposerData(long sessionId, @NonNull MultimediaData data) {
    }

    @Override
    public void endCallComposerSession(long sessionId) {
    }

    @Override
    public void sendPostCallNote(@NonNull String number, @NonNull String message) {
    }

    @Override
    public void onCapabilitiesReceived(
            @NonNull String number, @NonNull EnrichedCallCapabilities capabilities) {
    }

    @Override
    public void registerStateChangedListener(@NonNull StateChangedListener listener) {
    }

    @Nullable
    @Override
    public Session getSession(
            @NonNull String uniqueCallId, @NonNull String number, @Nullable Filter filter) {
        return null;
    }

    @Nullable
    @Override
    public Session getSession(long sessionId) {
        return null;
    }

    @MainThread
    @NonNull
    @Override
    public List<String> getAllSessionsForDisplay() {
        Assert.isMainThread();
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Filter createIncomingCallComposerFilter() {
        return session -> false;
    }

    @NonNull
    @Override
    public Filter createOutgoingCallComposerFilter() {
        return session -> false;
    }

    @Override
    public void registerHistoricalDataChangedListener(
            @NonNull HistoricalDataChangedListener listener) {
    }

    @Override
    public void unregisterHistoricalDataChangedListener(
            @NonNull HistoricalDataChangedListener listener) {
    }

    @Nullable
    @Override
    @MainThread
    public Map<CallDetailsEntry, List<HistoryResult>> getAllHistoricalData(
            @NonNull String number, @NonNull CallDetailsEntries entries) {
        Assert.isMainThread();
        return null;
    }

    @Override
    public boolean hasStoredData() {
        Assert.isMainThread();
        return false;
    }

    @MainThread
    @Override
    public void requestAllHistoricalData(
            @NonNull String number, @NonNull CallDetailsEntries entries) {
        Assert.isMainThread();
    }

    @Override
    public void unregisterStateChangedListener(@NonNull StateChangedListener listener) {
    }

    @Override
    public void onSessionStatusUpdate(long sessionId, @NonNull String number, int state) {
    }

    @Override
    public void onMessageUpdate(long sessionId, @NonNull String messageId, int state) {
    }

    @Override
    public void onIncomingCallComposerData(long sessionId, @NonNull MultimediaData multimediaData) {
    }

    @Override
    public void onIncomingPostCallData(
            @NonNull PendingResult pendingResult,
            long sessionId,
            @NonNull MultimediaData multimediaData) {
        pendingResult.finish();
    }

    @Override
    public void registerVideoShareListener(@NonNull VideoShareListener listener) {
    }

    @Override
    public void unregisterVideoShareListener(@NonNull VideoShareListener listener) {
    }

    @Override
    public boolean onIncomingVideoShareInvite(long sessionId, @NonNull String number) {
        return false;
    }

    @Override
    public long startVideoShareSession(String number) {
        return Session.NO_SESSION_ID;
    }

    @Override
    public boolean acceptVideoShareSession(long sessionId) {
        return false;
    }

    @Override
    public long getVideoShareInviteSessionId(@NonNull String number) {
        return Session.NO_SESSION_ID;
    }

    @MainThread
    @Nullable
    @Override
    public VideoShareSession getVideoShareSession(long sessionId) {
        Assert.isMainThread();
        return null;
    }

    @Override
    public void endVideoShareSession(long sessionId) {
    }
}
