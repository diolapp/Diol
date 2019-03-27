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
import android.text.TextUtils;

import java.util.List;

import app.diol.dialer.historyitemactions.HistoryItemActionModule;
import app.diol.dialer.historyitemactions.HistoryItemActionModuleInfo;
import app.diol.dialer.historyitemactions.HistoryItemActionModulesBuilder;
import app.diol.dialer.voicemail.model.VoicemailEntry;

/**
 * Configures the modules for the voicemail bottom sheet; these are the rows below the top row
 * (contact info) in the bottom sheet.
 */
final class Modules {

    static List<HistoryItemActionModule> fromVoicemailEntry(
            Context context, VoicemailEntry voicemailEntry) {
        return new HistoryItemActionModulesBuilder(context, buildModuleInfo(voicemailEntry))
                // TODO(uabdullah): add module for calls.
                .addModuleForAddingToContacts()
                .addModuleForSendingTextMessage()
                .addModuleForDivider()
                .addModuleForBlockedOrSpamNumber()
                .addModuleForCopyingNumber()
                // TODO(zachh): Module for CallComposer.
                .build();
    }

    private static HistoryItemActionModuleInfo buildModuleInfo(VoicemailEntry voicemailEntry) {
        return HistoryItemActionModuleInfo.newBuilder()
                .setNormalizedNumber(voicemailEntry.getNumber().getNormalizedNumber())
                .setCountryIso(voicemailEntry.getNumber().getCountryIso())
                .setName(voicemailEntry.getNumberAttributes().getName())
                .setCallType(voicemailEntry.getCallType())
                .setLookupUri(voicemailEntry.getNumberAttributes().getLookupUri())
                .setPhoneAccountComponentName(voicemailEntry.getPhoneAccountComponentName())
                .setCanReportAsInvalidNumber(
                        voicemailEntry.getNumberAttributes().getCanReportAsInvalidNumber())
                .setCanSupportAssistedDialing(
                        !TextUtils.isEmpty(voicemailEntry.getNumberAttributes().getLookupUri()))
                .setCanSupportCarrierVideoCall(
                        voicemailEntry.getNumberAttributes().getCanSupportCarrierVideoCall())
                .setIsBlocked(voicemailEntry.getNumberAttributes().getIsBlocked())
                .setIsEmergencyNumber(voicemailEntry.getNumberAttributes().getIsEmergencyNumber())
                .setIsSpam(voicemailEntry.getNumberAttributes().getIsSpam())
                // A voicemail call is an outgoing call to the voicemail box.
                // Voicemail entries are not voicemail calls.
                .setIsVoicemailCall(false)
                .setContactSource(voicemailEntry.getNumberAttributes().getContactSource())
                .setHost(HistoryItemActionModuleInfo.Host.VOICEMAIL)
                .build();
    }
}
