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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.telecom.CallAudioState;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import app.diol.R;
import app.diol.bubble.Bubble;
import app.diol.bubble.BubbleComponent;
import app.diol.bubble.BubbleInfo;
import app.diol.bubble.BubbleInfo.Action;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.contacts.ContactsComponent;
import app.diol.dialer.lettertile.LetterTileDrawable;
import app.diol.dialer.telecom.TelecomUtil;
import app.diol.dialer.theme.base.ThemeComponent;
import app.diol.incallui.ContactInfoCache.ContactCacheEntry;
import app.diol.incallui.ContactInfoCache.ContactInfoCacheCallback;
import app.diol.incallui.InCallPresenter.InCallState;
import app.diol.incallui.InCallPresenter.InCallUiListener;
import app.diol.incallui.audiomode.AudioModeProvider;
import app.diol.incallui.audiomode.AudioModeProvider.AudioModeListener;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.CallList.Listener;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.speakerbuttonlogic.SpeakerButtonInfo;

/**
 * Listens for events relevant to the return-to-call bubble and updates the bubble's state as
 * necessary.
 *
 * <p>Bubble shows when one of following happens: 1. a new outgoing/ongoing call appears 2. leave
 * in-call UI with an outgoing/ongoing call
 *
 * <p>Bubble hides when one of following happens: 1. a call disconnect and there is no more
 * outgoing/ongoing call 2. show in-call UI
 */
public class ReturnToCallController implements InCallUiListener, Listener, AudioModeListener {

    private static Boolean canShowBubblesForTesting = null;
    private final Context context;
    private final PendingIntent toggleSpeaker;
    private final PendingIntent showSpeakerSelect;
    private final PendingIntent toggleMute;
    private final PendingIntent endCall;
    private final PendingIntent fullScreen;
    private final ContactInfoCache contactInfoCache;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    Bubble bubble;
    private CallAudioState audioState;
    private InCallState inCallState;

    public ReturnToCallController(Context context, ContactInfoCache contactInfoCache) {
        this.context = context;
        this.contactInfoCache = contactInfoCache;

        toggleSpeaker = createActionIntent(ReturnToCallActionReceiver.ACTION_TOGGLE_SPEAKER);
        showSpeakerSelect =
                createActionIntent(ReturnToCallActionReceiver.ACTION_SHOW_AUDIO_ROUTE_SELECTOR);
        toggleMute = createActionIntent(ReturnToCallActionReceiver.ACTION_TOGGLE_MUTE);
        endCall = createActionIntent(ReturnToCallActionReceiver.ACTION_END_CALL);
        fullScreen = createActionIntent(ReturnToCallActionReceiver.ACTION_RETURN_TO_CALL);

        AudioModeProvider.getInstance().addListener(this);
        audioState = AudioModeProvider.getInstance().getAudioState();
        InCallPresenter.getInstance().addInCallUiListener(this);
        CallList.getInstance().addListener(this);
    }

    public static boolean isEnabled(Context context) {
        return ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getBoolean("enable_return_to_call_bubble_v2", false);
    }

    /**
     * Determines whether bubbles can be shown based on permissions obtained. This should be checked
     * before attempting to create a Bubble.
     *
     * @return true iff bubbles are able to be shown.
     * @see Settings#canDrawOverlays(Context)
     */
    private static boolean canShowBubbles(@NonNull Context context) {
        return canShowBubblesForTesting != null
                ? canShowBubblesForTesting
                : Settings.canDrawOverlays(context);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    static void setCanShowBubblesForTesting(boolean canShowBubbles) {
        canShowBubblesForTesting = canShowBubbles;
    }

    public void tearDown() {
        hide();
        InCallPresenter.getInstance().removeInCallUiListener(this);
        CallList.getInstance().removeListener(this);
        AudioModeProvider.getInstance().removeListener(this);
    }

    @Override
    public void onUiShowing(boolean showing) {
        if (!isEnabled(context)) {
            hide();
            return;
        }

        LogUtil.i("ReturnToCallController.onUiShowing", "showing: " + showing);
        if (showing) {
            LogUtil.i("ReturnToCallController.onUiShowing", "going to hide");
            hide();
        } else {
            if (getCall() != null) {
                LogUtil.i("ReturnToCallController.onUiShowing", "going to show");
                show();
            }
        }
    }

    private void hide() {
        if (bubble != null) {
            bubble.hide();
        } else {
            LogUtil.i("ReturnToCallController.hide", "hide() called without calling show()");
        }
    }

    private void show() {
        if (bubble == null) {
            bubble = startBubble();
        } else {
            bubble.show();
        }
        startContactInfoSearch();
    }

    private Bubble startBubble() {
        if (!canShowBubbles(context)) {
            LogUtil.i("ReturnToCallController.startBubble", "can't show bubble, no permission");
            return null;
        }
        Bubble returnToCallBubble = BubbleComponent.get(context).getBubble();
        returnToCallBubble.setBubbleInfo(generateBubbleInfo());
        returnToCallBubble.show();
        return returnToCallBubble;
    }

    @Override
    public void onIncomingCall(DialerCall call) {
    }

    @Override
    public void onUpgradeToVideo(DialerCall call) {
    }

    @Override
    public void onSessionModificationStateChange(DialerCall call) {
    }

    @Override
    public void onCallListChange(CallList callList) {
        if (!isEnabled(context)) {
            hide();
            return;
        }

        boolean shouldStartInBubbleMode = InCallPresenter.getInstance().shouldStartInBubbleMode();
        InCallState newInCallState =
                InCallPresenter.getInstance().getPotentialStateFromCallList(callList);
        boolean isNewBackgroundCall =
                newInCallState != inCallState
                        && newInCallState == InCallState.OUTGOING
                        && shouldStartInBubbleMode;
        boolean bubbleNeverVisible = (bubble == null || !(bubble.isVisible() || bubble.isDismissed()));
        if (bubble != null && isNewBackgroundCall) {
            // If new outgoing call is in bubble mode, update bubble info.
            // We don't update if new call is not in bubble mode even if the existing call is.
            bubble.setBubbleInfo(generateBubbleInfoForBackgroundCalling());
        }
        if (((bubbleNeverVisible && newInCallState != InCallState.OUTGOING) || isNewBackgroundCall)
                && getCall() != null
                && !InCallPresenter.getInstance().isShowingInCallUi()) {
            LogUtil.i("ReturnToCallController.onCallListChange", "going to show bubble");
            show();
        } else {
            // The call to display might be different for the existing bubble
            startContactInfoSearch();
        }
        inCallState = newInCallState;
    }

    @Override
    public void onDisconnect(DialerCall call) {
        if (!isEnabled(context)) {
            hide();
            return;
        }

        LogUtil.enterBlock("ReturnToCallController.onDisconnect");
        if (bubble != null && bubble.isVisible() && (getCall() == null)) {
            // Show "Call ended" and hide bubble when there is no outgoing, active or background call
            LogUtil.i("ReturnToCallController.onDisconnect", "show call ended and hide bubble");
            // Don't show text if it's Duo upgrade
            // It doesn't work for Duo fallback upgrade since we're not considered in call
            if (!TelecomUtil.isInCall(context) || CallList.getInstance().getIncomingCall() != null) {
                bubble.showText(context.getText(R.string.incall_call_ended));
            }
            hide();
        } else {
            startContactInfoSearch();
        }
    }

    @Override
    public void onWiFiToLteHandover(DialerCall call) {
    }

    @Override
    public void onHandoverToWifiFailed(DialerCall call) {
    }

    @Override
    public void onInternationalCallOnWifi(@NonNull DialerCall call) {
    }

    @Override
    public void onAudioStateChanged(CallAudioState audioState) {
        if (!isEnabled(context)) {
            hide();
            return;
        }

        this.audioState = audioState;
        if (bubble != null) {
            bubble.updateActions(generateActions());
        }
    }

    private void startContactInfoSearch() {
        DialerCall dialerCall = getCall();
        if (dialerCall != null) {
            contactInfoCache.findInfo(
                    dialerCall, false /* isIncoming */, new ReturnToCallContactInfoCacheCallback(this));
        }
    }

    private DialerCall getCall() {
        DialerCall dialerCall = CallList.getInstance().getOutgoingCall();
        if (dialerCall == null) {
            dialerCall = CallList.getInstance().getActiveOrBackgroundCall();
        }
        return dialerCall;
    }

    private void onPhotoAvatarReceived(@NonNull Drawable photo) {
        if (bubble != null) {
            bubble.updatePhotoAvatar(photo);
        }
    }

    private void onLetterTileAvatarReceived(@NonNull Drawable photo) {
        if (bubble != null) {
            bubble.updateAvatar(photo);
        }
    }

    private BubbleInfo generateBubbleInfo() {
        return BubbleInfo.builder()
                .setPrimaryColor(ThemeComponent.get(context).theme().getColorPrimary())
                .setPrimaryIcon(Icon.createWithResource(context, R.drawable.on_going_call))
                .setStartingYPosition(
                        InCallPresenter.getInstance().shouldStartInBubbleMode()
                                ? context.getResources().getDisplayMetrics().heightPixels / 2
                                : context
                                .getResources()
                                .getDimensionPixelOffset(R.dimen.return_to_call_initial_offset_y))
                .setActions(generateActions())
                .build();
    }

    private BubbleInfo generateBubbleInfoForBackgroundCalling() {
        return BubbleInfo.builder()
                .setPrimaryColor(ThemeComponent.get(context).theme().getColorPrimary())
                .setPrimaryIcon(Icon.createWithResource(context, R.drawable.on_going_call))
                .setStartingYPosition(context.getResources().getDisplayMetrics().heightPixels / 2)
                .setActions(generateActions())
                .build();
    }

    @NonNull
    private List<Action> generateActions() {
        List<Action> actions = new ArrayList<>();
        SpeakerButtonInfo speakerButtonInfo = new SpeakerButtonInfo(audioState);

        // Return to call
        actions.add(
                Action.builder()
                        .setIconDrawable(
                                context.getDrawable(R.drawable.quantum_ic_exit_to_app_flip_vd_theme_24))
                        .setIntent(fullScreen)
                        .setName(context.getText(R.string.bubble_return_to_call))
                        .setCheckable(false)
                        .build());
        // Mute/unmute
        actions.add(
                Action.builder()
                        .setIconDrawable(context.getDrawable(R.drawable.quantum_ic_mic_off_vd_theme_24))
                        .setChecked(audioState.isMuted())
                        .setIntent(toggleMute)
                        .setName(context.getText(R.string.incall_label_mute))
                        .build());
        // Speaker/audio selector
        actions.add(
                Action.builder()
                        .setIconDrawable(context.getDrawable(speakerButtonInfo.icon))
                        .setSecondaryIconDrawable(
                                speakerButtonInfo.nonBluetoothMode
                                        ? null
                                        : context.getDrawable(R.drawable.quantum_ic_arrow_drop_down_vd_theme_24))
                        .setName(context.getText(speakerButtonInfo.label))
                        .setCheckable(speakerButtonInfo.nonBluetoothMode)
                        .setChecked(speakerButtonInfo.isChecked)
                        .setIntent(speakerButtonInfo.nonBluetoothMode ? toggleSpeaker : showSpeakerSelect)
                        .build());
        // End call
        actions.add(
                Action.builder()
                        .setIconDrawable(context.getDrawable(R.drawable.quantum_ic_call_end_vd_theme_24))
                        .setIntent(endCall)
                        .setName(context.getText(R.string.incall_label_end_call))
                        .setCheckable(false)
                        .build());
        return actions;
    }

    @NonNull
    private PendingIntent createActionIntent(String action) {
        Intent intent = new Intent(context, ReturnToCallActionReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @NonNull
    private LetterTileDrawable createLettleTileDrawable(
            DialerCall dialerCall, ContactCacheEntry entry) {
        String preferredName =
                ContactsComponent.get(context)
                        .contactDisplayPreferences()
                        .getDisplayName(entry.namePrimary, entry.nameAlternative);
        if (TextUtils.isEmpty(preferredName)) {
            preferredName = entry.number;
        }

        LetterTileDrawable letterTile = new LetterTileDrawable(context.getResources());
        letterTile.setCanonicalDialerLetterTileDetails(
                dialerCall.updateNameIfRestricted(preferredName),
                entry.lookupKey,
                LetterTileDrawable.SHAPE_CIRCLE,
                LetterTileDrawable.getContactTypeFromPrimitives(
                        dialerCall.isVoiceMailNumber(),
                        dialerCall.isSpam(),
                        entry.isBusiness,
                        dialerCall.getNumberPresentation(),
                        dialerCall.isConferenceCall()));
        return letterTile;
    }

    private static class ReturnToCallContactInfoCacheCallback implements ContactInfoCacheCallback {

        private final WeakReference<ReturnToCallController> returnToCallControllerWeakReference;

        private ReturnToCallContactInfoCacheCallback(ReturnToCallController returnToCallController) {
            returnToCallControllerWeakReference = new WeakReference<>(returnToCallController);
        }

        @Override
        public void onContactInfoComplete(String callId, ContactCacheEntry entry) {
            ReturnToCallController returnToCallController = returnToCallControllerWeakReference.get();
            if (returnToCallController == null) {
                return;
            }
            if (entry.photo != null) {
                returnToCallController.onPhotoAvatarReceived(entry.photo);
            } else {
                DialerCall dialerCall = CallList.getInstance().getCallById(callId);
                if (dialerCall != null) {
                    returnToCallController.onLetterTileAvatarReceived(
                            returnToCallController.createLettleTileDrawable(dialerCall, entry));
                }
            }
        }

        @Override
        public void onImageLoadComplete(String callId, ContactCacheEntry entry) {
            ReturnToCallController returnToCallController = returnToCallControllerWeakReference.get();
            if (returnToCallController == null) {
                return;
            }
            if (entry.photo != null) {
                returnToCallController.onPhotoAvatarReceived(entry.photo);
            } else {
                DialerCall dialerCall = CallList.getInstance().getCallById(callId);
                if (dialerCall != null) {
                    returnToCallController.onLetterTileAvatarReceived(
                            returnToCallController.createLettleTileDrawable(dialerCall, entry));
                }
            }
        }
    }
}
