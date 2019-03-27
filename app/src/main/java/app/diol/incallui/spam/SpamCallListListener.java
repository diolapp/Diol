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

package app.diol.incallui.spam;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Icon;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.BuildCompat;
import android.telecom.DisconnectCause;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import java.util.Random;

import app.diol.R;
import app.diol.dialer.blocking.FilteredNumberCompat;
import app.diol.dialer.blocking.FilteredNumbersUtil;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorFactory;
import app.diol.dialer.location.GeoUtil;
import app.diol.dialer.logging.ContactLookupResult;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.notification.DialerNotificationManager;
import app.diol.dialer.notification.NotificationChannelId;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;
import app.diol.dialer.spam.SpamComponent;
import app.diol.dialer.telecom.TelecomUtil;
import app.diol.dialer.theme.base.ThemeComponent;
import app.diol.dialer.util.PermissionsUtil;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.DialerCall.CallHistoryStatus;

/**
 * Creates notifications after a call ends if the call matched the criteria (incoming, accepted,
 * etc).
 */
public class SpamCallListListener implements CallList.Listener {
    /**
     * Common ID for all spam notifications.
     */
    static final int NOTIFICATION_ID = 1;
    /**
     * Prefix used to generate a unique tag for each spam notification.
     */
    static final String NOTIFICATION_TAG_PREFIX = "SpamCall_";
    /**
     * Key used to associate all spam notifications into a single group. Note, unlike other group
     * notifications in Dialer, spam notifications don't have a top level group summary notification.
     * The group is still useful for things like rate limiting on a per group basis.
     */
    private static final String GROUP_KEY = "SpamCallGroup";

    private final Context context;
    private final Random random;
    private final DialerExecutorFactory dialerExecutorFactory;

    public SpamCallListListener(Context context, @NonNull DialerExecutorFactory factory) {
        this(context, new Random(), factory);
    }

    public SpamCallListListener(
            Context context, Random rand, @NonNull DialerExecutorFactory factory) {
        this.context = context;
        this.random = rand;
        this.dialerExecutorFactory = Assert.isNotNull(factory);
    }

    static String getNotificationTagForCall(@NonNull DialerCall call) {
        return NOTIFICATION_TAG_PREFIX + call.getUniqueCallId();
    }

    @Override
    public void onIncomingCall(final DialerCall call) {
        String number = call.getNumber();
        if (TextUtils.isEmpty(number)) {
            return;
        }

        if (!PermissionsUtil.hasCallLogReadPermissions(context)) {
            LogUtil.i(
                    "SpamCallListListener.onIncomingCall",
                    "call log permission missing, not checking if number is in call history");
            return;
        }

        NumberInCallHistoryWorker historyTask =
                new NumberInCallHistoryWorker(context, number, call.getCountryIso());
        dialerExecutorFactory
                .createNonUiTaskBuilder(historyTask)
                .onSuccess((result) -> call.setCallHistoryStatus(result))
                .build()
                .executeParallel(null);
    }

    @Override
    public void onUpgradeToVideo(DialerCall call) {
    }

    @Override
    public void onSessionModificationStateChange(DialerCall call) {
    }

    @Override
    public void onCallListChange(CallList callList) {
    }

    @Override
    public void onWiFiToLteHandover(DialerCall call) {
    }

    @Override
    public void onHandoverToWifiFailed(DialerCall call) {
    }

    @Override
    public void onInternationalCallOnWifi(@NonNull DialerCall call) {
    }

    @Override
    public void onDisconnect(DialerCall call) {
        if (!shouldShowAfterCallNotification(call)) {
            return;
        }
        String e164Number =
                PhoneNumberUtils.formatNumberToE164(
                        call.getNumber(), GeoUtil.getCurrentCountryIso(context));
        if (!FilteredNumbersUtil.canBlockNumber(context, e164Number, call.getNumber())
                || !FilteredNumberCompat.canAttemptBlockOperations(context)) {
            return;
        }
        if (e164Number == null) {
            return;
        }
        showNotification(call);
    }

    /**
     * Posts the intent for displaying the after call spam notification to the user.
     */
    private void showNotification(DialerCall call) {
        if (call.isSpam()) {
            maybeShowSpamCallNotification(call);
        } else {
            maybeShowNonSpamCallNotification(call);
        }
    }

    /**
     * Determines if the after call notification should be shown for the specified call.
     */
    private boolean shouldShowAfterCallNotification(DialerCall call) {
        if (!SpamComponent.get(context).spamSettings().isSpamNotificationEnabled()) {
            return false;
        }

        String number = call.getNumber();
        if (TextUtils.isEmpty(number)) {
            return false;
        }

        DialerCall.LogState logState = call.getLogState();
        if (!logState.isIncoming) {
            return false;
        }

        if (logState.telecomDurationMillis <= 0) {
            return false;
        }

        if (logState.contactLookupResult != ContactLookupResult.Type.NOT_FOUND
                && logState.contactLookupResult != ContactLookupResult.Type.UNKNOWN_LOOKUP_RESULT_TYPE) {
            return false;
        }

        int callHistoryStatus = call.getCallHistoryStatus();
        if (callHistoryStatus == DialerCall.CALL_HISTORY_STATUS_PRESENT) {
            return false;
        } else if (callHistoryStatus == DialerCall.CALL_HISTORY_STATUS_UNKNOWN) {
            LogUtil.i("SpamCallListListener.shouldShowAfterCallNotification", "history status unknown");
            return false;
        }

        // Check if call disconnected because of either user hanging up
        int disconnectCause = call.getDisconnectCause().getCode();
        if (disconnectCause != DisconnectCause.LOCAL && disconnectCause != DisconnectCause.REMOTE) {
            return false;
        }

        LogUtil.i("SpamCallListListener.shouldShowAfterCallNotification", "returning true");
        return true;
    }

    /**
     * Creates a notification builder with properties common among the two after call notifications.
     */
    private Notification.Builder createAfterCallNotificationBuilder(DialerCall call) {
        Notification.Builder builder =
                new Builder(context)
                        .setContentIntent(
                                createActivityPendingIntent(call, SpamNotificationActivity.ACTION_SHOW_DIALOG))
                        .setCategory(Notification.CATEGORY_STATUS)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setColor(ThemeComponent.get(context).theme().getColorPrimary())
                        .setSmallIcon(R.drawable.quantum_ic_call_end_vd_theme_24)
                        .setGroup(GROUP_KEY);
        if (BuildCompat.isAtLeastO()) {
            builder.setChannelId(NotificationChannelId.DEFAULT);
        }
        return builder;
    }

    private CharSequence getDisplayNumber(DialerCall call) {
        String formattedNumber =
                PhoneNumberHelper.formatNumber(
                        context, call.getNumber(), GeoUtil.getCurrentCountryIso(context));
        return PhoneNumberUtils.createTtsSpannable(formattedNumber);
    }

    /**
     * Display a notification with two actions: "add contact" and "report spam".
     */
    private void showNonSpamCallNotification(DialerCall call) {
        Notification.Builder notificationBuilder =
                createAfterCallNotificationBuilder(call)
                        .setContentText(
                                context.getString(
                                        SpamAlternativeExperimentUtil.getResourceIdByName(
                                                "spam_notification_non_spam_call_collapsed_text", context)))
                        .setStyle(
                                new Notification.BigTextStyle()
                                        .bigText(
                                                context.getString(
                                                        SpamAlternativeExperimentUtil.getResourceIdByName(
                                                                "spam_notification_non_spam_call_expanded_text", context))))
                        // Add contact
                        .addAction(
                                new Notification.Action.Builder(
                                        R.drawable.quantum_ic_person_add_vd_theme_24,
                                        context.getString(R.string.spam_notification_add_contact_action_text),
                                        createActivityPendingIntent(
                                                call, SpamNotificationActivity.ACTION_ADD_TO_CONTACTS))
                                        .build())
                        // Block/report spam
                        .addAction(
                                new Notification.Action.Builder(
                                        R.drawable.quantum_ic_block_vd_theme_24,
                                        context.getString(R.string.spam_notification_report_spam_action_text),
                                        createBlockReportSpamPendingIntent(call))
                                        .build())
                        .setContentTitle(
                                context.getString(R.string.non_spam_notification_title, getDisplayNumber(call)));
        DialerNotificationManager.notify(
                context, getNotificationTagForCall(call), NOTIFICATION_ID, notificationBuilder.build());
    }

    private boolean shouldThrottleSpamNotification() {
        int randomNumber = random.nextInt(100);
        int thresholdForShowing =
                SpamComponent.get(context).spamSettings().percentOfSpamNotificationsToShow();
        if (thresholdForShowing == 0) {
            LogUtil.d(
                    "SpamCallListListener.shouldThrottleSpamNotification",
                    "not showing - percentOfSpamNotificationsToShow is 0");
            return true;
        } else if (randomNumber < thresholdForShowing) {
            LogUtil.d(
                    "SpamCallListListener.shouldThrottleSpamNotification",
                    "showing " + randomNumber + " < " + thresholdForShowing);
            return false;
        } else {
            LogUtil.d(
                    "SpamCallListListener.shouldThrottleSpamNotification",
                    "not showing %d >= %d",
                    randomNumber,
                    thresholdForShowing);
            return true;
        }
    }

    private boolean shouldThrottleNonSpamNotification() {
        int randomNumber = random.nextInt(100);
        int thresholdForShowing =
                SpamComponent.get(context).spamSettings().percentOfNonSpamNotificationsToShow();
        if (thresholdForShowing == 0) {
            LogUtil.d(
                    "SpamCallListListener.shouldThrottleNonSpamNotification",
                    "not showing non spam notification: percentOfNonSpamNotificationsToShow is 0");
            return true;
        } else if (randomNumber < thresholdForShowing) {
            LogUtil.d(
                    "SpamCallListListener.shouldThrottleNonSpamNotification",
                    "showing non spam notification: %d < %d",
                    randomNumber,
                    thresholdForShowing);
            return false;
        } else {
            LogUtil.d(
                    "SpamCallListListener.shouldThrottleNonSpamNotification",
                    "not showing non spam notification: %d >= %d",
                    randomNumber,
                    thresholdForShowing);
            return true;
        }
    }

    private void maybeShowSpamCallNotification(DialerCall call) {
        if (shouldThrottleSpamNotification()) {
            Logger.get(context)
                    .logCallImpression(
                            DialerImpression.Type.SPAM_NOTIFICATION_NOT_SHOWN_AFTER_THROTTLE,
                            call.getUniqueCallId(),
                            call.getTimeAddedMs());
        } else {
            Logger.get(context)
                    .logCallImpression(
                            DialerImpression.Type.SPAM_NOTIFICATION_SHOWN_AFTER_THROTTLE,
                            call.getUniqueCallId(),
                            call.getTimeAddedMs());
            showSpamCallNotification(call);
        }
    }

    private void maybeShowNonSpamCallNotification(DialerCall call) {
        if (shouldThrottleNonSpamNotification()) {
            Logger.get(context)
                    .logCallImpression(
                            DialerImpression.Type.NON_SPAM_NOTIFICATION_NOT_SHOWN_AFTER_THROTTLE,
                            call.getUniqueCallId(),
                            call.getTimeAddedMs());
        } else {
            Logger.get(context)
                    .logCallImpression(
                            DialerImpression.Type.NON_SPAM_NOTIFICATION_SHOWN_AFTER_THROTTLE,
                            call.getUniqueCallId(),
                            call.getTimeAddedMs());
            showNonSpamCallNotification(call);
        }
    }

    /**
     * Display a notification with the action "not spam".
     */
    private void showSpamCallNotification(DialerCall call) {
        Notification.Builder notificationBuilder =
                createAfterCallNotificationBuilder(call)
                        .setLargeIcon(Icon.createWithResource(context, R.drawable.spam_notification_icon))
                        .setContentText(
                                context.getString(
                                        SpamAlternativeExperimentUtil.getResourceIdByName(
                                                "spam_notification_spam_call_collapsed_text", context)))
                        // Not spam
                        .addAction(
                                new Notification.Action.Builder(
                                        R.drawable.quantum_ic_close_vd_theme_24,
                                        context.getString(
                                                SpamAlternativeExperimentUtil.getResourceIdByName(
                                                        "spam_notification_was_not_spam_action_text", context)),
                                        createNotSpamPendingIntent(call))
                                        .build())
                        // Block/report spam
                        .addAction(
                                new Notification.Action.Builder(
                                        R.drawable.quantum_ic_block_vd_theme_24,
                                        context.getString(R.string.spam_notification_block_spam_action_text),
                                        createBlockReportSpamPendingIntent(call))
                                        .build())
                        .setContentTitle(
                                context.getString(
                                        SpamAlternativeExperimentUtil.getResourceIdByName(
                                                "spam_notification_title", context),
                                        getDisplayNumber(call)));
        DialerNotificationManager.notify(
                context, getNotificationTagForCall(call), NOTIFICATION_ID, notificationBuilder.build());
    }


    /**
     * Creates a pending intent for block/report spam action. If enabled, this intent is forwarded to
     * the {@link SpamNotificationActivity}, otherwise to the {@link SpamNotificationService}.
     */
    private PendingIntent createBlockReportSpamPendingIntent(DialerCall call) {
        String action = SpamNotificationActivity.ACTION_MARK_NUMBER_AS_SPAM;
        return SpamComponent.get(context).spamSettings().isDialogEnabledForSpamNotification()
                ? createActivityPendingIntent(call, action)
                : createServicePendingIntent(call, action);
    }

    /**
     * Creates a pending intent for not spam action. If enabled, this intent is forwarded to the
     * {@link SpamNotificationActivity}, otherwise to the {@link SpamNotificationService}.
     */
    private PendingIntent createNotSpamPendingIntent(DialerCall call) {
        String action = SpamNotificationActivity.ACTION_MARK_NUMBER_AS_NOT_SPAM;
        return SpamComponent.get(context).spamSettings().isDialogEnabledForSpamNotification()
                ? createActivityPendingIntent(call, action)
                : createServicePendingIntent(call, action);
    }

    /**
     * Creates a pending intent for {@link SpamNotificationService}.
     */
    private PendingIntent createServicePendingIntent(DialerCall call, String action) {
        Intent intent =
                SpamNotificationService.createServiceIntent(
                        context, call, action, getNotificationTagForCall(call), NOTIFICATION_ID);
        return PendingIntent.getService(
                context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);
    }

    /**
     * Creates a pending intent for {@link SpamNotificationActivity}.
     */
    private PendingIntent createActivityPendingIntent(DialerCall call, String action) {
        Intent intent =
                SpamNotificationActivity.createActivityIntent(
                        context, call, action, getNotificationTagForCall(call), NOTIFICATION_ID);
        return PendingIntent.getActivity(
                context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);
    }

    /**
     * Checks if the number is in the call history.
     */
    private static final class NumberInCallHistoryWorker implements Worker<Void, Integer> {

        private final Context appContext;
        private final String number;
        private final String countryIso;

        public NumberInCallHistoryWorker(
                @NonNull Context appContext, String number, String countryIso) {
            this.appContext = Assert.isNotNull(appContext);
            this.number = number;
            this.countryIso = countryIso;
        }

        @Override
        @NonNull
        @CallHistoryStatus
        public Integer doInBackground(@Nullable Void input) throws Throwable {
            String numberToQuery = number;
            String fieldToQuery = Calls.NUMBER;
            String normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso);

            // If we can normalize the number successfully, look in "normalized_number"
            // field instead. Otherwise, look for number in "number" field.
            if (!TextUtils.isEmpty(normalizedNumber)) {
                numberToQuery = normalizedNumber;
                fieldToQuery = Calls.CACHED_NORMALIZED_NUMBER;
            }

            try (Cursor cursor =
                         appContext
                                 .getContentResolver()
                                 .query(
                                         TelecomUtil.getCallLogUri(appContext),
                                         new String[]{CallLog.Calls._ID},
                                         fieldToQuery + " = ?",
                                         new String[]{numberToQuery},
                                         null)) {
                return cursor != null && cursor.getCount() > 0
                        ? DialerCall.CALL_HISTORY_STATUS_PRESENT
                        : DialerCall.CALL_HISTORY_STATUS_NOT_PRESENT;
            } catch (SQLiteException e) {
                LogUtil.e("NumberInCallHistoryWorker.doInBackground", "query call log error", e);
                return DialerCall.CALL_HISTORY_STATUS_UNKNOWN;
            }
        }
    }
}
