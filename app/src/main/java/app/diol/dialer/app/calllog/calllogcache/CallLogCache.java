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

package app.diol.dialer.app.calllog.calllogcache;

import android.content.Context;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import app.diol.dialer.app.calllog.CallLogAdapter;
import app.diol.dialer.calllogutils.PhoneAccountUtils;
import app.diol.dialer.telecom.TelecomUtil;
import app.diol.dialer.util.CallUtil;

/**
 * This is the base class for the CallLogCaches.
 *
 * <p>Keeps a cache of recently made queries to the Telecom/Telephony processes. The aim of this
 * cache is to reduce the number of cross-process requests to TelecomManager, which can negatively
 * affect performance.
 *
 * <p>This is designed with the specific use case of the {@link CallLogAdapter} in mind.
 */
@ThreadSafe
public class CallLogCache {
    // TODO: Dialer should be fixed so as not to check isVoicemail() so often but at the time of
    // this writing, that was a much larger undertaking than creating this cache.

    protected final Context context;
    private final Map<PhoneAccountHandle, String> phoneAccountLabelCache = new ArrayMap<>();
    private final Map<PhoneAccountHandle, Integer> phoneAccountColorCache = new ArrayMap<>();
    private final Map<PhoneAccountHandle, Boolean> phoneAccountCallWithNoteCache = new ArrayMap<>();
    private boolean hasCheckedForVideoAvailability;
    private int videoAvailability;

    public CallLogCache(Context context) {
        this.context = context;
    }

    public synchronized void reset() {
        phoneAccountLabelCache.clear();
        phoneAccountColorCache.clear();
        phoneAccountCallWithNoteCache.clear();
        hasCheckedForVideoAvailability = false;
        videoAvailability = 0;
    }

    /**
     * Returns true if the given number is the number of the configured voicemail. To be able to
     * mock-out this, it is not a static method.
     */
    public synchronized boolean isVoicemailNumber(
            PhoneAccountHandle accountHandle, @Nullable CharSequence number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        return TelecomUtil.isVoicemailNumber(context, accountHandle, number.toString());
    }

    /**
     * Returns {@code true} when the current sim supports checking video calling capabilities via the
     * {@link android.provider.ContactsContract.CommonDataKinds.Phone#CARRIER_PRESENCE} column.
     */
    public boolean canRelyOnVideoPresence() {
        if (!hasCheckedForVideoAvailability) {
            videoAvailability = CallUtil.getVideoCallingAvailability(context);
            hasCheckedForVideoAvailability = true;
        }
        return (videoAvailability & CallUtil.VIDEO_CALLING_PRESENCE) != 0;
    }

    /**
     * Extract account label from PhoneAccount object.
     */
    public synchronized String getAccountLabel(PhoneAccountHandle accountHandle) {
        if (phoneAccountLabelCache.containsKey(accountHandle)) {
            return phoneAccountLabelCache.get(accountHandle);
        } else {
            String label = PhoneAccountUtils.getAccountLabel(context, accountHandle);
            phoneAccountLabelCache.put(accountHandle, label);
            return label;
        }
    }

    /**
     * Extract account color from PhoneAccount object.
     */
    public synchronized int getAccountColor(PhoneAccountHandle accountHandle) {
        if (phoneAccountColorCache.containsKey(accountHandle)) {
            return phoneAccountColorCache.get(accountHandle);
        } else {
            Integer color = PhoneAccountUtils.getAccountColor(context, accountHandle);
            phoneAccountColorCache.put(accountHandle, color);
            return color;
        }
    }

    /**
     * Determines if the PhoneAccount supports specifying a call subject (i.e. calling with a note)
     * for outgoing calls.
     *
     * @param accountHandle The PhoneAccount handle.
     * @return {@code true} if calling with a note is supported, {@code false} otherwise.
     */
    public synchronized boolean doesAccountSupportCallSubject(PhoneAccountHandle accountHandle) {
        if (phoneAccountCallWithNoteCache.containsKey(accountHandle)) {
            return phoneAccountCallWithNoteCache.get(accountHandle);
        } else {
            Boolean supportsCallWithNote =
                    PhoneAccountUtils.getAccountSupportsCallSubject(context, accountHandle);
            phoneAccountCallWithNoteCache.put(accountHandle, supportsCallWithNote);
            return supportsCallWithNote;
        }
    }
}
