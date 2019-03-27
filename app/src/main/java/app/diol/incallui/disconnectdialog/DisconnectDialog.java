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

package app.diol.incallui.disconnectdialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.telecom.DisconnectCause;
import android.util.Pair;

import app.diol.incallui.call.DialerCall;

/**
 * Interface for disconnect dialog.
 */
public interface DisconnectDialog {

    boolean shouldShow(DisconnectCause disconnectCause);

    Pair<Dialog, CharSequence> createDialog(@NonNull Context context, DialerCall call);
}
