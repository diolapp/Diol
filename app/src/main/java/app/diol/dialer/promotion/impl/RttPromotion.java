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

package app.diol.dialer.promotion.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.DrawableRes;

import javax.inject.Inject;

import app.diol.R;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProvider;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.promotion.Promotion;
import app.diol.dialer.spannable.ContentWithLearnMoreSpanner;
import app.diol.dialer.storage.StorageComponent;
import app.diol.dialer.storage.Unencrypted;

/**
 * RTT promotion.
 */
public final class RttPromotion implements Promotion {
    private static final String SHARED_PREFERENCE_KEY_ENABLED = "rtt_promotion_enabled";
    private static final String SHARED_PREFERENCE_KEY_DISMISSED = "rtt_promotion_dismissed";
    private final Context appContext;
    private final SharedPreferences sharedPreferences;
    private final ConfigProvider configProvider;

    @Inject
    RttPromotion(
            @ApplicationContext Context context,
            @Unencrypted SharedPreferences sharedPreferences,
            ConfigProvider configProvider) {
        appContext = context;
        this.sharedPreferences = sharedPreferences;
        this.configProvider = configProvider;
    }

    public static void setEnabled(Context context) {
        LogUtil.enterBlock("RttPromotion.setEnabled");
        StorageComponent.get(context)
                .unencryptedSharedPrefs()
                .edit()
                .putBoolean(SHARED_PREFERENCE_KEY_ENABLED, true)
                .apply();
    }

    @Override
    public int getType() {
        return PromotionType.BOTTOM_SHEET;
    }

    @Override
    public boolean isEligibleToBeShown() {
        return sharedPreferences.getBoolean(SHARED_PREFERENCE_KEY_ENABLED, false)
                && !sharedPreferences.getBoolean(SHARED_PREFERENCE_KEY_DISMISSED, false);
    }

    @Override
    public CharSequence getTitle() {
        return appContext.getString(R.string.rtt_promotion_title);
    }

    @Override
    public CharSequence getDetails() {
        return new ContentWithLearnMoreSpanner(appContext)
                .create(
                        appContext.getString(R.string.rtt_promotion_details),
                        configProvider.getString(
                                "rtt_promo_learn_more_link_full_url",
                                "http://support.google.com/pixelphone/?p=dialer_rtt"));
    }

    @Override
    @DrawableRes
    public int getIconRes() {
        return R.drawable.quantum_ic_rtt_vd_theme_24;
    }

    @Override
    public void dismiss() {
        sharedPreferences.edit().putBoolean(SHARED_PREFERENCE_KEY_DISMISSED, true).apply();
    }
}
