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

package app.diol.dialer.notification;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.BuildCompat;
import android.telecom.PhoneAccountHandle;
import android.util.ArraySet;

import java.util.Set;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Creates all notification channels for Dialer.
 */
@TargetApi(VERSION_CODES.O)
public final class NotificationChannelManager {

    private NotificationChannelManager() {
    }

    /**
     * Creates all the notification channels Dialer will need. This method is called at app startup
     * and must be fast. Currently it takes between 3 to 7 milliseconds on a Pixel XL.
     *
     * <p>An alternative approach would be to lazily create channels when we actualy post a
     * notification. The advatange to precreating channels is that:
     *
     * <ul>
     * <li>channels will be available to user right away. For example, users can customize voicemail
     * sounds when they first get their device without waiting for a voicemail to arrive first.
     * <li>code that posts a notification can be simpler
     * <li>channel management code is simpler and it's easier to ensure that the correct set of
     * channels are visible.
     * <ul>
     */
    public static void initChannels(@NonNull Context context) {
        Assert.checkArgument(BuildCompat.isAtLeastO());
        Assert.isNotNull(context);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        Set<String> desiredChannelIds = getAllDesiredChannelIds(context);
        Set<String> existingChannelIds = getAllExistingChannelIds(context);

        if (desiredChannelIds.equals(existingChannelIds)) {
            return;
        }
        LogUtil.i(
                "NotificationChannelManager.initChannels",
                "doing an expensive initialization of all notification channels");
        LogUtil.i(
                "NotificationChannelManager.initChannels", "desired channel IDs: " + desiredChannelIds);
        LogUtil.i(
                "NotificationChannelManager.initChannels", "existing channel IDs: " + existingChannelIds);

        // Delete any old channels that we don't use any more. This is safe because if we're recreate
        // this later then any user settings will be restored. An example is SIM specific voicemail
        // channel that gets deleted when the user removes the SIM and is then restored when the user
        // re-inserts the SIM.
        for (String existingChannelId : existingChannelIds) {
            if (!desiredChannelIds.contains(existingChannelId)) {
                notificationManager.deleteNotificationChannel(existingChannelId);
            }
        }

        // Just recreate all desired channels. We won't do this often so it's ok to do this now.
        createIncomingCallChannel(context);
        createOngoingCallChannel(context);
        createMissedCallChannel(context);
        createDefaultChannel(context);
        VoicemailChannelUtils.createAllChannels(context);
    }

    @NonNull
    public static String getVoicemailChannelId(
            @NonNull Context context, @Nullable PhoneAccountHandle handle) {
        Assert.checkArgument(BuildCompat.isAtLeastO());
        Assert.isNotNull(context);
        return VoicemailChannelUtils.getChannelId(context, handle);
    }

    private static Set<String> getAllExistingChannelIds(@NonNull Context context) {
        Set<String> result = new ArraySet<>();
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        for (NotificationChannel channel : notificationManager.getNotificationChannels()) {
            result.add(channel.getId());
        }
        return result;
    }

    private static Set<String> getAllDesiredChannelIds(@NonNull Context context) {
        Set<String> result = new ArraySet<>();
        result.add(NotificationChannelId.INCOMING_CALL);
        result.add(NotificationChannelId.ONGOING_CALL);
        result.add(NotificationChannelId.MISSED_CALL);
        result.add(NotificationChannelId.DEFAULT);
        result.addAll(VoicemailChannelUtils.getAllChannelIds(context));
        return result;
    }

    private static void createIncomingCallChannel(@NonNull Context context) {
        NotificationChannel channel =
                new NotificationChannel(
                        NotificationChannelId.INCOMING_CALL,
                        context.getText(R.string.notification_channel_incoming_call),
                        NotificationManager.IMPORTANCE_MAX);
        channel.setShowBadge(false);
        channel.enableLights(true);
        channel.enableVibration(false);
        channel.setSound(
                null, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private static void createOngoingCallChannel(@NonNull Context context) {
        NotificationChannel channel =
                new NotificationChannel(
                        NotificationChannelId.ONGOING_CALL,
                        context.getText(R.string.notification_channel_ongoing_call),
                        NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(false);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setSound(
                null, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private static void createMissedCallChannel(@NonNull Context context) {
        NotificationChannel channel =
                new NotificationChannel(
                        NotificationChannelId.MISSED_CALL,
                        context.getText(R.string.notification_channel_missed_call),
                        NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setSound(
                null, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private static void createDefaultChannel(@NonNull Context context) {
        NotificationChannel channel =
                new NotificationChannel(
                        NotificationChannelId.DEFAULT,
                        context.getText(R.string.notification_channel_misc),
                        NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(false);
        channel.enableLights(true);
        channel.enableVibration(true);
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
}
