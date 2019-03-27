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

package app.diol.incallui.bindings;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * This interface allows the container application to customize the in call UI.
 */
public interface InCallUiBindings {

    @Nullable
    PhoneNumberService newPhoneNumberService(Context context);

    /**
     * @return An {@link Intent} to be broadcast when the call state button in the InCallUI is touched
     * while in a call.
     */
    @Nullable
    Intent getCallStateButtonBroadcastIntent(Context context);
}
