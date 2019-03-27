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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.Random;

import app.diol.R;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.logging.DialerImpression.Type;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.storage.StorageComponent;

/**
 * Listen to the broadcast when the user dials "*#*#[number]#*#*" to toggle the event answer hint.
 */
public class PawSecretCodeListener extends BroadcastReceiver {

    public static final String PAW_ENABLED_WITH_SECRET_CODE_KEY = "paw_enabled_with_secret_code";
    /**
     * Which paw to show, must be {@link PawType}
     */
    public static final String PAW_TYPE = "paw_type";
    /**
     * Resource id is not stable across app versions. Use {@link #PAW_TYPE} instead.
     */
    @Deprecated
    public static final String PAW_DRAWABLE_ID_KEY = "paw_drawable_id";
    public static final int PAW_TYPE_INVALID = 0;
    public static final int PAW_TYPE_CAT = 1;
    public static final int PAW_TYPE_DOG = 2;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    static final String CONFIG_PAW_SECRET_CODE = "paw_secret_code";

    public static void selectPawType(SharedPreferences preferences) {
        @PawType int pawType;
        if (new Random().nextBoolean()) {
            pawType = PAW_TYPE_CAT;
        } else {
            pawType = PAW_TYPE_DOG;
        }
        preferences
                .edit()
                .putBoolean(PAW_ENABLED_WITH_SECRET_CODE_KEY, true)
                .putInt(PAW_TYPE, pawType)
                .apply();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String host = intent.getData().getHost();
        if (TextUtils.isEmpty(host)) {
            return;
        }
        String secretCode =
                ConfigProviderComponent.get(context)
                        .getConfigProvider()
                        .getString(CONFIG_PAW_SECRET_CODE, "729");
        if (secretCode == null) {
            return;
        }
        if (!TextUtils.equals(secretCode, host)) {
            return;
        }
        SharedPreferences preferences = StorageComponent.get(context).unencryptedSharedPrefs();
        boolean wasEnabled = preferences.getBoolean(PAW_ENABLED_WITH_SECRET_CODE_KEY, false);
        if (wasEnabled) {
            preferences.edit().putBoolean(PAW_ENABLED_WITH_SECRET_CODE_KEY, false).apply();
            Toast.makeText(context, R.string.event_deactivated, Toast.LENGTH_SHORT).show();
            Logger.get(context).logImpression(Type.EVENT_ANSWER_HINT_DEACTIVATED);
            LogUtil.i("PawSecretCodeListener.onReceive", "PawAnswerHint disabled");
        } else {
            selectPawType(preferences);
            Toast.makeText(context, R.string.event_activated, Toast.LENGTH_SHORT).show();
            Logger.get(context).logImpression(Type.EVENT_ANSWER_HINT_ACTIVATED);
            LogUtil.i("PawSecretCodeListener.onReceive", "PawAnswerHint enabled");
        }
    }

    /**
     * Enum for all paws.
     */
    @IntDef({PAW_TYPE_INVALID, PAW_TYPE_CAT, PAW_TYPE_DOG})
    @interface PawType {
    }
}
