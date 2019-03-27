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

package app.diol.voicemail.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;

import com.google.auto.value.AutoValue;

import java.util.Optional;

/**
 * Identifies a carrier.
 */
@AutoValue
@TargetApi(VERSION_CODES.O)
@SuppressWarnings({"missingpermission"})
public abstract class CarrierIdentifier {

    public static Builder builder() {
        return new AutoValue_CarrierIdentifier.Builder().setGid1("");
    }

    /**
     * Create a identifier for a {@link PhoneAccountHandle}. Absent if the handle is not valid.
     */
    public static Optional<CarrierIdentifier> forHandle(
            Context context, @Nullable PhoneAccountHandle phoneAccountHandle) {
        if (phoneAccountHandle == null) {
            return Optional.empty();
        }
        TelephonyManager telephonyManager =
                context
                        .getSystemService(TelephonyManager.class)
                        .createForPhoneAccountHandle(phoneAccountHandle);
        if (telephonyManager == null) {
            return Optional.empty();
        }
        String gid1 = telephonyManager.getGroupIdLevel1();
        if (gid1 == null) {
            gid1 = "";
        }

        return Optional.of(
                builder().setMccMnc(telephonyManager.getSimOperator()).setGid1(gid1).build());
    }

    public abstract String mccMnc();

    /**
     * Group ID Level 1. Used to identify MVNO(Mobile Virtual Network Operators) who subleases other
     * carrier's network and share their mccMnc. MVNO should have a GID1 different from the host.
     */
    public abstract String gid1();

    /**
     * Builder for the matcher
     */
    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setMccMnc(String mccMnc);

        public abstract Builder setGid1(String gid1);

        public abstract CarrierIdentifier build();
    }
}
