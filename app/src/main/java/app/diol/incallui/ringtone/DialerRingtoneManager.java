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

package app.diol.incallui.ringtone;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

import app.diol.incallui.call.CallList;
import app.diol.incallui.call.state.DialerCallState;

/**
 * Class that determines when ringtones should be played and can play the call waiting tone when
 * necessary.
 */
public class DialerRingtoneManager {

    /*
     * Flag used to determine if the Dialer is responsible for playing ringtones for incoming calls.
     * Once we're ready to enable Dialer Ringing, these flags should be removed.
     */
    private static final boolean IS_DIALER_RINGING_ENABLED = false;
    private final InCallTonePlayer inCallTonePlayer;
    private final CallList callList;
    private Boolean isDialerRingingEnabledForTesting;

    /**
     * Creates the DialerRingtoneManager with the given {@link InCallTonePlayer}.
     *
     * @param inCallTonePlayer the tone player used to play in-call tones.
     * @param callList         the CallList used to check for {@link DialerCallState#CALL_WAITING}
     * @throws NullPointerException if inCallTonePlayer or callList are null
     */
    public DialerRingtoneManager(
            @NonNull InCallTonePlayer inCallTonePlayer, @NonNull CallList callList) {
        this.inCallTonePlayer = Objects.requireNonNull(inCallTonePlayer);
        this.callList = Objects.requireNonNull(callList);
    }

    /**
     * Determines if a ringtone should be played for the given call state (see {@link
     * DialerCallState}) and {@link Uri}.
     *
     * @param callState   the call state for the call being checked.
     * @param ringtoneUri the ringtone to potentially play.
     * @return {@code true} if the ringtone should be played, {@code false} otherwise.
     */
    public boolean shouldPlayRingtone(int callState, @Nullable Uri ringtoneUri) {
        return isDialerRingingEnabled()
                && translateCallStateForCallWaiting(callState) == DialerCallState.INCOMING
                && ringtoneUri != null;
    }

    /**
     * Determines if an incoming call should vibrate as well as ring.
     *
     * @param resolver {@link ContentResolver} used to look up the {@link
     *                 Settings.System#VIBRATE_WHEN_RINGING} setting.
     * @return {@code true} if the call should vibrate, {@code false} otherwise.
     */
    public boolean shouldVibrate(ContentResolver resolver) {
        return Settings.System.getInt(resolver, Settings.System.VIBRATE_WHEN_RINGING, 0) != 0;
    }

    /**
     * The incoming callState is never set as {@link DialerCallState#CALL_WAITING} because {@link
     * DialerCall#translateState(int)} doesn't account for that case, check for it here
     */
    private int translateCallStateForCallWaiting(int callState) {
        if (callState != DialerCallState.INCOMING) {
            return callState;
        }
        return callList.getActiveCall() == null
                ? DialerCallState.INCOMING
                : DialerCallState.CALL_WAITING;
    }

    private boolean isDialerRingingEnabled() {
        return isDialerRingingEnabledForTesting != null
                ? isDialerRingingEnabledForTesting
                : IS_DIALER_RINGING_ENABLED;
    }

    /**
     * Determines if a call waiting tone should be played for the the given call state (see {@link
     * DialerCallState}).
     *
     * @param callState the call state for the call being checked.
     * @return {@code true} if the call waiting tone should be played, {@code false} otherwise.
     */
    public boolean shouldPlayCallWaitingTone(int callState) {
        return isDialerRingingEnabled()
                && translateCallStateForCallWaiting(callState) == DialerCallState.CALL_WAITING
                && !inCallTonePlayer.isPlayingTone();
    }

    /**
     * Plays the call waiting tone.
     */
    public void playCallWaitingTone() {
        if (!isDialerRingingEnabled()) {
            return;
        }
        inCallTonePlayer.play(InCallTonePlayer.TONE_CALL_WAITING);
    }

    /**
     * Stops playing the call waiting tone.
     */
    public void stopCallWaitingTone() {
        if (!isDialerRingingEnabled()) {
            return;
        }
        inCallTonePlayer.stop();
    }

    void setDialerRingingEnabledForTesting(boolean status) {
        isDialerRingingEnabledForTesting = status;
    }
}
