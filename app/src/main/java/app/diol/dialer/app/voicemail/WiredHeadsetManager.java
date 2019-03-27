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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import app.diol.dialer.common.LogUtil;

/**
 * Listens for and caches headset state.
 */
class WiredHeadsetManager {

    private static final String TAG = WiredHeadsetManager.class.getSimpleName();
    private final WiredHeadsetBroadcastReceiver receiver;
    private boolean isPluggedIn;
    private Listener listener;
    private Context context;

    WiredHeadsetManager(Context context) {
        this.context = context;
        receiver = new WiredHeadsetBroadcastReceiver();

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isPluggedIn = audioManager.isWiredHeadsetOn();
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    boolean isPluggedIn() {
        return isPluggedIn;
    }

    void registerReceiver() {
        // Register for misc other intent broadcasts.
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(receiver, intentFilter);
    }

    void unregisterReceiver() {
        context.unregisterReceiver(receiver);
    }

    private void onHeadsetPluggedInChanged(boolean isPluggedIn) {
        if (this.isPluggedIn != isPluggedIn) {
            LogUtil.v(
                    TAG,
                    "onHeadsetPluggedInChanged, mIsPluggedIn: " + this.isPluggedIn + " -> " + isPluggedIn);
            boolean oldIsPluggedIn = this.isPluggedIn;
            this.isPluggedIn = isPluggedIn;
            if (listener != null) {
                listener.onWiredHeadsetPluggedInChanged(oldIsPluggedIn, this.isPluggedIn);
            }
        }
    }

    interface Listener {

        void onWiredHeadsetPluggedInChanged(boolean oldIsPluggedIn, boolean newIsPluggedIn);
    }

    /**
     * Receiver for wired headset plugged and unplugged events.
     */
    private class WiredHeadsetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                boolean isPluggedIn = intent.getIntExtra("state", 0) == 1;
                LogUtil.v(TAG, "ACTION_HEADSET_PLUG event, plugged in: " + isPluggedIn);
                onHeadsetPluggedInChanged(isPluggedIn);
            }
        }
    }
}
