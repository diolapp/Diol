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

package app.diol.dialer.voicemail.listui.error;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;

import app.diol.voicemail.VisualVoicemailTypeExtensions;

/**
 * Given a VoicemailStatus, {@link VoicemailErrorMessageCreator#create(Context, VoicemailStatus)}
 * will return a {@link VoicemailErrorMessage} representing the message to be shown to the user, or
 * <code>null</code> if no message should be shown.
 */
public class VoicemailErrorMessageCreator {

    @Nullable
    public VoicemailErrorMessage create(
            Context context, VoicemailStatus status, VoicemailStatusReader statusReader) {
        // Never return error message before NMR1. Voicemail status is not supported on those.
        if (VERSION.SDK_INT < VERSION_CODES.N_MR1) {
            return null;
        }
        switch (status.type) {
            case VisualVoicemailTypeExtensions.VVM_TYPE_VVM3:
                return Vvm3VoicemailMessageCreator.create(context, status, statusReader);
            default:
                return OmtpVoicemailMessageCreator.create(context, status, statusReader);
        }
    }

    public boolean isSyncBlockingError(VoicemailStatus status) {
        switch (status.type) {
            case VisualVoicemailTypeExtensions.VVM_TYPE_VVM3:
                return Vvm3VoicemailMessageCreator.isSyncBlockingError(status);
            default:
                return OmtpVoicemailMessageCreator.isSyncBlockingError(status);
        }
    }
}
