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

package app.diol.dialer.spam.promo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.Icon;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.os.BuildCompat;
import android.view.View;
import android.widget.Toast;

import app.diol.R;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.notification.DialerNotificationManager;
import app.diol.dialer.notification.NotificationChannelId;
import app.diol.dialer.spam.SpamSettings;
import app.diol.dialer.spam.promo.SpamBlockingPromoDialogFragment.OnEnableListener;
import app.diol.dialer.storage.StorageComponent;
import app.diol.dialer.theme.base.ThemeComponent;

/**
 * Helper class for showing spam blocking on-boarding promotions.
 */
public class SpamBlockingPromoHelper {

    public static final String ENABLE_SPAM_BLOCKING_PROMO = "enable_spam_blocking_promo";
    public static final String ENABLE_AFTER_CALL_SPAM_BLOCKING_PROMO =
            "enable_after_call_spam_blocking_promo";
    static final String SPAM_BLOCKING_PROMO_PERIOD_MILLIS = "spam_blocking_promo_period_millis";
    static final String SPAM_BLOCKING_PROMO_LAST_SHOW_MILLIS = "spam_blocking_promo_last_show_millis";
    private final Context context;
    private final SpamSettings spamSettings;

    public SpamBlockingPromoHelper(Context context, SpamSettings spamSettings) {
        this.context = context;
        this.spamSettings = spamSettings;
    }

    /**
     * Returns true if we should show a spam blocking promo.
     *
     * <p>Should show spam blocking promo only when all of the following criteria meet 1. Spam
     * blocking promo is enabled by flag. 2. Spam blocking setting is available. 3. Spam blocking
     * setting is not yet enabled. 4. Time since last spam blocking promo exceeds the threshold.
     *
     * @return true if we should show a spam blocking promo.
     */
    public boolean shouldShowSpamBlockingPromo() {
        if (!ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getBoolean(ENABLE_SPAM_BLOCKING_PROMO, false)
                || !spamSettings.isSpamEnabled()
                || !spamSettings.isSpamBlockingEnabledByFlag()
                || spamSettings.isSpamBlockingEnabledByUser()) {
            return false;
        }

        long lastShowMillis =
                StorageComponent.get(context)
                        .unencryptedSharedPrefs()
                        .getLong(SPAM_BLOCKING_PROMO_LAST_SHOW_MILLIS, 0);
        long showPeriodMillis =
                ConfigProviderComponent.get(context)
                        .getConfigProvider()
                        .getLong(SPAM_BLOCKING_PROMO_PERIOD_MILLIS, Long.MAX_VALUE);
        return lastShowMillis == 0 || System.currentTimeMillis() - lastShowMillis > showPeriodMillis;
    }

    /* Returns true if we should show a spam blocking promo in after call notification scenario. */
    public boolean shouldShowAfterCallSpamBlockingPromo() {
        return shouldShowSpamBlockingPromo()
                && ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getBoolean(ENABLE_AFTER_CALL_SPAM_BLOCKING_PROMO, false);
    }

    /**
     * Shows a spam blocking promo dialog.
     *
     * @param fragmentManager   the fragment manager to show the dialog.
     * @param onEnableListener  the listener called when enable button is clicked.
     * @param onDismissListener the listener called when the dialog is dismissed.
     */
    public void showSpamBlockingPromoDialog(
            FragmentManager fragmentManager,
            OnEnableListener onEnableListener,
            OnDismissListener onDismissListener) {
        updateLastShowSpamTimestamp();
        SpamBlockingPromoDialogFragment.newInstance(onEnableListener, onDismissListener)
                .show(fragmentManager, SpamBlockingPromoDialogFragment.SPAM_BLOCKING_PROMO_DIALOG_TAG);
    }

    private void updateLastShowSpamTimestamp() {
        StorageComponent.get(context)
                .unencryptedSharedPrefs()
                .edit()
                .putLong(SPAM_BLOCKING_PROMO_LAST_SHOW_MILLIS, System.currentTimeMillis())
                .apply();
    }

    /**
     * Shows a modify setting on complete snackbar and a link to redirect to setting page.
     *
     * @param view    the view to attach on-complete notice snackbar.
     * @param success whether the modify setting operation succceeds.
     */
    public void showModifySettingOnCompleteSnackbar(View view, boolean success) {
        String snackBarText =
                success
                        ? context.getString(R.string.spam_blocking_settings_enable_complete_text)
                        : context.getString(R.string.spam_blocking_settings_enable_error_text);
        Snackbar.make(view, snackBarText, Snackbar.LENGTH_LONG)
                .setAction(
                        R.string.spam_blocking_setting_prompt,
                        v -> context.startActivity(spamSettings.getSpamBlockingSettingIntent(context)))
                .setActionTextColor(
                        context.getResources().getColor(R.color.dialer_snackbar_action_text_color))
                .show();
    }

    /**
     * Shows a modify setting on complete toast message.
     */
    public void showModifySettingOnCompleteToast(boolean success) {
        String toastText =
                success
                        ? context.getString(R.string.spam_blocking_settings_enable_complete_text)
                        : context.getString(R.string.spam_blocking_settings_enable_error_text);
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a spam blocking promo notification.
     *
     * @param notificationTag a string identifier for this notification.
     * @param notificationId  an identifier for this notification.
     * @param contentIntent   pending intent to be sent when notification is clicked.
     * @param actionIntent    pending intent to be sent when enable-spam-blocking button is clicked.
     */
    public void showSpamBlockingPromoNotification(
            String notificationTag,
            int notificationId,
            PendingIntent contentIntent,
            PendingIntent actionIntent) {
        updateLastShowSpamTimestamp();
        Logger.get(context)
                .logImpression(DialerImpression.Type.SPAM_BLOCKING_AFTER_CALL_NOTIFICATION_PROMO_SHOWN);
        DialerNotificationManager.notify(
                context,
                notificationTag,
                notificationId,
                getSpamBlockingPromoNotification(contentIntent, actionIntent));
    }

    /**
     * Builds a spam blocking promo notification with given intents.
     *
     * @param contentIntent pending intent to be sent when notification is clicked.
     * @param actionIntent  pending intent to be sent when enable-spam-blocking button is clicked.
     */
    @SuppressLint("NewApi")
    private Notification getSpamBlockingPromoNotification(
            PendingIntent contentIntent, PendingIntent actionIntent) {
        Notification.Builder builder =
                new Builder(context)
                        .setContentIntent(contentIntent)
                        .setCategory(Notification.CATEGORY_STATUS)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setColor(ThemeComponent.get(context).theme().getColorPrimary())
                        .setSmallIcon(R.drawable.quantum_ic_call_vd_theme_24)
                        .setLargeIcon(Icon.createWithResource(context, R.drawable.spam_blocking_promo_icon))
                        .setContentText(context.getString(R.string.spam_blocking_promo_text))
                        .setStyle(
                                new Notification.BigTextStyle()
                                        .bigText(context.getString(R.string.spam_blocking_promo_text)))
                        .addAction(
                                new Notification.Action.Builder(
                                        R.drawable.quantum_ic_block_vd_theme_24,
                                        context.getString(R.string.spam_blocking_promo_action_filter_spam),
                                        actionIntent)
                                        .build())
                        .setContentTitle(context.getString(R.string.spam_blocking_promo_title));

        if (BuildCompat.isAtLeastO()) {
            builder.setChannelId(NotificationChannelId.DEFAULT);
        }
        return builder.build();
    }
}

