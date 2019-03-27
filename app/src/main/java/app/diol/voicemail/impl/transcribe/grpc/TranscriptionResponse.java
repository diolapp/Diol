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
package app.diol.voicemail.impl.transcribe.grpc;

import android.support.annotation.Nullable;

import app.diol.dialer.common.Assert;
import io.grpc.Status;
import io.grpc.Status.Code;

/**
 * Base class for encapulating a voicemail transcription server response. This
 * handles the Grpc status response, subclasses will handle request specific
 * responses.
 */
public abstract class TranscriptionResponse {
    @Nullable
    public final Status status;

    TranscriptionResponse() {
        this.status = null;
    }

    TranscriptionResponse(Status status) {
        Assert.checkArgument(status != null);
        this.status = status;
    }

    public boolean hasRecoverableError() {
        if (status != null) {
            return status.getCode() == Status.Code.UNAVAILABLE;
        }

        return false;
    }

    public boolean isStatusAlreadyExists() {
        if (status != null) {
            return status.getCode() == Code.ALREADY_EXISTS;
        }

        return false;
    }

    public boolean hasFatalError() {
        if (status != null) {
            return status.getCode() != Status.Code.OK && status.getCode() != Status.Code.UNAVAILABLE;
        }

        return false;
    }

    @Override
    public String toString() {
        return "status: " + status;
    }
}
