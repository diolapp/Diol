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

package app.diol.dialer.spam;

import android.content.Context;
import android.content.Intent;

/**
 * Allows the container application to interact with spam settings.
 */
public interface SpamSettings {

    /**
     * @return if spam module is enabled
     */
    boolean isSpamEnabled();

    /**
     * @return if spam after call notification is enabled
     */
    boolean isSpamNotificationEnabled();

    /**
     * @return if spam blocking is enabled
     */
    boolean isSpamBlockingEnabled();

    /**
     * @return if spam blocking user setting is controlled by carrier
     */
    boolean isSpamBlockingControlledByCarrier();

    /**
     * @return if spam blocking module is enabled by flag
     */
    boolean isSpamBlockingEnabledByFlag();

    /**
     * @return if spam blocking setting is enabled by user
     */
    boolean isSpamBlockingEnabledByUser();

    /**
     * @return if dialog is used by default for spam after call notification
     */
    boolean isDialogEnabledForSpamNotification();

    /**
     * @return if report spam is checked by default in block/report dialog
     */
    boolean isDialogReportSpamCheckedByDefault();

    /**
     * @return percentage of after call notifications for spam numbers to show to the user
     */
    int percentOfSpamNotificationsToShow();

    /**
     * @return percentage of after call notifications for nonspam numbers to show to the user
     */
    int percentOfNonSpamNotificationsToShow();

    /**
     * Modifies spam blocking setting.
     *
     * @param enabled  Whether to enable or disable the setting.
     * @param listener The callback to be invoked after setting change is done.
     */
    void modifySpamBlockingSetting(boolean enabled, ModifySettingListener listener);

    /**
     * @return an intent to start spam blocking setting
     */
    Intent getSpamBlockingSettingIntent(Context context);

    /**
     * Callback to be invoked when setting change completes.
     */
    interface ModifySettingListener {

        /**
         * Called when setting change completes.
         */
        void onComplete(boolean success);
    }
}
