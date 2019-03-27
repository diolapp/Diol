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
package app.diol.dialer.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

import java.util.Set;

/**
 * Class that helps us store dialer preferences that are phone account dependent. This is necessary
 * for cases such as settings that are phone account dependent e.g endless vm. The logic is to
 * essentially store the shared preference by appending the phone account id to the key.
 */
public class PerAccountSharedPreferences {
    private final String sharedPrefsKeyPrefix;
    private final SharedPreferences preferences;
    private final PhoneAccountHandle phoneAccountHandle;

    public PerAccountSharedPreferences(
            Context context, PhoneAccountHandle handle, SharedPreferences prefs) {
        preferences = prefs;
        phoneAccountHandle = handle;
        sharedPrefsKeyPrefix = "phone_account_dependent_";
    }

    /**
     * Not to be used, currently only used by {@VisualVoicemailPreferences} for legacy reasons.
     */
    protected PerAccountSharedPreferences(
            Context context, PhoneAccountHandle handle, SharedPreferences prefs, String prefix) {
        Assert.checkArgument(prefix.equals("visual_voicemail_"));
        preferences = prefs;
        phoneAccountHandle = handle;
        sharedPrefsKeyPrefix = prefix;
    }

    public Editor edit() {
        return new Editor();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return getValue(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return getValue(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return getValue(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return getValue(key, defValue);
    }

    public String getString(String key, String defValue) {
        return getValue(key, defValue);
    }

    @Nullable
    public String getString(String key) {
        return getValue(key, null);
    }

    public Set<String> getStringSet(String key, Set<String> defValue) {
        return getValue(key, defValue);
    }

    public boolean contains(String key) {
        return preferences.contains(getKey(key));
    }

    private <T> T getValue(String key, T defValue) {
        if (!contains(key)) {
            return defValue;
        }
        Object object = preferences.getAll().get(getKey(key));
        if (object == null) {
            return defValue;
        }
        return (T) object;
    }

    private String getKey(String key) {
        return sharedPrefsKeyPrefix + key + "_" + phoneAccountHandle.getId();
    }

    public class Editor {

        private final SharedPreferences.Editor editor;

        private Editor() {
            editor = preferences.edit();
        }

        public void apply() {
            editor.apply();
        }

        public Editor putBoolean(String key, boolean value) {
            editor.putBoolean(getKey(key), value);
            return this;
        }

        public Editor putFloat(String key, float value) {
            editor.putFloat(getKey(key), value);
            return this;
        }

        public Editor putInt(String key, int value) {
            editor.putInt(getKey(key), value);
            return this;
        }

        public Editor putLong(String key, long value) {
            editor.putLong(getKey(key), value);
            return this;
        }

        public Editor putString(String key, String value) {
            editor.putString(getKey(key), value);
            return this;
        }

        public Editor putStringSet(String key, Set<String> value) {
            editor.putStringSet(getKey(key), value);
            return this;
        }
    }
}
