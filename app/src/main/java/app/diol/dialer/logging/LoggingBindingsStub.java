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

package app.diol.dialer.logging;

import android.app.Activity;
import android.widget.QuickContactBadge;

import java.util.Collection;

/**
 * Default implementation for logging bindings.
 */
public class LoggingBindingsStub implements LoggingBindings {

    @Override
    public void logImpression(DialerImpression.Type dialerImpression) {
    }

    @Override
    public void logImpression(int dialerImpression) {
    }

    @Override
    public void logCallImpression(
            DialerImpression.Type dialerImpression, String callId, long callStartTimeMillis) {
    }

    @Override
    public void logInteraction(InteractionEvent.Type interaction) {
    }

    @Override
    public void logScreenView(ScreenEvent.Type screenEvent, Activity activity) {
    }

    @Override
    public void logSpeedDialContactComposition(
            int counter,
            int starredContactsCount,
            int pinnedContactsCount,
            int multipleNumbersContactsCount,
            int contactsWithPhotoCount,
            int contactsWithNameCount,
            int lightbringerReachableContactsCount) {
    }

    @Override
    public void sendHitEventAnalytics(String category, String action, String label, long value) {
    }

    @Override
    public void logQuickContactOnTouch(
            QuickContactBadge quickContact,
            InteractionEvent.Type interactionEvent,
            boolean shouldPerformClick) {
    }

    @Override
    public void logPeopleApiLookupReportWithError(
            long latency, int httpResponseCode, PeopleApiLookupError.Type errorType) {
    }

    @Override
    public void logSuccessfulPeopleApiLookupReport(long latency, int httpResponseCode) {
    }

    @Override
    public void logAutoBlockedCall(String phoneNumber) {
    }

    @Override
    public void logAnnotatedCallLogMetrics(int invalidNumbersInCallLog) {
    }

    @Override
    public void logAnnotatedCallLogMetrics(int numberRowsThatDidPop, int numberRowsThatDidNotPop) {
    }

    @Override
    public void logContactsProviderMetrics(Collection<ContactsProviderMatchInfo> matchInfos) {
    }
}
