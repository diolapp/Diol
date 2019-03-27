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

package app.diol.voicemail.stub;

import android.content.Context;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;

import java.util.List;

import javax.inject.Inject;

import app.diol.dialer.common.Assert;
import app.diol.voicemail.PinChanger;
import app.diol.voicemail.VoicemailClient;

/**
 * A no-op version of the voicemail module for build targets that don't support the new OTMP client.
 */
public final class StubVoicemailClient implements VoicemailClient {
    @Inject
    public StubVoicemailClient() {
    }

    @Override
    public boolean isVoicemailModuleEnabled() {
        return false;
    }

    @Override
    public boolean isVoicemailEnabled(Context context, PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @Override
    public void setVoicemailEnabled(
            Context context, PhoneAccountHandle phoneAccountHandle, boolean enabled) {
    }

    @Override
    public void appendOmtpVoicemailSelectionClause(
            Context context, StringBuilder where, List<String> selectionArgs) {
    }

    @Override
    public void appendOmtpVoicemailStatusSelectionClause(
            Context context, StringBuilder where, List<String> selectionArgs) {
    }

    @Override
    public boolean isVoicemailArchiveEnabled(Context context, PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @Override
    public boolean isVoicemailArchiveAvailable(Context context) {
        return false;
    }

    @Override
    public void setVoicemailArchiveEnabled(
            Context context, PhoneAccountHandle phoneAccountHandle, boolean value) {
    }

    @Override
    public boolean isVoicemailTranscriptionAvailable(
            Context context, PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @Override
    public boolean isVoicemailTranscriptionEnabled(Context context, PhoneAccountHandle account) {
        return false;
    }

    @Override
    public boolean isVoicemailDonationAvailable(Context context, PhoneAccountHandle account) {
        return false;
    }

    @Override
    public boolean isVoicemailDonationEnabled(Context context, PhoneAccountHandle account) {
        return false;
    }

    @Override
    public void setVoicemailTranscriptionEnabled(
            Context context, PhoneAccountHandle phoneAccountHandle, boolean enabled) {
    }

    @Override
    public void setVoicemailDonationEnabled(
            Context context, PhoneAccountHandle phoneAccountHandle, boolean enabled) {
    }

    @Override
    public boolean isActivated(Context context, PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @Override
    public void showConfigUi(@NonNull Context context) {
    }

    @Override
    public PersistableBundle getConfig(
            @NonNull Context context, @Nullable PhoneAccountHandle phoneAccountHandle) {
        return new PersistableBundle();
    }

    @Override
    public void onBoot(@NonNull Context context) {
    }

    @Override
    public void onShutdown(@NonNull Context context) {
    }

    @Override
    public void addActivationStateListener(ActivationStateListener listener) {
        // Do nothing
    }

    @Override
    public void removeActivationStateListener(ActivationStateListener listener) {
        // Do nothing
    }

    @Override
    public boolean hasCarrierSupport(Context context, PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @Override
    public PinChanger createPinChanger(Context context, PhoneAccountHandle phoneAccountHandle) {
        throw Assert.createAssertionFailException("should never be called on stub.");
    }

    @Override
    public void onTosAccepted(Context context, PhoneAccountHandle account) {
    }

    @Override
    public boolean hasAcceptedTos(Context context, PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @Override
    @Nullable
    public String getCarrierConfigString(Context context, PhoneAccountHandle account, String key) {
        return null;
    }
}
