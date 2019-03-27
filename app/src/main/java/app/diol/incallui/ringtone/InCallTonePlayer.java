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

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import app.diol.incallui.Log;
import app.diol.incallui.async.PausableExecutor;

/**
 * Class responsible for playing in-call related tones in a background thread. This class only
 * allows one tone to be played at a time.
 */
public class InCallTonePlayer {

    public static final int TONE_CALL_WAITING = 4;

    public static final int VOLUME_RELATIVE_HIGH_PRIORITY = 80;

    @NonNull
    private final ToneGeneratorFactory toneGeneratorFactory;
    @NonNull
    private final PausableExecutor executor;
    private @Nullable
    CountDownLatch numPlayingTones;

    /**
     * Creates a new InCallTonePlayer.
     *
     * @param toneGeneratorFactory the {@link ToneGeneratorFactory} used to create {@link
     *                             ToneGenerator}s.
     * @param executor             the {@link PausableExecutor} used to play tones in a background thread.
     * @throws NullPointerException if audioModeProvider, toneGeneratorFactory, or executor are {@code
     *                              null}.
     */
    public InCallTonePlayer(
            @NonNull ToneGeneratorFactory toneGeneratorFactory, @NonNull PausableExecutor executor) {
        this.toneGeneratorFactory = Objects.requireNonNull(toneGeneratorFactory);
        this.executor = Objects.requireNonNull(executor);
    }

    /**
     * @return {@code true} if a tone is currently playing, {@code false} otherwise.
     */
    public boolean isPlayingTone() {
        return numPlayingTones != null && numPlayingTones.getCount() > 0;
    }

    /**
     * Plays the given tone in a background thread.
     *
     * @param tone the tone to play.
     * @throws IllegalStateException    if a tone is already playing.
     * @throws IllegalArgumentException if the tone is invalid.
     */
    public void play(int tone) {
        if (isPlayingTone()) {
            throw new IllegalStateException("Tone already playing");
        }
        final ToneGeneratorInfo info = getToneGeneratorInfo(tone);
        numPlayingTones = new CountDownLatch(1);
        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        playOnBackgroundThread(info);
                    }
                });
    }

    private ToneGeneratorInfo getToneGeneratorInfo(int tone) {
        switch (tone) {
            case TONE_CALL_WAITING:
                /*
                 * DialerCall waiting tones play until they're stopped either by the user accepting or
                 * declining the call so the tone length is set at what's effectively forever. The
                 * tone is played at a high priority volume and through STREAM_VOICE_CALL since it's
                 * call related and using that stream will route it through bluetooth devices
                 * appropriately.
                 */
                return new ToneGeneratorInfo(
                        ToneGenerator.TONE_SUP_CALL_WAITING,
                        VOLUME_RELATIVE_HIGH_PRIORITY,
                        Integer.MAX_VALUE,
                        AudioManager.STREAM_VOICE_CALL);
            default:
                throw new IllegalArgumentException("Bad tone: " + tone);
        }
    }

    private void playOnBackgroundThread(ToneGeneratorInfo info) {
        ToneGenerator toneGenerator = null;
        try {
            Log.v(this, "Starting tone " + info);
            toneGenerator = toneGeneratorFactory.newInCallToneGenerator(info.stream, info.volume);
            toneGenerator.startTone(info.tone);
            /*
             * During tests, this will block until the tests call mExecutor.ackMilestone. This call
             * allows for synchronization to the point where the tone has started playing.
             */
            executor.milestone();
            if (numPlayingTones != null) {
                numPlayingTones.await(info.toneLengthMillis, TimeUnit.MILLISECONDS);
                // Allows for synchronization to the point where the tone has completed playing.
                executor.milestone();
            }
        } catch (InterruptedException e) {
            Log.w(this, "Interrupted while playing in-call tone.");
        } finally {
            if (toneGenerator != null) {
                toneGenerator.release();
            }
            if (numPlayingTones != null) {
                numPlayingTones.countDown();
            }
            // Allows for synchronization to the point where this background thread has cleaned up.
            executor.milestone();
        }
    }

    /**
     * Stops playback of the current tone.
     */
    public void stop() {
        if (numPlayingTones != null) {
            numPlayingTones.countDown();
        }
    }

    private static class ToneGeneratorInfo {

        public final int tone;
        public final int volume;
        public final int toneLengthMillis;
        public final int stream;

        public ToneGeneratorInfo(int toneGeneratorType, int volume, int toneLengthMillis, int stream) {
            this.tone = toneGeneratorType;
            this.volume = volume;
            this.toneLengthMillis = toneLengthMillis;
            this.stream = stream;
        }

        @Override
        public String toString() {
            return "ToneGeneratorInfo{"
                    + "toneLengthMillis="
                    + toneLengthMillis
                    + ", tone="
                    + tone
                    + ", volume="
                    + volume
                    + '}';
        }
    }
}
