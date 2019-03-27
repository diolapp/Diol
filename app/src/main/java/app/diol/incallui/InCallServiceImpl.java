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

package app.diol.incallui;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Trace;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;

import app.diol.dialer.blocking.FilteredNumberAsyncQueryHandler;
import app.diol.dialer.feedback.FeedbackComponent;
import app.diol.incallui.audiomode.AudioModeProvider;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.ExternalCallList;
import app.diol.incallui.call.TelecomAdapter;
import app.diol.incallui.speakeasy.SpeakEasyCallManager;
import app.diol.incallui.speakeasy.SpeakEasyComponent;

/**
 * Used to receive updates about calls from the Telecom component. This service is bound to Telecom
 * while there exist calls which potentially require UI. This includes ringing (incoming), dialing
 * (outgoing), and active calls. When the last call is disconnected, Telecom will unbind to the
 * service triggering InCallActivity (via CallList) to finish soon after.
 */
public class InCallServiceImpl extends InCallService {

    private ReturnToCallController returnToCallController;
    private CallList.Listener feedbackListener;
    // We only expect there to be one speakEasyCallManager to be instantiated at a time.
    // We did not use a singleton SpeakEasyCallManager to avoid holding on to state beyond the
    // lifecycle of this service, because the singleton is associated with the state of the
    // Application, not this service.
    private SpeakEasyCallManager speakEasyCallManager;

    @Override
    public void onCallAudioStateChanged(CallAudioState audioState) {
        Trace.beginSection("InCallServiceImpl.onCallAudioStateChanged");
        AudioModeProvider.getInstance().onAudioStateChanged(audioState);
        Trace.endSection();
    }

    @Override
    public void onBringToForeground(boolean showDialpad) {
        Trace.beginSection("InCallServiceImpl.onBringToForeground");
        InCallPresenter.getInstance().onBringToForeground(showDialpad);
        Trace.endSection();
    }

    @Override
    public void onCallAdded(Call call) {
        Trace.beginSection("InCallServiceImpl.onCallAdded");
        InCallPresenter.getInstance().onCallAdded(call);
        Trace.endSection();
    }

    @Override
    public void onCallRemoved(Call call) {
        Trace.beginSection("InCallServiceImpl.onCallRemoved");
        speakEasyCallManager.onCallRemoved(CallList.getInstance().getDialerCallFromTelecomCall(call));

        InCallPresenter.getInstance().onCallRemoved(call);
        Trace.endSection();
    }

    @Override
    public void onCanAddCallChanged(boolean canAddCall) {
        Trace.beginSection("InCallServiceImpl.onCanAddCallChanged");
        InCallPresenter.getInstance().onCanAddCallChanged(canAddCall);
        Trace.endSection();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.speakEasyCallManager = SpeakEasyComponent.get(this).speakEasyCallManager();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Trace.beginSection("InCallServiceImpl.onBind");
        final Context context = getApplicationContext();
        final ContactInfoCache contactInfoCache = ContactInfoCache.getInstance(context);
        AudioModeProvider.getInstance().initializeAudioState(this);
        InCallPresenter.getInstance()
                .setUp(
                        context,
                        CallList.getInstance(),
                        new ExternalCallList(),
                        new StatusBarNotifier(context, contactInfoCache),
                        new ExternalCallNotifier(context, contactInfoCache),
                        contactInfoCache,
                        new ProximitySensor(
                                context, AudioModeProvider.getInstance(), new AccelerometerListener(context)),
                        new FilteredNumberAsyncQueryHandler(context),
                        speakEasyCallManager);
        InCallPresenter.getInstance().onServiceBind();
        InCallPresenter.getInstance().maybeStartRevealAnimation(intent);
        TelecomAdapter.getInstance().setInCallService(this);
        returnToCallController =
                new ReturnToCallController(this, ContactInfoCache.getInstance(context));
        feedbackListener = FeedbackComponent.get(context).getCallFeedbackListener();
        CallList.getInstance().addListener(feedbackListener);

        IBinder iBinder = super.onBind(intent);
        Trace.endSection();
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Trace.beginSection("InCallServiceImpl.onUnbind");
        super.onUnbind(intent);

        InCallPresenter.getInstance().onServiceUnbind();
        tearDown();

        Trace.endSection();
        return false;
    }

    private void tearDown() {
        Trace.beginSection("InCallServiceImpl.tearDown");
        Log.v(this, "tearDown");
        // Tear down the InCall system
        InCallPresenter.getInstance().tearDown();
        TelecomAdapter.getInstance().clearInCallService();
        if (returnToCallController != null) {
            returnToCallController.tearDown();
            returnToCallController = null;
        }
        if (feedbackListener != null) {
            CallList.getInstance().removeListener(feedbackListener);
            feedbackListener = null;
        }
        Trace.endSection();
    }
}
