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

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;

import app.diol.R;
import app.diol.contacts.common.ContactsUtils.UserType;
import app.diol.contacts.common.util.ContactDisplayUtils;
import app.diol.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder;
import app.diol.dialer.logging.ContactSource;
import app.diol.dialer.phonenumbercache.ContactInfo;

/**
 * The details of a phone call to be shown in the UI.
 */
public class PhoneCallDetails {

    // The number of the other party involved in the call.
    public CharSequence number;
    // Post-dial digits associated with the outgoing call.
    public String postDialDigits;
    // The secondary line number the call was received via.
    public String viaNumber;
    // The number presenting rules set by the network, e.g., {@link Calls#PRESENTATION_ALLOWED}
    public int numberPresentation;
    // The country corresponding with the phone number.
    public String countryIso;
    // The geocoded location for the phone number.
    public String geocode;

    /**
     * The type of calls, as defined in the call log table, e.g., {@link Calls#INCOMING_TYPE}.
     *
     * <p>There might be multiple types if this represents a set of entries grouped together.
     */
    public int[] callTypes;

    // The date of the call, in milliseconds since the epoch.
    public long date;
    // The duration of the call in milliseconds, or 0 for missed calls.
    public long duration;
    // The name of the contact, or the empty string.
    public CharSequence namePrimary;
    // The alternative name of the contact, e.g. last name first, or the empty string
    public CharSequence nameAlternative;
    /**
     * The user's preference on name display order, last name first or first time first. {@see
     * ContactsPreferences}
     */
    public DisplayOrder nameDisplayOrder;
    // The type of phone, e.g., {@link Phone#TYPE_HOME}, 0 if not available.
    public int numberType;
    // The custom label associated with the phone number in the contact, or the empty string.
    public CharSequence numberLabel;
    // The URI of the contact associated with this phone call.
    public Uri contactUri;

    /**
     * The photo URI of the picture of the contact that is associated with this phone call or null if
     * there is none.
     *
     * <p>This is meant to store the high-res photo only.
     */
    public Uri photoUri;

    // The source type of the contact associated with this call.
    public ContactSource.Type sourceType;

    // The object id type of the contact associated with this call.
    public String objectId;

    // The unique identifier for the account associated with the call.
    public PhoneAccountHandle accountHandle;

    // Features applicable to this call.
    public int features;

    // Total data usage for this call.
    public Long dataUsage;

    // Voicemail transcription
    public String transcription;

    // Voicemail transcription state, ie. in-progress, failed, etc.
    public int transcriptionState;

    // The display string for the number.
    public String displayNumber;

    // Whether the contact number is a voicemail number.
    public boolean isVoicemail;

    /**
     * The {@link UserType} of the contact
     */
    public @UserType
    long contactUserType;

    /**
     * If this is a voicemail, whether the message is read. For other types of calls, this defaults to
     * {@code true}.
     */
    public boolean isRead = true;

    // If this call is a spam number.
    public boolean isSpam = false;

    // If this call is a blocked number.
    public boolean isBlocked = false;

    // Call location and date text.
    public CharSequence callLocationAndDate;

    // Call description.
    public CharSequence callDescription;
    public String accountComponentName;
    public String accountId;
    public ContactInfo cachedContactInfo;
    public int voicemailId;
    public int previousGroup;

    // The URI of the voicemail associated with this phone call, if this call went to voicemail.
    public String voicemailUri;

    /**
     * Constructor with required fields for the details of a call with a number associated with a
     * contact.
     */
    public PhoneCallDetails(
            CharSequence number, int numberPresentation, CharSequence postDialDigits) {
        this.number = number;
        this.numberPresentation = numberPresentation;
        this.postDialDigits = postDialDigits.toString();
    }

    /**
     * Construct the "on {accountLabel} via {viaNumber}" accessibility description for the account
     * list item, depending on the existence of the accountLabel and viaNumber.
     *
     * @param viaNumber    The number that this call is being placed via.
     * @param accountLabel The {@link PhoneAccount} label that this call is being placed with.
     * @return The description of the account that this call has been placed on.
     */
    public static CharSequence createAccountLabelDescription(
            Resources resources, @Nullable String viaNumber, @Nullable CharSequence accountLabel) {

        if ((!TextUtils.isEmpty(viaNumber)) && !TextUtils.isEmpty(accountLabel)) {
            String msg =
                    resources.getString(
                            R.string.description_via_number_phone_account, accountLabel, viaNumber);
            CharSequence accountNumberLabel =
                    ContactDisplayUtils.getTelephoneTtsSpannable(msg, viaNumber);
            return (accountNumberLabel == null) ? msg : accountNumberLabel;
        } else if (!TextUtils.isEmpty(viaNumber)) {
            CharSequence viaNumberLabel =
                    ContactDisplayUtils.getTtsSpannedPhoneNumber(
                            resources, R.string.description_via_number, viaNumber);
            return (viaNumberLabel == null) ? viaNumber : viaNumberLabel;
        } else if (!TextUtils.isEmpty(accountLabel)) {
            return TextUtils.expandTemplate(
                    resources.getString(R.string.description_phone_account), accountLabel);
        }
        return "";
    }

    /**
     * Returns the preferred name for the call details as specified by the {@link #nameDisplayOrder}
     *
     * @return the preferred name
     */
    public CharSequence getPreferredName() {
        if (nameDisplayOrder == DisplayOrder.PRIMARY || TextUtils.isEmpty(nameAlternative)) {
            return namePrimary;
        }
        return nameAlternative;
    }

    public void updateDisplayNumber(
            Context context, CharSequence formattedNumber, boolean isVoicemail) {
        displayNumber =
                PhoneNumberDisplayUtil.getDisplayNumber(
                        context, number, numberPresentation, formattedNumber, postDialDigits, isVoicemail)
                        .toString();
    }

    public boolean hasIncomingCalls() {
        for (int i = 0; i < callTypes.length; i++) {
            if (callTypes[i] == CallLog.Calls.INCOMING_TYPE
                    || callTypes[i] == CallLog.Calls.MISSED_TYPE
                    || callTypes[i] == CallLog.Calls.VOICEMAIL_TYPE
                    || callTypes[i] == CallLog.Calls.REJECTED_TYPE
                    || callTypes[i] == CallLog.Calls.BLOCKED_TYPE) {
                return true;
            }
        }
        return false;
    }
}
