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

package app.diol.dialer.calllog.ui.menu;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import app.diol.R;
import app.diol.dialer.calldetails.CallDetailsActivity;
import app.diol.dialer.calldetails.CallDetailsHeaderInfo;
import app.diol.dialer.calllog.model.CoalescedRow;
import app.diol.dialer.calllogutils.CallLogEntryText;
import app.diol.dialer.calllogutils.PhotoInfoBuilder;
import app.diol.dialer.historyitemactions.HistoryItemActionModule;
import app.diol.dialer.historyitemactions.HistoryItemActionModuleInfo;
import app.diol.dialer.historyitemactions.HistoryItemActionModulesBuilder;
import app.diol.dialer.historyitemactions.IntentModule;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;

/**
 * Configures the modules for the bottom sheet; these are the rows below the top row (contact info)
 * in the bottom sheet.
 */
final class Modules {

    /**
     * Returns a list of {@link HistoryItemActionModule HistoryItemActionModules}, which are items in
     * the bottom sheet.
     */
    static List<HistoryItemActionModule> fromRow(Context context, CoalescedRow row) {
        HistoryItemActionModulesBuilder modulesBuilder =
                new HistoryItemActionModulesBuilder(context, buildModuleInfo(row));


        // TODO(zachh): Module for CallComposer.

        if (PhoneNumberHelper.canPlaceCallsTo(
                row.getNumber().getNormalizedNumber(), row.getNumberPresentation())) {
            modulesBuilder
                    .addModuleForVoiceCall()
                    .addModuleForVideoCall()
                    .addModuleForSendingTextMessage()
                    .addModuleForDivider()
                    .addModuleForAddingToContacts()
                    .addModuleForBlockedOrSpamNumber()
                    .addModuleForCopyingNumber();
        }

        List<HistoryItemActionModule> modules = modulesBuilder.build();

        // Add modules only available in the call log.
        modules.add(createModuleForAccessingCallDetails(context, row));
        modules.add(new DeleteCallLogItemModule(context, row.getCoalescedIds()));
        return modules;
    }

    private static HistoryItemActionModule createModuleForAccessingCallDetails(
            Context context, CoalescedRow row) {
        boolean canReportAsInvalidNumber =
                !row.getIsVoicemailCall() && row.getNumberAttributes().getCanReportAsInvalidNumber();

        return new IntentModule(
                context,
                CallDetailsActivity.newInstance(
                        context,
                        row.getCoalescedIds(),
                        createCallDetailsHeaderInfoFromRow(context, row),
                        canReportAsInvalidNumber,
                        canSupportAssistedDialing(row)),
                R.string.call_details_menu_label,
                R.drawable.quantum_ic_info_outline_vd_theme_24);
    }

    private static CallDetailsHeaderInfo createCallDetailsHeaderInfoFromRow(
            Context context, CoalescedRow row) {
        return CallDetailsHeaderInfo.newBuilder()
                .setDialerPhoneNumber(row.getNumber())
                .setPhotoInfo(PhotoInfoBuilder.fromCoalescedRow(context, row))
                .setPrimaryText(CallLogEntryText.buildPrimaryText(context, row).toString())
                .setSecondaryText(
                        CallLogEntryText.buildSecondaryTextForBottomSheet(context, row).toString())
                .build();
    }

    private static boolean canSupportAssistedDialing(CoalescedRow row) {
        return !TextUtils.isEmpty(row.getNumberAttributes().getLookupUri());
    }

    private static HistoryItemActionModuleInfo buildModuleInfo(CoalescedRow row) {
        return HistoryItemActionModuleInfo.newBuilder()
                .setNormalizedNumber(row.getNumber().getNormalizedNumber())
                .setCountryIso(row.getNumber().getCountryIso())
                .setName(row.getNumberAttributes().getName())
                .setCallType(row.getCallType())
                .setFeatures(row.getFeatures())
                .setLookupUri(row.getNumberAttributes().getLookupUri())
                .setPhoneAccountComponentName(row.getPhoneAccountComponentName())
                .setCanReportAsInvalidNumber(row.getNumberAttributes().getCanReportAsInvalidNumber())
                .setCanSupportAssistedDialing(canSupportAssistedDialing(row))
                .setCanSupportCarrierVideoCall(row.getNumberAttributes().getCanSupportCarrierVideoCall())
                .setIsBlocked(row.getNumberAttributes().getIsBlocked())
                .setIsEmergencyNumber(row.getNumberAttributes().getIsEmergencyNumber())
                .setIsSpam(row.getNumberAttributes().getIsSpam())
                .setIsVoicemailCall(row.getIsVoicemailCall())
                .setContactSource(row.getNumberAttributes().getContactSource())
                .setHost(HistoryItemActionModuleInfo.Host.CALL_LOG)
                .build();
    }
}
