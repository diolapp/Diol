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

package app.diol.dialer.preferredsim.suggestion;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.telecom.PhoneAccountHandle;

import com.google.common.base.Optional;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Provides hints to the user when selecting a SIM to make a call.
 */
@SuppressWarnings("Guava")
public interface SuggestionProvider {

    String EXTRA_SIM_SUGGESTION_REASON = "sim_suggestion_reason";

    /**
     * Return the hint for {@code phoneAccountHandle}. Absent if no hint is available for the account.
     */
    static Optional<String> getHint(
            Context context, PhoneAccountHandle phoneAccountHandle, @Nullable Suggestion suggestion) {
        if (suggestion == null) {
            return Optional.absent();
        }
        if (!phoneAccountHandle.equals(suggestion.phoneAccountHandle)) {
            return Optional.absent();
        }
        switch (suggestion.reason) {
            case INTRA_CARRIER:
                return Optional.of(
                        context.getString(R.string.pre_call_select_phone_account_hint_intra_carrier));
            case FREQUENT:
                return Optional.of(context.getString(R.string.pre_call_select_phone_account_hint_frequent));
            default:
                LogUtil.w("CallingAccountSelector.getHint", "unhandled reason " + suggestion.reason);
                return Optional.absent();
        }
    }

    @WorkerThread
    @NonNull
    Optional<Suggestion> getSuggestion(@NonNull Context context, @NonNull String number);

    @WorkerThread
    void reportUserSelection(
            @NonNull Context context,
            @NonNull String number,
            @NonNull PhoneAccountHandle phoneAccountHandle,
            boolean rememberSelection);

    @WorkerThread
    void reportIncorrectSuggestion(
            @NonNull Context context, @NonNull String number, @NonNull PhoneAccountHandle newAccount);

    /**
     * The reason the suggestion is made.
     */
    enum Reason {
        UNKNOWN,
        // The SIM has the same carrier as the callee.
        INTRA_CARRIER,
        // The user has selected the SIM for the callee multiple times.
        FREQUENT,
        // The user has select the SIM for this category of calls (contacts from certain accounts,
        // etc.).
        USER_SET,
        // The user has selected the SIM for all contacts on the account.
        ACCOUNT,
        // Unspecified reason.
        OTHER,
    }

    /**
     * The suggestion.
     */
    class Suggestion {
        @NonNull
        public final PhoneAccountHandle phoneAccountHandle;
        @NonNull
        public final Reason reason;
        public final boolean shouldAutoSelect;

        public Suggestion(
                @NonNull PhoneAccountHandle phoneAccountHandle,
                @NonNull Reason reason,
                boolean shouldAutoSelect) {
            this.phoneAccountHandle = Assert.isNotNull(phoneAccountHandle);
            this.reason = Assert.isNotNull(reason);
            this.shouldAutoSelect = shouldAutoSelect;
        }
    }
}
