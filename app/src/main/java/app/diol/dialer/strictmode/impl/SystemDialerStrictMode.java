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

package app.diol.dialer.strictmode.impl;

import android.app.Application;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.Map;

import javax.inject.Inject;

import app.diol.dialer.common.Assert;
import app.diol.dialer.strictmode.DialerStrictMode;
import app.diol.dialer.strictmode.StrictModeUtils;

final class SystemDialerStrictMode implements DialerStrictMode {
    private static final VmPolicy VM_DEATH_PENALTY =
            new StrictMode.VmPolicy.Builder().penaltyLog().penaltyDeath().build();

    private static final ThreadPolicy THREAD_DEATH_PENALTY =
            new StrictMode.ThreadPolicy.Builder().penaltyLog().penaltyDeath().build();

    @Inject
    public SystemDialerStrictMode() {
    }

    /**
     * Set the recommended policy for the app.
     *
     * @param threadPenalties policy with preferred penalties. Detection bits will be ignored.
     */
    private static void setRecommendedMainThreadPolicy(StrictMode.ThreadPolicy threadPenalties) {
        StrictMode.ThreadPolicy threadPolicy =
                new StrictMode.ThreadPolicy.Builder(threadPenalties).detectAll().build();
        StrictMode.setThreadPolicy(threadPolicy);
    }

    /**
     * Set the recommended policy for the app.
     *
     * @param vmPenalties policy with preferred penalties. Detection bits should be unset.
     */
    private static void setRecommendedVMPolicy(StrictMode.VmPolicy vmPenalties) {
        setRecommendedVMPolicy(vmPenalties, StrictModeVmConfig.empty());
    }

    /**
     * Set the recommended policy for the app.
     *
     * @param vmPenalties policy with preferred penalties. Detection bits should be unset.
     */
    private static void setRecommendedVMPolicy(
            StrictMode.VmPolicy vmPenalties, StrictModeVmConfig config) {
        Assert.isNotNull(config);
        StrictMode.VmPolicy.Builder vmPolicyBuilder =
                new StrictMode.VmPolicy.Builder(vmPenalties)
                        .detectLeakedClosableObjects()
                        .detectLeakedSqlLiteObjects();
        if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
            vmPolicyBuilder.detectContentUriWithoutPermission();
            // TODO(azlatin): Enable detecting untagged sockets once: a bug is fixed.
            // vmPolicyBuilder.detectUntaggedSockets();
        }
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

    @MainThread
    @Override
    public void onApplicationCreate(Application application) {
        if (StrictModeUtils.isStrictModeAllowed()) {
            StrictModeUtils.warmupSharedPrefs(application);
            setRecommendedMainThreadPolicy(THREAD_DEATH_PENALTY);
            setRecommendedVMPolicy(VM_DEATH_PENALTY);

            // Because Android resets StrictMode policies after Application.onCreate is done, we set it
            // again right after.
            // See cl/105932355 for the discussion.
            // See a bug for the public bug.
            Handler handler = new Handler(Looper.myLooper());
            handler.postAtFrontOfQueue(() -> setRecommendedMainThreadPolicy(THREAD_DEATH_PENALTY));
        }
    }

    /**
     * VmPolicy configuration.
     */
    @AutoValue
    abstract static class StrictModeVmConfig {
        StrictModeVmConfig() {
        }

        public static StrictModeVmConfig empty() {
            return builder().build();
        }

        public static Builder builder() {
            return new AutoValue_SystemDialerStrictMode_StrictModeVmConfig.Builder();
        }

        /**
         * A map of a class to the maximum number of allowed instances of that class.
         */
        @Nullable
        abstract Map<Class<?>, Integer> maxInstanceLimits();

        /**
         * VmPolicy configuration builder.
         */
        @AutoValue.Builder
        public abstract static class Builder {
            Builder() {
            }

            public abstract Builder setMaxInstanceLimits(Map<Class<?>, Integer> limits);

            public abstract StrictModeVmConfig build();
        }
    }
}
