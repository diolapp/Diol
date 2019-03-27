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

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.VoicemailContract;
import android.support.annotation.VisibleForTesting;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import app.diol.R;
import app.diol.dialer.app.voicemail.VoicemailAudioManager;
import app.diol.dialer.app.voicemail.VoicemailErrorManager;
import app.diol.dialer.app.voicemail.VoicemailPlaybackPresenter;
import app.diol.dialer.common.FragmentUtils;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.util.PermissionsUtil;
import app.diol.dialer.voicemail.listui.error.VoicemailErrorMessageCreator;
import app.diol.dialer.voicemail.listui.error.VoicemailStatus;
import app.diol.dialer.voicemail.listui.error.VoicemailStatusWorker;
import app.diol.dialer.widget.EmptyContentView;

public class VisualVoicemailCallLogFragment extends CallLogFragment {

    private final ContentObserver voicemailStatusObserver = new CustomContentObserver();
    private VoicemailPlaybackPresenter voicemailPlaybackPresenter;
    private DialerExecutor<Context> preSyncVoicemailStatusCheckExecutor;

    private VoicemailErrorManager voicemailErrorManager;

    public VisualVoicemailCallLogFragment() {
        super(CallLog.Calls.VOICEMAIL_TYPE);
    }

    @Override
    protected VoicemailPlaybackPresenter getVoicemailPlaybackPresenter() {
        return voicemailPlaybackPresenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        voicemailPlaybackPresenter =
                VoicemailPlaybackPresenter.getInstance(getActivity(), savedInstanceState);
        if (PermissionsUtil.hasReadVoicemailPermissions(getContext())
                && PermissionsUtil.hasAddVoicemailPermissions(getContext())) {
            getActivity()
                    .getContentResolver()
                    .registerContentObserver(
                            VoicemailContract.Status.CONTENT_URI, true, voicemailStatusObserver);
        } else {
            LogUtil.w(
                    "VisualVoicemailCallLogFragment.onActivityCreated",
                    "read voicemail permission unavailable.");
        }
        super.onActivityCreated(savedInstanceState);

        preSyncVoicemailStatusCheckExecutor =
                DialerExecutorComponent.get(getContext())
                        .dialerExecutorFactory()
                        .createUiTaskBuilder(
                                getActivity().getFragmentManager(),
                                "fetchVoicemailStatus",
                                new VoicemailStatusWorker())
                        .onSuccess(this::onPreSyncVoicemailStatusChecked)
                        .build();

        voicemailErrorManager =
                new VoicemailErrorManager(getContext(), getAdapter().getAlertManager(), modalAlertManager);

        if (PermissionsUtil.hasReadVoicemailPermissions(getContext())
                && PermissionsUtil.hasAddVoicemailPermissions(getContext())) {
            getActivity()
                    .getContentResolver()
                    .registerContentObserver(
                            VoicemailContract.Status.CONTENT_URI,
                            true,
                            voicemailErrorManager.getContentObserver());
        } else {
            LogUtil.w(
                    "VisualVoicemailCallLogFragment.onActivityCreated",
                    "read voicemail permission unavailable.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.call_log_fragment, container, false);
        setupView(view);
        EmptyContentView emptyContentView = view.findViewById(R.id.empty_list_view);
        emptyContentView.setImage(R.drawable.quantum_ic_voicemail_vd_theme_24);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        voicemailPlaybackPresenter.onResume();
        voicemailErrorManager.onResume();
    }

    @Override
    public void onPause() {
        voicemailPlaybackPresenter.onPause();
        voicemailErrorManager.onPause();
        // Necessary to reset the speaker when leaving otherwise the platform will still remain in
        // speaker mode
        AudioManager audioManager = getContext().getSystemService(AudioManager.class);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (isAdded()) {
            getActivity()
                    .getContentResolver()
                    .unregisterContentObserver(voicemailErrorManager.getContentObserver());
            voicemailPlaybackPresenter.onDestroy();
            voicemailErrorManager.onDestroy();
            getActivity().getContentResolver().unregisterContentObserver(voicemailStatusObserver);
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (voicemailPlaybackPresenter != null) {
            voicemailPlaybackPresenter.onSaveInstanceState(outState);
        }
    }

    @Override
    public void fetchCalls() {
        super.fetchCalls();
        if (FragmentUtils.getParent(this, CallLogFragmentListener.class) != null) {
            FragmentUtils.getParentUnsafe(this, CallLogFragmentListener.class).updateTabUnreadCounts();
        }
    }

    @Override
    public void onVisible() {
        LogUtil.enterBlock("VisualVoicemailCallLogFragment.onVisible");
        super.onVisible();
        if (getActivity() != null && preSyncVoicemailStatusCheckExecutor != null) {
            preSyncVoicemailStatusCheckExecutor.executeParallel(getActivity());
            Logger.get(getActivity()).logImpression(DialerImpression.Type.VVM_TAB_VIEWED);
            getActivity().setVolumeControlStream(VoicemailAudioManager.PLAYBACK_STREAM);
        }
    }

    private void onPreSyncVoicemailStatusChecked(List<VoicemailStatus> statuses) {
        if (!shouldAutoSync(new VoicemailErrorMessageCreator(), statuses)) {
            return;
        }

        Intent intent = new Intent(VoicemailContract.ACTION_SYNC_VOICEMAIL);
        intent.setPackage(getActivity().getPackageName());
        getActivity().sendBroadcast(intent);
    }

    @VisibleForTesting
    boolean shouldAutoSync(
            VoicemailErrorMessageCreator errorMessageCreator, List<VoicemailStatus> statuses) {
        for (VoicemailStatus status : statuses) {
            if (!status.isActive(getContext())) {
                continue;
            }
            if (errorMessageCreator.isSyncBlockingError(status)) {
                LogUtil.i(
                        "VisualVoicemailCallLogFragment.shouldAutoSync", "auto-sync blocked due to " + status);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onNotVisible() {
        LogUtil.enterBlock("VisualVoicemailCallLogFragment.onNotVisible");
        super.onNotVisible();
        if (getActivity() != null) {
            getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            // onNotVisible will be called in the lock screen when the call ends
            if (!getActivity().getSystemService(KeyguardManager.class).inKeyguardRestrictedInputMode()) {
                LogUtil.i("VisualVoicemailCallLogFragment.onNotVisible", "clearing all new voicemails");
                CallLogNotificationsService.markAllNewVoicemailsAsOld(getActivity());
            }
        }
    }
}
