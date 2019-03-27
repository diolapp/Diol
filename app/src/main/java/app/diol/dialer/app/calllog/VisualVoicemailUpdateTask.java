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

package app.diol.dialer.app.calllog;

import android.content.Context;
import android.net.Uri;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.diol.R;
import app.diol.dialer.app.calllog.CallLogNotificationsQueryHelper.NewCall;
import app.diol.dialer.blocking.FilteredNumberAsyncQueryHandler;
import app.diol.dialer.blocking.FilteredNumbersUtil;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.notification.DialerNotificationManager;
import app.diol.dialer.phonenumbercache.ContactInfo;
import app.diol.dialer.spam.SpamComponent;
import app.diol.dialer.telecom.TelecomUtil;

/**
 * Updates voicemail notifications in the background.
 */
class VisualVoicemailUpdateTask implements Worker<VisualVoicemailUpdateTask.Input, Void> {
    /**
     * Updates the notification and notifies of the call with the given URI.
     *
     * <p>Clears the notification if there are no new voicemails, and notifies if the given URI
     * corresponds to a new voicemail.
     */
    @WorkerThread
    private static void updateNotification(
            Context context,
            CallLogNotificationsQueryHelper queryHelper,
            FilteredNumberAsyncQueryHandler queryHandler) {
        Assert.isWorkerThread();
        LogUtil.enterBlock("VisualVoicemailUpdateTask.updateNotification");

        List<NewCall> voicemailsToNotify = queryHelper.getNewVoicemails();
        if (voicemailsToNotify == null) {
            // Query failed, just return
            return;
        }

        if (FilteredNumbersUtil.hasRecentEmergencyCall(context)) {
            LogUtil.i(
                    "VisualVoicemailUpdateTask.updateNotification",
                    "not filtering due to recent emergency call");
        } else {
            voicemailsToNotify = filterBlockedNumbers(context, queryHandler, voicemailsToNotify);
            voicemailsToNotify = filterSpamNumbers(context, voicemailsToNotify);
        }
        boolean shouldAlert =
                !voicemailsToNotify.isEmpty()
                        && voicemailsToNotify.size() > getExistingNotificationCount(context);
        voicemailsToNotify.addAll(getAndUpdateVoicemailsWithExistingNotification(context, queryHelper));
        if (voicemailsToNotify.isEmpty()) {
            LogUtil.i("VisualVoicemailUpdateTask.updateNotification", "no voicemails to notify about");
            VisualVoicemailNotifier.cancelAllVoicemailNotifications(context);
            VoicemailNotificationJobService.cancelJob(context);
            return;
        }

        // This represents a list of names to include in the notification.
        String callers = null;

        // Maps each number into a name: if a number is in the map, it has already left a more
        // recent voicemail.
        Map<String, ContactInfo> contactInfos = new ArrayMap<>();
        for (NewCall newCall : voicemailsToNotify) {
            if (!contactInfos.containsKey(newCall.number)) {
                ContactInfo contactInfo =
                        queryHelper.getContactInfo(
                                newCall.number, newCall.numberPresentation, newCall.countryIso);
                contactInfos.put(newCall.number, contactInfo);

                // This is a new caller. Add it to the back of the list of callers.
                if (TextUtils.isEmpty(callers)) {
                    callers = contactInfo.name;
                } else {
                    callers =
                            context.getString(
                                    R.string.notification_voicemail_callers_list, callers, contactInfo.name);
                }
            }
        }
        VisualVoicemailNotifier.showNotifications(
                context, voicemailsToNotify, contactInfos, callers, shouldAlert);

        // Set trigger to update notifications when database changes.
        VoicemailNotificationJobService.scheduleJob(context);
    }

    @WorkerThread
    @NonNull
    private static int getExistingNotificationCount(Context context) {
        Assert.isWorkerThread();
        int result = 0;
        for (StatusBarNotification notification :
                DialerNotificationManager.getActiveNotifications(context)) {
            if (notification.getId() != VisualVoicemailNotifier.NOTIFICATION_ID) {
                continue;
            }
            if (TextUtils.isEmpty(notification.getTag())
                    || !notification.getTag().startsWith(VisualVoicemailNotifier.NOTIFICATION_TAG_PREFIX)) {
                continue;
            }
            result++;
        }
        return result;
    }

    /**
     * Cancel notification for voicemail that is already deleted. Returns a list of voicemails that
     * already has notifications posted and should be updated.
     */
    @WorkerThread
    @NonNull
    private static List<NewCall> getAndUpdateVoicemailsWithExistingNotification(
            Context context, CallLogNotificationsQueryHelper queryHelper) {
        Assert.isWorkerThread();
        List<NewCall> result = new ArrayList<>();
        for (StatusBarNotification notification :
                DialerNotificationManager.getActiveNotifications(context)) {
            if (notification.getId() != VisualVoicemailNotifier.NOTIFICATION_ID) {
                continue;
            }
            if (TextUtils.isEmpty(notification.getTag())
                    || !notification.getTag().startsWith(VisualVoicemailNotifier.NOTIFICATION_TAG_PREFIX)) {
                continue;
            }
            String uri =
                    notification.getTag().replace(VisualVoicemailNotifier.NOTIFICATION_TAG_PREFIX, "");
            NewCall existingCall = queryHelper.getNewCallsQuery().queryUnreadVoicemail(Uri.parse(uri));
            if (existingCall != null) {
                result.add(existingCall);
            } else {
                LogUtil.i(
                        "VisualVoicemailUpdateTask.getVoicemailsWithExistingNotification",
                        "voicemail deleted, removing notification");
                DialerNotificationManager.cancel(context, notification.getTag(), notification.getId());
            }
        }
        return result;
    }

    @WorkerThread
    private static List<NewCall> filterBlockedNumbers(
            Context context, FilteredNumberAsyncQueryHandler queryHandler, List<NewCall> newCalls) {
        Assert.isWorkerThread();
        List<NewCall> result = new ArrayList<>();
        for (NewCall newCall : newCalls) {
            if (queryHandler.getBlockedIdSynchronous(newCall.number, newCall.countryIso) != null) {
                LogUtil.i(
                        "VisualVoicemailUpdateTask.filterBlockedNumbers",
                        "found voicemail from blocked number, deleting");
                if (newCall.voicemailUri != null) {
                    // Delete the voicemail.
                    CallLogAsyncTaskUtil.deleteVoicemailSynchronous(context, newCall.voicemailUri);
                }
            } else {
                result.add(newCall);
            }
        }
        return result;
    }

    @WorkerThread
    private static List<NewCall> filterSpamNumbers(Context context, List<NewCall> newCalls) {
        Assert.isWorkerThread();
        if (!SpamComponent.get(context).spamSettings().isSpamBlockingEnabled()) {
            return newCalls;
        }

        List<NewCall> result = new ArrayList<>();
        for (NewCall newCall : newCalls) {
            Logger.get(context).logImpression(DialerImpression.Type.INCOMING_VOICEMAIL_SCREENED);
            if (SpamComponent.get(context)
                    .spam()
                    .checkSpamStatusSynchronous(newCall.number, newCall.countryIso)) {
                LogUtil.i(
                        "VisualVoicemailUpdateTask.filterSpamNumbers",
                        "found voicemail from spam number, suppressing notification");
                Logger.get(context)
                        .logImpression(DialerImpression.Type.INCOMING_VOICEMAIL_AUTO_BLOCKED_AS_SPAM);
                if (newCall.voicemailUri != null) {
                    // Mark auto blocked voicemail as old so that we don't process it again.
                    VoicemailQueryHandler.markSingleNewVoicemailAsOld(context, newCall.voicemailUri);
                }
            } else {
                result.add(newCall);
            }
        }
        return result;
    }

    /**
     * Updates the voicemail notifications displayed.
     */
    static void scheduleTask(@NonNull Context context, @NonNull Runnable callback) {
        Assert.isNotNull(context);
        Assert.isNotNull(callback);
        if (!TelecomUtil.isDefaultDialer(context)) {
            LogUtil.i("VisualVoicemailUpdateTask.scheduleTask", "not default dialer, not running");
            callback.run();
            return;
        }

        Input input =
                new Input(
                        context,
                        CallLogNotificationsQueryHelper.getInstance(context),
                        new FilteredNumberAsyncQueryHandler(context));
        DialerExecutorComponent.get(context)
                .dialerExecutorFactory()
                .createNonUiTaskBuilder(new VisualVoicemailUpdateTask())
                .onSuccess(
                        output -> {
                            LogUtil.i("VisualVoicemailUpdateTask.scheduleTask", "update successful");
                            callback.run();
                        })
                .onFailure(
                        throwable -> {
                            LogUtil.i("VisualVoicemailUpdateTask.scheduleTask", "update failed: " + throwable);
                            callback.run();
                        })
                .build()
                .executeParallel(input);
    }

    @Nullable
    @Override
    public Void doInBackground(@NonNull Input input) throws Throwable {
        updateNotification(input.context, input.queryHelper, input.queryHandler);
        return null;
    }

    static class Input {
        @NonNull
        final Context context;
        @NonNull
        final CallLogNotificationsQueryHelper queryHelper;
        @NonNull
        final FilteredNumberAsyncQueryHandler queryHandler;

        Input(
                Context context,
                CallLogNotificationsQueryHelper queryHelper,
                FilteredNumberAsyncQueryHandler queryHandler) {
            this.context = context;
            this.queryHelper = queryHelper;
            this.queryHandler = queryHandler;
        }
    }
}
