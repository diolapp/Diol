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

package app.diol.incallui.answer.impl.hint;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.storage.StorageComponent;
import app.diol.incallui.util.AccessibilityUtil;

/**
 * Selects a AnswerHint to show. If there's no suitable hints {@link EmptyAnswerHint} will be used,
 * which does nothing.
 */
public class AnswerHintFactory {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    static final String CONFIG_ANSWER_HINT_ANSWERED_THRESHOLD_KEY = "answer_hint_answered_threshold";

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    static final String CONFIG_ANSWER_HINT_WHITELISTED_DEVICES_KEY =
            "answer_hint_whitelisted_devices";
    @VisibleForTesting
    static final String ANSWERED_COUNT_PREFERENCE_KEY = "answer_hint_answered_count";
    // Most popular devices released before NDR1 is whitelisted. Their user are likely to have seen
    // the legacy UI.
    private static final String DEFAULT_WHITELISTED_DEVICES_CSV =
            "/hammerhead//bullhead//angler//shamu//gm4g//gm4g_s//AQ4501//gce_x86_phone//gm4gtkc_s/"
                    + "/Sparkle_V//Mi-498//AQ4502//imobileiq2//A65//H940//m8_google//m0xx//A10//ctih220/"
                    + "/Mi438S//bacon/";
    private final PawImageLoader pawImageLoader;

    public AnswerHintFactory(@NonNull PawImageLoader pawImageLoader) {
        this.pawImageLoader = Assert.isNotNull(pawImageLoader);
    }

    public static void increaseAnsweredCount(Context context) {
        SharedPreferences sharedPreferences = StorageComponent.get(context).unencryptedSharedPrefs();
        int answeredCount = sharedPreferences.getInt(ANSWERED_COUNT_PREFERENCE_KEY, 0);
        sharedPreferences.edit().putInt(ANSWERED_COUNT_PREFERENCE_KEY, answeredCount + 1).apply();
    }

    @VisibleForTesting
    static boolean shouldShowAnswerHint(Context context, String device) {
        if (AccessibilityUtil.isTouchExplorationEnabled(context)) {
            return false;
        }
        // Devices that has the legacy dialer installed are whitelisted as they are likely to go through
        // a UX change during updates.
        if (!isDeviceWhitelisted(context, device)) {
            return false;
        }

        // If the user has gone through the process a few times we can assume they have learnt the
        // method.
        int answeredCount =
                StorageComponent.get(context)
                        .unencryptedSharedPrefs()
                        .getInt(ANSWERED_COUNT_PREFERENCE_KEY, 0);
        long threshold =
                ConfigProviderComponent.get(context)
                        .getConfigProvider()
                        .getLong(CONFIG_ANSWER_HINT_ANSWERED_THRESHOLD_KEY, 3);
        LogUtil.i(
                "AnswerHintFactory.shouldShowAnswerHint",
                "answerCount: %d, threshold: %d",
                answeredCount,
                threshold);
        return answeredCount < threshold;
    }

    /**
     * @param device         should be the value of{@link Build#PRODUCT}.
     * @param configProvider should provide a list of devices quoted with '/' concatenated to a
     *                       string.
     */
    private static boolean isDeviceWhitelisted(Context context, String device) {
        return ConfigProviderComponent.get(context)
                .getConfigProvider()
                .getString(CONFIG_ANSWER_HINT_WHITELISTED_DEVICES_KEY, DEFAULT_WHITELISTED_DEVICES_CSV)
                .contains("/" + device + "/");
    }

    @NonNull
    public AnswerHint create(Context context, long puckUpDuration, long puckUpDelay) {
        if (shouldShowAnswerHint(context, Build.PRODUCT)) {
            return new DotAnswerHint(context, puckUpDuration, puckUpDelay);
        }

        // Display the event answer hint if the payload is available.
        Drawable eventPayload = pawImageLoader.loadPayload(context);
        if (eventPayload != null) {
            return new PawAnswerHint(context, eventPayload, puckUpDuration, puckUpDelay);
        }

        return new EmptyAnswerHint();
    }
}
