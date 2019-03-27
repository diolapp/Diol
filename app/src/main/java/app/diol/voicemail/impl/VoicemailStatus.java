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

package app.diol.voicemail.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.VoicemailContract;
import android.provider.VoicemailContract.Status;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

import app.diol.dialer.strictmode.StrictModeUtils;

public class VoicemailStatus {

    private static final String TAG = "VvmStatus";

    public static Editor edit(Context context, PhoneAccountHandle phoneAccountHandle) {
        return new Editor(context, phoneAccountHandle);
    }

    /**
     * Reset the status to the "disabled" state, which the UI should not show
     * anything for this phoneAccountHandle.
     */
    public static void disable(Context context, PhoneAccountHandle phoneAccountHandle) {
        edit(context, phoneAccountHandle).setConfigurationState(Status.CONFIGURATION_STATE_NOT_CONFIGURED)
                .setDataChannelState(Status.DATA_CHANNEL_STATE_NO_CONNECTION)
                .setNotificationChannelState(Status.NOTIFICATION_CHANNEL_STATE_NO_CONNECTION).apply();
    }

    public static DeferredEditor deferredEdit(Context context, PhoneAccountHandle phoneAccountHandle) {
        return new DeferredEditor(context, phoneAccountHandle);
    }

    public static class Editor {

        private final Context context;
        @Nullable
        private final PhoneAccountHandle phoneAccountHandle;

        private ContentValues values = new ContentValues();

        private Editor(Context context, PhoneAccountHandle phoneAccountHandle) {
            this.context = context;
            this.phoneAccountHandle = phoneAccountHandle;
            if (this.phoneAccountHandle == null) {
                VvmLog.w(TAG, "VoicemailStatus.Editor created with null phone account, status will" + " not be written");
            }
        }

        @Nullable
        public PhoneAccountHandle getPhoneAccountHandle() {
            return phoneAccountHandle;
        }

        public Editor setType(String type) {
            values.put(Status.SOURCE_TYPE, type);
            return this;
        }

        public Editor setConfigurationState(int configurationState) {
            values.put(Status.CONFIGURATION_STATE, configurationState);
            return this;
        }

        public Editor setDataChannelState(int dataChannelState) {
            values.put(Status.DATA_CHANNEL_STATE, dataChannelState);
            return this;
        }

        public Editor setNotificationChannelState(int notificationChannelState) {
            values.put(Status.NOTIFICATION_CHANNEL_STATE, notificationChannelState);
            return this;
        }

        public Editor setQuota(int occupied, int total) {
            if (occupied == VoicemailContract.Status.QUOTA_UNAVAILABLE
                    && total == VoicemailContract.Status.QUOTA_UNAVAILABLE) {
                return this;
            }

            values.put(Status.QUOTA_OCCUPIED, occupied);
            values.put(Status.QUOTA_TOTAL, total);
            return this;
        }

        /**
         * Apply the changes to the {@link VoicemailStatus} {@link #Editor}.
         *
         * @return {@code true} if the changes were successfully applied, {@code false}
         * otherwise.
         */
        public boolean apply() {
            if (phoneAccountHandle == null) {
                return false;
            }
            values.put(Status.PHONE_ACCOUNT_COMPONENT_NAME, phoneAccountHandle.getComponentName().flattenToString());
            values.put(Status.PHONE_ACCOUNT_ID, phoneAccountHandle.getId());
            ContentResolver contentResolver = context.getContentResolver();
            Uri statusUri = VoicemailContract.Status.buildSourceUri(context.getPackageName());
            try {
                StrictModeUtils.bypass(() -> contentResolver.insert(statusUri, values));
            } catch (IllegalArgumentException iae) {
                VvmLog.e(TAG, "apply :: failed to insert content resolver ", iae);
                values.clear();
                return false;
            }
            values.clear();
            return true;
        }

        public ContentValues getValues() {
            return values;
        }
    }

    /**
     * A voicemail status editor that the decision of whether to actually write to
     * the database can be deferred. This object will be passed around as a usual
     * {@link Editor}, but {@link #apply()} doesn't do anything. If later the
     * creator of this object decides any status changes written to it should be
     * committed, {@link #deferredApply()} should be called.
     */
    public static class DeferredEditor extends Editor {

        private DeferredEditor(Context context, PhoneAccountHandle phoneAccountHandle) {
            super(context, phoneAccountHandle);
        }

        @Override
        public boolean apply() {
            // Do nothing
            return true;
        }

        public void deferredApply() {
            super.apply();
        }
    }
}
