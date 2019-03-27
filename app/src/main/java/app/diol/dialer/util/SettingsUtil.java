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

package app.diol.dialer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import app.diol.R;

public class SettingsUtil {

    private static final String DEFAULT_NOTIFICATION_URI_STRING =
            Settings.System.DEFAULT_NOTIFICATION_URI.toString();

    /**
     * Queries for a ringtone name, and sets the name using a handler. This is a method was originally
     * copied from com.android.settings.SoundSettings.
     *
     * @param context The application context.
     * @param handler The handler, which takes the name of the ringtone as a String as a parameter.
     * @param type    The type of sound.
     * @param key     The key to the shared preferences entry being updated.
     * @param msg     An integer identifying the message sent to the handler.
     */
    public static void updateRingtoneName(
            Context context, Handler handler, int type, String key, int msg) {
        final Uri ringtoneUri;
        boolean defaultRingtone = false;
        if (type == RingtoneManager.TYPE_RINGTONE) {
            // For ringtones, we can just lookup the system default because changing the settings
            // in Call Settings changes the system default.
            ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
        } else {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            // For voicemail notifications, we use the value saved in Phone's shared preferences.
            String uriString = prefs.getString(key, DEFAULT_NOTIFICATION_URI_STRING);
            if (TextUtils.isEmpty(uriString)) {
                // silent ringtone
                ringtoneUri = null;
            } else {
                if (uriString.equals(DEFAULT_NOTIFICATION_URI_STRING)) {
                    // If it turns out that the voicemail notification is set to the system
                    // default notification, we retrieve the actual URI to prevent it from showing
                    // up as "Unknown Ringtone".
                    defaultRingtone = true;
                    ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
                } else {
                    ringtoneUri = Uri.parse(uriString);
                }
            }
        }
        getRingtoneName(context, handler, ringtoneUri, msg, defaultRingtone);
    }

    public static void getRingtoneName(Context context, Handler handler, Uri ringtoneUri, int msg) {
        getRingtoneName(context, handler, ringtoneUri, msg, false);
    }

    public static void getRingtoneName(
            Context context, Handler handler, Uri ringtoneUri, int msg, boolean defaultRingtone) {
        CharSequence summary = context.getString(R.string.ringtone_unknown);
        // Is it a silent ringtone?
        if (ringtoneUri == null) {
            summary = context.getString(R.string.ringtone_silent);
        } else {
            // Fetch the ringtone title from the media provider
            final Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
            if (ringtone != null) {
                try {
                    final String title = ringtone.getTitle(context);
                    if (!TextUtils.isEmpty(title)) {
                        summary = title;
                    }
                } catch (SQLiteException sqle) {
                    // Unknown title for the ringtone
                }
            }
        }
        if (defaultRingtone) {
            summary = context.getString(R.string.default_notification_description, summary);
        }
        handler.sendMessage(handler.obtainMessage(msg, summary));
    }
}
