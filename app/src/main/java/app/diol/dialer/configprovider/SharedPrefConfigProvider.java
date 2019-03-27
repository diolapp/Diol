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
package app.diol.dialer.configprovider;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.storage.StorageComponent;
import app.diol.dialer.storage.Unencrypted;
import app.diol.dialer.strictmode.StrictModeUtils;

/**
 * {@link ConfigProvider} which uses a shared preferences file.
 *
 * <p>Config flags can be written using adb (with root access), for example:
 *
 * <pre>
 *   adb root
 *   adb shell am startservice -n \
 *     'com.android.dialer/.configprovider.SharedPrefConfigProvider\$Service' \
 *     --ez boolean_flag_name flag_value
 * </pre>
 *
 * <p>(For longs use --el and for strings use --es.)
 *
 * <p>Flags can be viewed with:
 *
 * <pre>
 *   adb shell cat \
 *     /data/user_de/0/com.android.dialer/shared_prefs/com.android.dialer_preferences.xml
 * </pre>
 */
public class SharedPrefConfigProvider implements ConfigProvider {
    private static final String PREF_PREFIX = "config_provider_prefs_";

    private final SharedPreferences sharedPreferences;

    @Inject
    SharedPrefConfigProvider(@Unencrypted SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    private static SharedPreferences getSharedPrefs(Context appContext) {
        return StorageComponent.get(appContext).unencryptedSharedPrefs();
    }

    /**
     * Set a boolean config value.
     */
    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(PREF_PREFIX + key, value).apply();
    }

    public void putLong(String key, long value) {
        sharedPreferences.edit().putLong(PREF_PREFIX + key, value).apply();
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(PREF_PREFIX + key, value).apply();
    }

    @Override
    public String getString(String key, String defaultValue) {
        // Reading shared prefs on the main thread is generally safe since a single instance is cached.
        return StrictModeUtils.bypass(
                () -> sharedPreferences.getString(PREF_PREFIX + key, defaultValue));
    }

    @Override
    public long getLong(String key, long defaultValue) {
        // Reading shared prefs on the main thread is generally safe since a single instance is cached.
        return StrictModeUtils.bypass(() -> sharedPreferences.getLong(PREF_PREFIX + key, defaultValue));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        // Reading shared prefs on the main thread is generally safe since a single instance is cached.
        return StrictModeUtils.bypass(
                () -> sharedPreferences.getBoolean(PREF_PREFIX + key, defaultValue));
    }

    /**
     * Service to write values into {@link SharedPrefConfigProvider} using adb.
     */
    public static class Service extends IntentService {

        public Service() {
            super("SharedPrefConfigProvider.Service");
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            if (intent == null || intent.getExtras() == null || intent.getExtras().size() != 1) {
                LogUtil.w("SharedPrefConfigProvider.Service.onHandleIntent", "must set exactly one extra");
                return;
            }
            String key = intent.getExtras().keySet().iterator().next();
            Object value = intent.getExtras().get(key);
            put(key, value);
        }

        private void put(String key, Object value) {
            Editor editor = getSharedPrefs(getApplicationContext()).edit();
            String prefixedKey = PREF_PREFIX + key;
            if (value instanceof Boolean) {
                editor.putBoolean(prefixedKey, (Boolean) value);
            } else if (value instanceof Long) {
                editor.putLong(prefixedKey, (Long) value);
            } else if (value instanceof String) {
                editor.putString(prefixedKey, (String) value);
            } else {
                throw Assert.createAssertionFailException("unsupported extra type: " + value.getClass());
            }
            editor.apply();
        }
    }
}
