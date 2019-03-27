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

package app.diol.dialer.voicemailstatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import app.diol.dialer.database.CallLogQueryHandler;

/**
 * Helper class to check whether visual voicemail is enabled.
 *
 * <p>Call isVisualVoicemailEnabled() to retrieve the result.
 *
 * <p>The result is cached and saved in a SharedPreferences, stored as a boolean in
 * PREF_KEY_HAS_ACTIVE_VOICEMAIL_PROVIDER. Every time a new instance is created, it will try to
 * restore the cached result from the SharedPreferences.
 *
 * <p>Call asyncUpdate() to make a CallLogQuery to check the actual status. This is a async call so
 * isVisualVoicemailEnabled() will not be affected immediately.
 *
 * <p>If the status has changed as a result of asyncUpdate(),
 * Callback.onVisualVoicemailEnabledStatusChanged() will be called with the new value.
 */
public class VisualVoicemailEnabledChecker implements CallLogQueryHandler.Listener {

    public static final String PREF_KEY_HAS_ACTIVE_VOICEMAIL_PROVIDER =
            "has_active_voicemail_provider";
    private SharedPreferences prefs;
    private boolean hasActiveVoicemailProvider;
    private CallLogQueryHandler callLogQueryHandler;
    private Context context;
    private Callback callback;

    public VisualVoicemailEnabledChecker(Context context, @Nullable Callback callback) {
        this.context = context;
        this.callback = callback;
        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        hasActiveVoicemailProvider = prefs.getBoolean(PREF_KEY_HAS_ACTIVE_VOICEMAIL_PROVIDER, false);
    }

    /**
     * @return whether visual voicemail is enabled. Result is cached, call asyncUpdate() to update the
     * result.
     */
    public boolean isVisualVoicemailEnabled() {
        return hasActiveVoicemailProvider;
    }

    /**
     * Perform an async query into the system to check the status of visual voicemail. If the status
     * has changed, Callback.onVisualVoicemailEnabledStatusChanged() will be called.
     */
    public void asyncUpdate() {
        callLogQueryHandler = new CallLogQueryHandler(context, context.getContentResolver(), this);
        callLogQueryHandler.fetchVoicemailStatus();
    }

    @Override
    public void onVoicemailStatusFetched(Cursor statusCursor) {
        boolean hasActiveVoicemailProvider =
                VoicemailStatusHelper.getNumberActivityVoicemailSources(statusCursor) > 0;
        if (hasActiveVoicemailProvider != this.hasActiveVoicemailProvider) {
            this.hasActiveVoicemailProvider = hasActiveVoicemailProvider;
            prefs
                    .edit()
                    .putBoolean(PREF_KEY_HAS_ACTIVE_VOICEMAIL_PROVIDER, this.hasActiveVoicemailProvider)
                    .apply();
            if (callback != null) {
                callback.onVisualVoicemailEnabledStatusChanged(this.hasActiveVoicemailProvider);
            }
        }
    }

    @Override
    public void onVoicemailUnreadCountFetched(Cursor cursor) {
        // Do nothing
    }

    @Override
    public void onMissedCallsUnreadCountFetched(Cursor cursor) {
        // Do nothing
    }

    @Override
    public boolean onCallsFetched(Cursor combinedCursor) {
        // Do nothing
        return false;
    }

    public interface Callback {

        /**
         * Callback to notify enabled status has changed to the @param newValue
         */
        void onVisualVoicemailEnabledStatusChanged(boolean newValue);
    }
}
