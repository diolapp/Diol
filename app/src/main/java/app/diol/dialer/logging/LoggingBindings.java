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

import com.google.auto.value.AutoValue;

import java.util.Collection;

/**
 * Allows the container application to gather analytics.
 */
public interface LoggingBindings {

    /**
     * Logs an DialerImpression event that's not associated with a specific call.
     *
     * @param dialerImpression an integer representing what event occurred.
     */
    void logImpression(DialerImpression.Type dialerImpression);

    /**
     * Logs an impression for a general dialer event that's not associated with a specific call.
     *
     * @param dialerImpression an integer representing what event occurred.
     */
    @Deprecated
    void logImpression(int dialerImpression);

    /**
     * Logs an impression for a general dialer event that's associated with a specific call.
     *
     * @param dialerImpression    an integer representing what event occurred.
     * @param callId              unique ID of the call.
     * @param callStartTimeMillis the absolute time when the call started.
     */
    void logCallImpression(
            DialerImpression.Type dialerImpression, String callId, long callStartTimeMillis);

    /**
     * Logs an interaction that occurred.
     *
     * @param interaction an integer representing what interaction occurred.
     * @see app.diol.dialer.logging.InteractionEvent
     */
    void logInteraction(InteractionEvent.Type interaction);

    /**
     * Logs an event indicating that a screen was displayed.
     *
     * @param screenEvent an integer representing the displayed screen.
     * @param activity    Parent activity of the displayed screen.
     * @see app.diol.dialer.logging.ScreenEvent
     */
    void logScreenView(app.diol.dialer.logging.ScreenEvent.Type screenEvent, Activity activity);

    /**
     * Logs the composition of contact tiles in the speed dial tab.
     */
    void logSpeedDialContactComposition(
            int counter,
            int starredContactsCount,
            int pinnedContactsCount,
            int multipleNumbersContactsCount,
            int contactsWithPhotoCount,
            int contactsWithNameCount,
            int lightbringerReachableContactsCount);

    /**
     * Logs a hit event to the analytics server.
     */
    void sendHitEventAnalytics(String category, String action, String label, long value);

    /**
     * Logs where a quick contact badge is clicked
     */
    void logQuickContactOnTouch(
            QuickContactBadge quickContact,
            InteractionEvent.Type interactionEvent,
            boolean shouldPerformClick);

    /**
     * Logs People Api lookup result with error
     */
    void logPeopleApiLookupReportWithError(
            long latency, int httpResponseCode, PeopleApiLookupError.Type errorType);

    /**
     * Logs successful People Api lookup result
     */
    void logSuccessfulPeopleApiLookupReport(long latency, int httpResponseCode);

    /**
     * Logs a call auto-blocked in call screening.
     */
    void logAutoBlockedCall(String phoneNumber);

    /**
     * Logs annotated call log metrics.
     */
    void logAnnotatedCallLogMetrics(int invalidNumbersInCallLog);

    /**
     * Logs annotated call log metrics.
     */
    void logAnnotatedCallLogMetrics(int numberRowsThatDidPop, int numberRowsThatDidNotPop);

    /**
     * Logs contacts provider metrics.
     */
    void logContactsProviderMetrics(Collection<ContactsProviderMatchInfo> matchInfos);

    /**
     * Input type for {@link #logContactsProviderMetrics(Collection)}.
     */
    @AutoValue
    abstract class ContactsProviderMatchInfo {
        public static Builder builder() {
            return new AutoValue_LoggingBindings_ContactsProviderMatchInfo.Builder()
                    .setMatchedContact(false)
                    .setMatchedNumberLength(0)
                    .setMatchedNumberHasPostdialDigits(false);
        }

        public abstract boolean matchedContact();

        public abstract boolean inputNumberValid();

        public abstract int inputNumberLength();

        public abstract int matchedNumberLength();

        public abstract boolean inputNumberHasPostdialDigits();

        public abstract boolean matchedNumberHasPostdialDigits();

        /**
         * Builder.
         */
        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder setMatchedContact(boolean value);

            public abstract Builder setInputNumberValid(boolean value);

            public abstract Builder setInputNumberLength(int value);

            public abstract Builder setMatchedNumberLength(int value);

            public abstract Builder setInputNumberHasPostdialDigits(boolean value);

            public abstract Builder setMatchedNumberHasPostdialDigits(boolean value);

            public abstract ContactsProviderMatchInfo build();
        }
    }
}
