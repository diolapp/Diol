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

package app.diol.dialer.app.voicemail;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.diol.dialer.app.calllog.CallLogAlertManager;
import app.diol.dialer.app.calllog.CallLogModalAlertManager;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.database.CallLogQueryHandler;
import app.diol.dialer.voicemail.listui.error.VoicemailErrorAlert;
import app.diol.dialer.voicemail.listui.error.VoicemailErrorMessageCreator;
import app.diol.dialer.voicemail.listui.error.VoicemailStatus;
import app.diol.dialer.voicemail.listui.error.VoicemailStatusReader;
import app.diol.voicemail.VoicemailComponent;

/**
 * Fetches voicemail status and generate {@link VoicemailStatus} for {@link VoicemailErrorAlert} to
 * show.
 */
public class VoicemailErrorManager implements CallLogQueryHandler.Listener, VoicemailStatusReader {

    private final Context context;
    private final CallLogQueryHandler callLogQueryHandler;
    private final VoicemailErrorAlert alertItem;

    private final Map<PhoneAccountHandle, ServiceStateListener> listeners = new ArrayMap<>();
    private boolean isForeground;
    private boolean statusInvalidated;
    private final ContentObserver statusObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    fetchStatus();
                }
            };

    public VoicemailErrorManager(
            Context context,
            CallLogAlertManager alertManager,
            CallLogModalAlertManager modalAlertManager) {
        this.context = context;
        alertItem =
                new VoicemailErrorAlert(
                        context, alertManager, modalAlertManager, new VoicemailErrorMessageCreator());
        callLogQueryHandler = new CallLogQueryHandler(context, context.getContentResolver(), this);
        fetchStatus();
    }

    public ContentObserver getContentObserver() {
        return statusObserver;
    }

    @MainThread
    @Override
    public void onVoicemailStatusFetched(Cursor statusCursor) {
        List<VoicemailStatus> statuses = new ArrayList<>();
        while (statusCursor.moveToNext()) {
            VoicemailStatus status = new VoicemailStatus(context, statusCursor);
            if (status.isActive(context)) {
                statuses.add(status);
                addServiceStateListener(status);
            } else {
                LogUtil.i("VisualVoicemailCallLogFragment.shouldAutoSync", "inactive source ignored");
            }
        }
        alertItem.updateStatus(statuses, this);
        // TODO(twyen): a bug support error from multiple sources.
        return;
    }

    @MainThread
    private void addServiceStateListener(VoicemailStatus status) {
        Assert.isMainThread();
        if (!VoicemailComponent.get(context).getVoicemailClient().isVoicemailModuleEnabled()) {
            LogUtil.i("VoicemailErrorManager.addServiceStateListener", "VVM module not enabled");
            return;
        }
        if (!status.sourcePackage.equals(context.getPackageName())) {
            LogUtil.i("VoicemailErrorManager.addServiceStateListener", "non-dialer source");
            return;
        }
        TelephonyManager telephonyManager =
                context
                        .getSystemService(TelephonyManager.class)
                        .createForPhoneAccountHandle(status.getPhoneAccountHandle());
        if (telephonyManager == null) {
            LogUtil.e("VoicemailErrorManager.addServiceStateListener", "invalid PhoneAccountHandle");
            return;
        }
        PhoneAccountHandle phoneAccountHandle = status.getPhoneAccountHandle();
        if (listeners.containsKey(phoneAccountHandle)) {
            return;
        }
        LogUtil.i(
                "VoicemailErrorManager.addServiceStateListener",
                "adding listener for " + phoneAccountHandle);
        ServiceStateListener serviceStateListener = new ServiceStateListener();
        telephonyManager.listen(serviceStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        listeners.put(phoneAccountHandle, serviceStateListener);
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

    public void onResume() {
        isForeground = true;
        if (statusInvalidated) {
            fetchStatus();
        }
    }

    public void onPause() {
        isForeground = false;
        statusInvalidated = false;
    }

    public void onDestroy() {
        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class);
        for (ServiceStateListener listener : listeners.values()) {
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
        }
    }

    @Override
    public void refresh() {
        fetchStatus();
    }

    /**
     * Fetch the status when the dialer is in foreground, or queue a fetch when the dialer resumes.
     */
    private void fetchStatus() {
        if (!isForeground) {
            // Dialer is in the background, UI should not be updated. Reload the status when it resumes.
            statusInvalidated = true;
            return;
        }
        callLogQueryHandler.fetchVoicemailStatus();
    }

    private class ServiceStateListener extends PhoneStateListener {

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            fetchStatus();
        }
    }
}
