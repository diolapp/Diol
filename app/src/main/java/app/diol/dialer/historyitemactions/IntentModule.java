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

package app.diol.dialer.historyitemactions;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.google.common.collect.ImmutableList;

import app.diol.R;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.precall.PreCall;
import app.diol.dialer.util.DialerUtils;
import app.diol.dialer.util.IntentUtil;

/**
 * {@link HistoryItemActionModule} useful for making easy to build modules based on starting an
 * intent.
 */
public class IntentModule implements HistoryItemActionModule {

    private final Context context;
    private final Intent intent;
    private final @StringRes
    int text;
    private final @DrawableRes
    int image;
    private final ImmutableList<DialerImpression.Type> impressions;

    /**
     * @deprecated use {@link IntentModule#IntentModule(Context, Intent, int, int, ImmutableList)}
     * instead.
     */
    @Deprecated
    public IntentModule(Context context, Intent intent, @StringRes int text, @DrawableRes int image) {
        this(context, intent, text, image, /* impressions = */ ImmutableList.of());
    }

    IntentModule(
            Context context,
            Intent intent,
            @StringRes int text,
            @DrawableRes int image,
            ImmutableList<DialerImpression.Type> impressions) {
        this.context = context;
        this.intent = intent;
        this.text = text;
        this.image = image;
        this.impressions = impressions;
    }

    /**
     * @deprecated Use {@link #newCallModule(Context, CallIntentBuilder, ImmutableList)} instead.
     */
    @Deprecated
    public static IntentModule newCallModule(Context context, CallIntentBuilder callIntentBuilder) {
        return newCallModule(context, callIntentBuilder, /* impressions = */ ImmutableList.of());
    }

    /**
     * Creates a module for starting an outgoing call with a {@link CallIntentBuilder}.
     */
    static IntentModule newCallModule(
            Context context,
            CallIntentBuilder callIntentBuilder,
            ImmutableList<DialerImpression.Type> impressions) {
        @StringRes int text;
        @DrawableRes int image;

        if (callIntentBuilder.isVideoCall()) {
            text = R.string.video_call;
            image = R.drawable.quantum_ic_videocam_vd_white_24;
        } else {
            text = R.string.voice_call;
            image = R.drawable.quantum_ic_call_white_24;
        }

        return new IntentModule(
                context, PreCall.getIntent(context, callIntentBuilder), text, image, impressions);
    }

    /**
     * @deprecated Use {@link #newModuleForSendingTextMessage(Context, String, ImmutableList)}
     * instead.
     */
    @Deprecated
    public static IntentModule newModuleForSendingTextMessage(Context context, String number) {
        return newModuleForSendingTextMessage(context, number, /* impressions = */ ImmutableList.of());
    }

    /**
     * Creates a module for sending a text message to the given number.
     */
    static IntentModule newModuleForSendingTextMessage(
            Context context, String number, ImmutableList<DialerImpression.Type> impressions) {
        return new IntentModule(
                context,
                IntentUtil.getSendSmsIntent(number),
                R.string.send_a_message,
                R.drawable.quantum_ic_message_vd_theme_24,
                impressions);
    }

    @Override
    public int getStringId() {
        return text;
    }

    @Override
    public int getDrawableId() {
        return image;
    }

    @Override
    public boolean onClick() {
        DialerUtils.startActivityWithErrorToast(context, intent);
        impressions.forEach(Logger.get(context)::logImpression);
        return true;
    }
}
