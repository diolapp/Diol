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

package app.diol.dialer.telecom;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.telecom.Call;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.google.common.base.Optional;

import app.diol.dialer.common.Assert;
import app.diol.dialer.location.GeoUtil;

/**
 * Class to provide a standard interface for obtaining information from the underlying
 * android.telecom.Call. Much of this should be obtained through the incall.Call, but on occasion we
 * need to interact with the telecom.Call directly (eg. call blocking, before the incall.Call has
 * been created).
 */
public class TelecomCallUtil {

    /**
     * Returns Whether the call handle is an emergency number.
     */
    public static boolean isEmergencyCall(@NonNull Call call) {
        Assert.isNotNull(call);
        Uri handle = call.getDetails().getHandle();
        return PhoneNumberUtils.isEmergencyNumber(handle == null ? "" : handle.getSchemeSpecificPart());
    }

    /**
     * Returns The phone number which the {@code Call} is currently connected, or {@code null} if the
     * number is not available.
     */
    @Nullable
    public static String getNumber(@Nullable Call call) {
        if (call == null) {
            return null;
        }
        if (call.getDetails().getGatewayInfo() != null) {
            return call.getDetails().getGatewayInfo().getOriginalAddress().getSchemeSpecificPart();
        }
        Uri handle = getHandle(call);
        return handle == null ? null : handle.getSchemeSpecificPart();
    }

    /**
     * Returns The handle (e.g., phone number) to which the {@code Call} is currently connected, or
     * {@code null} if the number is not available.
     */
    @Nullable
    public static Uri getHandle(@Nullable Call call) {
        return call == null ? null : call.getDetails().getHandle();
    }

    /**
     * Normalizes the number of the {@code call} to E.164. If the number for the call does not contain
     * a country code, then the current location as defined by {@link
     * GeoUtil#getCurrentCountryIso(Context)} is used.
     *
     * <p>If the number cannot be formatted (because for example number is invalid), returns the
     * number with non-dialable digits removed.
     */
    @WorkerThread
    public static Optional<String> getNormalizedNumber(Context appContext, Call call) {
        Assert.isWorkerThread();

        Optional<String> validE164 = getValidE164Number(appContext, call);
        if (validE164.isPresent()) {
            return validE164;
        }
        String rawNumber = getNumber(call);
        if (TextUtils.isEmpty(rawNumber)) {
            return Optional.absent();
        }
        return Optional.of(PhoneNumberUtils.normalizeNumber(rawNumber));
    }

    /**
     * Formats the number of the {@code call} to E.164 if it is valid. If the number for the call does
     * not contain a country code, then the current location as defined by {@link
     * GeoUtil#getCurrentCountryIso(Context)} is used.
     *
     * <p>If the number cannot be formatted (because for example it is invalid), returns {@link
     * Optional#absent()}.
     */
    @WorkerThread
    public static Optional<String> getValidE164Number(Context appContext, Call call) {
        Assert.isWorkerThread();
        String rawNumber = getNumber(call);
        if (TextUtils.isEmpty(rawNumber)) {
            return Optional.absent();
        }
        return Optional.fromNullable(
                PhoneNumberUtils.formatNumberToE164(rawNumber, GeoUtil.getCurrentCountryIso(appContext)));
    }
}
