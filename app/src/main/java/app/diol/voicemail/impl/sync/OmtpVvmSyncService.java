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
package app.diol.voicemail.impl.sync;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Network;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.support.v4.os.BuildCompat;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.diol.dialer.logging.DialerImpression;
import app.diol.voicemail.VoicemailComponent;
import app.diol.voicemail.impl.ActivationTask;
import app.diol.voicemail.impl.Assert;
import app.diol.voicemail.impl.OmtpEvents;
import app.diol.voicemail.impl.OmtpVvmCarrierConfigHelper;
import app.diol.voicemail.impl.Voicemail;
import app.diol.voicemail.impl.VoicemailStatus;
import app.diol.voicemail.impl.VvmLog;
import app.diol.voicemail.impl.fetch.VoicemailFetchedCallback;
import app.diol.voicemail.impl.imap.ImapHelper;
import app.diol.voicemail.impl.imap.ImapHelper.InitializingException;
import app.diol.voicemail.impl.mail.store.ImapFolder.Quota;
import app.diol.voicemail.impl.scheduling.BaseTask;
import app.diol.voicemail.impl.settings.VisualVoicemailSettingsUtil;
import app.diol.voicemail.impl.sync.VvmNetworkRequest.NetworkWrapper;
import app.diol.voicemail.impl.sync.VvmNetworkRequest.RequestFailedException;
import app.diol.voicemail.impl.utils.LoggerUtils;
import app.diol.voicemail.impl.utils.VoicemailDatabaseUtil;

/**
 * Sync OMTP visual voicemail.
 */
@TargetApi(VERSION_CODES.O)
public class OmtpVvmSyncService {

    private static final String TAG = "OmtpVvmSyncService";

    /**
     * Threshold for whether we should archive and delete voicemails from the remote
     * VM server.
     */
    private static final float AUTO_DELETE_ARCHIVE_VM_THRESHOLD = 0.75f;

    private final Context context;
    private final VoicemailsQueryHelper queryHelper;

    public OmtpVvmSyncService(Context context) {
        this.context = context;
        queryHelper = new VoicemailsQueryHelper(this.context);
    }

    private static boolean isArchiveAllowedAndEnabled(Context context, PhoneAccountHandle phoneAccountHandle) {

        if (!VoicemailComponent.get(context).getVoicemailClient().isVoicemailArchiveAvailable(context)) {
            VvmLog.i("isArchiveAllowedAndEnabled", "voicemail archive is not available");
            return false;
        }
        if (!VisualVoicemailSettingsUtil.isArchiveEnabled(context, phoneAccountHandle)) {
            VvmLog.i("isArchiveAllowedAndEnabled", "voicemail archive is turned off");
            return false;
        }
        if (!VisualVoicemailSettingsUtil.isEnabled(context, phoneAccountHandle)) {
            VvmLog.i("isArchiveAllowedAndEnabled", "voicemail is turned off");
            return false;
        }
        return true;
    }

    public void sync(BaseTask task, PhoneAccountHandle phoneAccount, Voicemail voicemail, VoicemailStatus.Editor status) {
        Assert.isTrue(phoneAccount != null);
        VvmLog.v(TAG, "Sync requested for account: " + phoneAccount);
        setupAndSendRequest(task, phoneAccount, voicemail, status);
    }

    private void setupAndSendRequest(BaseTask task, PhoneAccountHandle phoneAccount, Voicemail voicemail,
                                     VoicemailStatus.Editor status) {
        if (!VisualVoicemailSettingsUtil.isEnabled(context, phoneAccount)) {
            VvmLog.e(TAG, "Sync requested for disabled account");
            return;
        }
        if (!VvmAccountManager.isAccountActivated(context, phoneAccount)) {
            ActivationTask.start(context, phoneAccount, null);
            return;
        }

        OmtpVvmCarrierConfigHelper config = new OmtpVvmCarrierConfigHelper(context, phoneAccount);
        LoggerUtils.logImpressionOnMainThread(context, DialerImpression.Type.VVM_SYNC_STARTED);
        // DATA_IMAP_OPERATION_STARTED posting should not be deferred. This event clears
        // all data
        // channel errors, which should happen when the task starts, not when it ends.
        // It is the
        // "Sync in progress..." status, which is currently displayed to the user as no
        // error.
        config.handleEvent(VoicemailStatus.edit(context, phoneAccount), OmtpEvents.DATA_IMAP_OPERATION_STARTED);
        try (NetworkWrapper network = VvmNetworkRequest.getNetwork(config, phoneAccount, status)) {
            if (network == null) {
                VvmLog.e(TAG, "unable to acquire network");
                task.fail();
                return;
            }
            doSync(task, network.get(), phoneAccount, voicemail, status);
        } catch (RequestFailedException e) {
            config.handleEvent(status, OmtpEvents.DATA_NO_CONNECTION_CELLULAR_REQUIRED);
            task.fail();
        }
    }

    private void doSync(BaseTask task, Network network, PhoneAccountHandle phoneAccount, Voicemail voicemail,
                        VoicemailStatus.Editor status) {
        try (ImapHelper imapHelper = new ImapHelper(context, phoneAccount, network, status)) {
            boolean success;
            if (voicemail == null) {
                success = syncAll(imapHelper, phoneAccount);
            } else {
                success = downloadOneVoicemail(imapHelper, voicemail, phoneAccount);
            }
            if (success) {
                // TODO: a bug failure should interrupt all subsequent task via exceptions
                imapHelper.updateQuota();
                autoDeleteAndArchiveVM(imapHelper, phoneAccount);
                imapHelper.handleEvent(OmtpEvents.DATA_IMAP_OPERATION_COMPLETED);
                LoggerUtils.logImpressionOnMainThread(context, DialerImpression.Type.VVM_SYNC_COMPLETED);
            } else {
                task.fail();
            }
        } catch (InitializingException e) {
            VvmLog.w(TAG, "Can't retrieve Imap credentials.", e);
            return;
        }
    }

    /**
     * If the VM quota exceeds {@value AUTO_DELETE_ARCHIVE_VM_THRESHOLD}, we should
     * archive the VMs and delete them from the server to ensure new VMs can be
     * received.
     */
    private void autoDeleteAndArchiveVM(ImapHelper imapHelper, PhoneAccountHandle phoneAccountHandle) {
        if (!isArchiveAllowedAndEnabled(context, phoneAccountHandle)) {
            VvmLog.i(TAG, "autoDeleteAndArchiveVM is turned off");
            LoggerUtils.logImpressionOnMainThread(context, DialerImpression.Type.VVM_ARCHIVE_AUTO_DELETE_TURNED_OFF);
            return;
        }
        Quota quotaOnServer = imapHelper.getQuota();
        if (quotaOnServer == null) {
            LoggerUtils.logImpressionOnMainThread(context,
                    DialerImpression.Type.VVM_ARCHIVE_AUTO_DELETE_FAILED_DUE_TO_FAILED_QUOTA_CHECK);
            VvmLog.e(TAG, "autoDeleteAndArchiveVM failed - Can't retrieve Imap quota.");
            return;
        }

        if ((float) quotaOnServer.occupied / (float) quotaOnServer.total > AUTO_DELETE_ARCHIVE_VM_THRESHOLD) {
            deleteAndArchiveVM(imapHelper, quotaOnServer);
            imapHelper.updateQuota();
            LoggerUtils.logImpressionOnMainThread(context, DialerImpression.Type.VVM_ARCHIVE_AUTO_DELETED_VM_FROM_SERVER);
        } else {
            VvmLog.i(TAG, "no need to archive and auto delete VM, quota below threshold");
        }
    }

    private void deleteAndArchiveVM(ImapHelper imapHelper, Quota quotaOnServer) {
        // Archive column should only be used for 0 and above
        Assert.isTrue(BuildCompat.isAtLeastO());

        // The number of voicemails that exceed our threshold and should be deleted from
        // the server
        int numVoicemails = quotaOnServer.occupied - (int) (AUTO_DELETE_ARCHIVE_VM_THRESHOLD * quotaOnServer.total);
        List<Voicemail> oldestVoicemails = queryHelper.oldestVoicemailsOnServer(numVoicemails);
        VvmLog.w(TAG, "number of voicemails to delete " + numVoicemails);
        if (!oldestVoicemails.isEmpty()) {
            queryHelper.markArchivedInDatabase(oldestVoicemails);
            imapHelper.markMessagesAsDeleted(oldestVoicemails);
            VvmLog.i(TAG, String.format("successfully archived and deleted %d voicemails", oldestVoicemails.size()));
        } else {
            VvmLog.w(TAG, "remote voicemail server is empty");
        }
    }

    private boolean syncAll(ImapHelper imapHelper, PhoneAccountHandle account) {

        List<Voicemail> serverVoicemails = imapHelper.fetchAllVoicemails();
        List<Voicemail> localVoicemails = queryHelper.getAllVoicemails(account);
        List<Voicemail> deletedVoicemails = queryHelper.getDeletedVoicemails(account);
        boolean succeeded = true;

        if (localVoicemails == null || serverVoicemails == null) {
            // Null value means the query failed.
            VvmLog.e(TAG, "syncAll: query failed");
            return false;
        }

        if (deletedVoicemails.size() > 0) {
            if (imapHelper.markMessagesAsDeleted(deletedVoicemails)) {
                // Delete only the voicemails that was deleted on the server, in case more are
                // deleted
                // since the IMAP query was completed.
                queryHelper.deleteFromDatabase(deletedVoicemails);
            } else {
                succeeded = false;
            }
        }

        Map<String, Voicemail> remoteMap = buildMap(serverVoicemails);

        List<Voicemail> localReadVoicemails = new ArrayList<>();

        // Go through all the local voicemails and check if they are on the server.
        // They may be read or deleted on the server but not locally. Perform the
        // appropriate local operation if the status differs from the server. Remove
        // the messages that exist both locally and on the server to know which server
        // messages to insert locally.
        // Voicemails that were removed automatically from the server, are marked as
        // archived and are stored locally. We do not delete them, as they were removed
        // from the server
        // by design (to make space).
        for (int i = 0; i < localVoicemails.size(); i++) {
            Voicemail localVoicemail = localVoicemails.get(i);
            Voicemail remoteVoicemail = remoteMap.remove(localVoicemail.getSourceData());

            // Do not delete voicemails that are archived marked as archived.
            if (remoteVoicemail == null) {
                queryHelper.deleteNonArchivedFromDatabase(localVoicemail);
            } else {
                if (remoteVoicemail.isRead() && !localVoicemail.isRead()) {
                    queryHelper.markReadInDatabase(localVoicemail);
                } else if (localVoicemail.isRead() && !remoteVoicemail.isRead()) {
                    localReadVoicemails.add(localVoicemail);
                }

                if (!TextUtils.isEmpty(remoteVoicemail.getTranscription())
                        && TextUtils.isEmpty(localVoicemail.getTranscription())) {
                    LoggerUtils.logImpressionOnMainThread(context, DialerImpression.Type.VVM_TRANSCRIPTION_DOWNLOADED);
                    queryHelper.updateWithTranscription(localVoicemail, remoteVoicemail.getTranscription());
                }
            }
        }

        if (localReadVoicemails.size() > 0) {
            VvmLog.i(TAG, "Marking voicemails as read");
            if (imapHelper.markMessagesAsRead(localReadVoicemails)) {
                VvmLog.i(TAG, "Marking voicemails as clean");
                queryHelper.markCleanInDatabase(localReadVoicemails);
            } else {
                return false;
            }
        }

        // The leftover messages are messages that exist on the server but not locally.
        boolean prefetchEnabled = shouldPerformPrefetch(account, imapHelper);
        for (Voicemail remoteVoicemail : remoteMap.values()) {
            if (!TextUtils.isEmpty(remoteVoicemail.getTranscription())) {
                LoggerUtils.logImpressionOnMainThread(context, DialerImpression.Type.VVM_TRANSCRIPTION_DOWNLOADED);
            }
            Uri uri = VoicemailDatabaseUtil.insert(context, remoteVoicemail);
            if (prefetchEnabled) {
                VoicemailFetchedCallback fetchedCallback = new VoicemailFetchedCallback(context, uri, account);
                imapHelper.fetchVoicemailPayload(fetchedCallback, remoteVoicemail.getSourceData());
            }
        }

        return succeeded;
    }

    private boolean downloadOneVoicemail(ImapHelper imapHelper, Voicemail voicemail, PhoneAccountHandle account) {
        if (shouldPerformPrefetch(account, imapHelper)) {
            VoicemailFetchedCallback callback = new VoicemailFetchedCallback(context, voicemail.getUri(), account);
            imapHelper.fetchVoicemailPayload(callback, voicemail.getSourceData());
        }

        return imapHelper.fetchTranscription(new TranscriptionFetchedCallback(context, voicemail),
                voicemail.getSourceData());
    }

    private boolean shouldPerformPrefetch(PhoneAccountHandle account, ImapHelper imapHelper) {
        OmtpVvmCarrierConfigHelper carrierConfigHelper = new OmtpVvmCarrierConfigHelper(context, account);
        return carrierConfigHelper.isPrefetchEnabled() && !imapHelper.isRoaming();
    }

    /**
     * Builds a map from provider data to message for the given collection of
     * voicemails.
     */
    private Map<String, Voicemail> buildMap(List<Voicemail> messages) {
        Map<String, Voicemail> map = new ArrayMap<String, Voicemail>();
        for (Voicemail message : messages) {
            map.put(message.getSourceData(), message);
        }
        return map;
    }

    /**
     * Callback for
     * {@link ImapHelper#fetchTranscription(TranscriptionFetchedCallback, String)}
     */
    public static class TranscriptionFetchedCallback {

        private Context context;
        private Voicemail voicemail;

        public TranscriptionFetchedCallback(Context context, Voicemail voicemail) {
            this.context = context;
            this.voicemail = voicemail;
        }

        public void setVoicemailTranscription(String transcription) {
            VoicemailsQueryHelper queryHelper = new VoicemailsQueryHelper(context);
            queryHelper.updateWithTranscription(voicemail, transcription);
        }
    }
}
