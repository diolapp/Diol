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

package app.diol.dialer.preferredsim.suggestion.stub;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.telecom.PhoneAccountHandle;

import com.google.common.base.Optional;

import javax.inject.Inject;

import app.diol.dialer.preferredsim.suggestion.SuggestionProvider;

/**
 * {@link SuggestionProvider} that does nothing.
 */
public class StubSuggestionProvider implements SuggestionProvider {

    @Inject
    public StubSuggestionProvider() {
    }

    @WorkerThread
    @Override
    public Optional<Suggestion> getSuggestion(Context context, String number) {
        return Optional.absent();
    }

    @Override
    public void reportUserSelection(
            @NonNull Context context,
            @NonNull String number,
            @NonNull PhoneAccountHandle phoneAccountHandle,
            boolean rememberSelection) {
    }

    @Override
    public void reportIncorrectSuggestion(
            @NonNull Context context, @NonNull String number, PhoneAccountHandle newAccount) {
    }
}
