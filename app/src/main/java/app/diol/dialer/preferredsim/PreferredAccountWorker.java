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

package app.diol.dialer.preferredsim;

import android.telecom.PhoneAccountHandle;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import app.diol.contacts.common.widget.SelectPhoneAccountDialogOptions;
import app.diol.dialer.preferredsim.suggestion.SuggestionProvider.Suggestion;

/**
 * Query a preferred SIM to make a call with.
 */
@SuppressWarnings({"missingPermission", "Guava"})
public interface PreferredAccountWorker {

    /**
     * @return a {@link SelectPhoneAccountDialogOptions} for a dialog to select SIM for voicemail call
     */
    SelectPhoneAccountDialogOptions getVoicemailDialogOptions();

    /**
     * Return {@link Result} for the best {@link PhoneAccountHandle} among {@code candidates} to call
     * the number with. If none are eligible, a {@link SelectPhoneAccountDialogOptions} will be
     * provided to show a dialog for the user to manually select.
     */
    ListenableFuture<Result> selectAccount(String phoneNumber, List<PhoneAccountHandle> candidates);

    /**
     * Result of the query.
     */
    @AutoValue
    abstract class Result {

        public static Builder builder(PhoneAccountHandle selectedPhoneAccountHandle) {
            return new AutoValue_PreferredAccountWorker_Result.Builder()
                    .setSelectedPhoneAccountHandle(selectedPhoneAccountHandle);
        }

        public static Builder builder(SelectPhoneAccountDialogOptions.Builder optionsBuilder) {
            return new AutoValue_PreferredAccountWorker_Result.Builder()
                    .setDialogOptionsBuilder(optionsBuilder);
        }

        /**
         * The phone account to dial with for the number. Absent if no account can be auto selected. If
         * absent, {@link #getSelectedPhoneAccountHandle()} will be present to show a dialog for the
         * user to manually select.
         */
        public abstract Optional<PhoneAccountHandle> getSelectedPhoneAccountHandle();

        /**
         * The {@link SelectPhoneAccountDialogOptions} that should be used to show the selection dialog.
         * Absent if {@link #getSelectedPhoneAccountHandle()} is present, which should be used directly
         * instead of asking the user.
         */
        public abstract Optional<SelectPhoneAccountDialogOptions.Builder> getDialogOptionsBuilder();

        /**
         * {@link android.provider.ContactsContract.Data#_ID} of the row matching the number. If the
         * preferred account is to be set it should be stored in this row
         */
        public abstract Optional<String> getDataId();

        public abstract Optional<Suggestion> getSuggestion();

        /**
         * For implementations of {@link PreferredAccountWorker} only.
         */
        @AutoValue.Builder
        public abstract static class Builder {

            abstract Builder setSelectedPhoneAccountHandle(PhoneAccountHandle phoneAccountHandle);

            public abstract Builder setDataId(String dataId);

            abstract Builder setDialogOptionsBuilder(
                    SelectPhoneAccountDialogOptions.Builder optionsBuilder);

            public abstract Builder setSuggestion(Suggestion suggestion);

            public abstract Result build();
        }
    }
}
