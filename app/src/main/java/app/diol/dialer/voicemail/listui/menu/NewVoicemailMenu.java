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

package app.diol.dialer.voicemail.listui.menu;

import android.content.Context;
import android.view.View;

import app.diol.dialer.historyitemactions.HistoryItemActionBottomSheet;
import app.diol.dialer.voicemail.model.VoicemailEntry;

/**
 * Handles configuration of the bottom sheet menus for voicemail entries.
 */
public final class NewVoicemailMenu {

    /**
     * Creates and returns the OnClickListener which opens the menu for the provided row.
     */
    public static View.OnClickListener createOnClickListener(
            Context context, VoicemailEntry voicemailEntry) {
        return (view) ->
                HistoryItemActionBottomSheet.show(
                        context,
                        BottomSheetHeader.fromVoicemailEntry(voicemailEntry),
                        Modules.fromVoicemailEntry(context, voicemailEntry));
    }
}
