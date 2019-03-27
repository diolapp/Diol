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

package app.diol.dialer.calllogutils;

import android.text.TextUtils;

import app.diol.dialer.NumberAttributes;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.consolidator.PhoneLookupInfoConsolidator;

/**
 * Builds {@link NumberAttributes} from other data types.
 */
public final class NumberAttributesBuilder {

    /**
     * Returns a {@link NumberAttributes.Builder} with info from {@link PhoneLookupInfo}.
     */
    public static NumberAttributes.Builder fromPhoneLookupInfo(PhoneLookupInfo phoneLookupInfo) {
        PhoneLookupInfoConsolidator phoneLookupInfoConsolidator =
                new PhoneLookupInfoConsolidator(phoneLookupInfo);
        return NumberAttributes.newBuilder()
                .setName(phoneLookupInfoConsolidator.getName())
                .setPhotoUri(
                        !TextUtils.isEmpty(phoneLookupInfoConsolidator.getPhotoThumbnailUri())
                                ? phoneLookupInfoConsolidator.getPhotoThumbnailUri()
                                : phoneLookupInfoConsolidator.getPhotoUri())
                .setPhotoId(phoneLookupInfoConsolidator.getPhotoId())
                .setLookupUri(phoneLookupInfoConsolidator.getLookupUri())
                .setNumberTypeLabel(phoneLookupInfoConsolidator.getNumberLabel())
                .setIsBusiness(phoneLookupInfoConsolidator.isBusiness())
                .setIsBlocked(phoneLookupInfoConsolidator.isBlocked())
                .setIsSpam(phoneLookupInfoConsolidator.isSpam())
                .setCanReportAsInvalidNumber(phoneLookupInfoConsolidator.canReportAsInvalidNumber())
                .setIsCp2InfoIncomplete(phoneLookupInfoConsolidator.isDefaultCp2InfoIncomplete())
                .setContactSource(phoneLookupInfoConsolidator.getContactSource())
                .setCanSupportCarrierVideoCall(phoneLookupInfoConsolidator.canSupportCarrierVideoCall())
                .setGeolocation(phoneLookupInfoConsolidator.getGeolocation())
                .setIsEmergencyNumber(phoneLookupInfoConsolidator.isEmergencyNumber());
    }
}
