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

package app.diol.dialer.enrichedcall.extensions;

import android.support.annotation.NonNull;

import app.diol.dialer.common.Assert;
import app.diol.dialer.enrichedcall.Session;
import app.diol.dialer.enrichedcall.Session.State;

/**
 * Extends the {@link State} to include a toString method.
 */
public class StateExtension {

    /**
     * Returns the string representation for the given {@link State}.
     */
    @NonNull
    public static String toString(@State int callComposerState) {
        if (callComposerState == Session.STATE_NONE) {
            return "STATE_NONE";
        }
        if (callComposerState == Session.STATE_STARTING) {
            return "STATE_STARTING";
        }
        if (callComposerState == Session.STATE_STARTED) {
            return "STATE_STARTED";
        }
        if (callComposerState == Session.STATE_START_FAILED) {
            return "STATE_START_FAILED";
        }
        if (callComposerState == Session.STATE_MESSAGE_SENT) {
            return "STATE_MESSAGE_SENT";
        }
        if (callComposerState == Session.STATE_MESSAGE_FAILED) {
            return "STATE_MESSAGE_FAILED";
        }
        if (callComposerState == Session.STATE_CLOSED) {
            return "STATE_CLOSED";
        }
        Assert.checkArgument(false, "Unexpected callComposerState: %d", callComposerState);
        return null;
    }
}
