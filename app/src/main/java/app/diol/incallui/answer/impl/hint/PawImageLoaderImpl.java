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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.storage.StorageComponent;
import app.diol.incallui.answer.impl.hint.PawSecretCodeListener.PawType;

/**
 * Decrypt the event payload to be shown if in a specific time range and the key is received.
 */
public final class PawImageLoaderImpl implements PawImageLoader {

    @Override
    @Nullable
    public Drawable loadPayload(@NonNull Context context) {
        Assert.isNotNull(context);

        SharedPreferences preferences = StorageComponent.get(context).unencryptedSharedPrefs();
        if (!preferences.getBoolean(PawSecretCodeListener.PAW_ENABLED_WITH_SECRET_CODE_KEY, false)) {
            return null;
        }
        @PawType
        int pawType =
                preferences.getInt(PawSecretCodeListener.PAW_TYPE, PawSecretCodeListener.PAW_TYPE_INVALID);

        if (pawType == PawSecretCodeListener.PAW_TYPE_INVALID) {
            LogUtil.i("PawImageLoaderImpl.loadPayload", "paw type not found, rerolling");
            PawSecretCodeListener.selectPawType(preferences);
            pawType =
                    preferences.getInt(
                            PawSecretCodeListener.PAW_TYPE, PawSecretCodeListener.PAW_TYPE_INVALID);
        }

        switch (pawType) {
            case PawSecretCodeListener.PAW_TYPE_CAT:
                return context.getDrawable(R.drawable.cat_paw);
            case PawSecretCodeListener.PAW_TYPE_DOG:
                return context.getDrawable(R.drawable.dog_paw);
            default:
                throw Assert.createAssertionFailException("unknown paw type " + pawType);
        }
    }
}
