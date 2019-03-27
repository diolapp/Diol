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

package app.diol.dialer.app.calllog;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;

import java.util.ArrayList;

import app.diol.contacts.common.model.Contact;
import app.diol.contacts.common.model.ContactLoader;
import app.diol.dialer.calldetails.CallDetailsEntries;
import app.diol.dialer.calldetails.OldCallDetailsActivity;
import app.diol.dialer.callintent.CallInitiationType;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.dialercontact.DialerContact;
import app.diol.dialer.duo.DuoComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.precall.PreCall;
import app.diol.dialer.util.IntentUtil;

/**
 * Used to create an intent to attach to an action in the call log.
 *
 * <p>The intent is constructed lazily with the given information.
 */
public abstract class IntentProvider {

    private static final String TAG = IntentProvider.class.getSimpleName();

    public static IntentProvider getReturnCallIntentProvider(final String number) {
        return getReturnCallIntentProvider(number, null);
    }

    public static IntentProvider getReturnCallIntentProvider(
            final String number, final PhoneAccountHandle accountHandle) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return PreCall.getIntent(
                        context,
                        new CallIntentBuilder(number, CallInitiationType.Type.CALL_LOG)
                                .setPhoneAccountHandle(accountHandle));
            }
        };
    }

    public static IntentProvider getAssistedDialIntentProvider(
            final String number, final Context context, final TelephonyManager telephonyManager) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return PreCall.getIntent(
                        context,
                        new CallIntentBuilder(number, CallInitiationType.Type.CALL_LOG)
                                .setAllowAssistedDial(true));
            }
        };
    }

    public static IntentProvider getReturnVideoCallIntentProvider(final String number) {
        return getReturnVideoCallIntentProvider(number, null);
    }

    public static IntentProvider getReturnVideoCallIntentProvider(
            final String number, final PhoneAccountHandle accountHandle) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return PreCall.getIntent(
                        context,
                        new CallIntentBuilder(number, CallInitiationType.Type.CALL_LOG)
                                .setPhoneAccountHandle(accountHandle)
                                .setIsVideoCall(true));
            }
        };
    }

    public static IntentProvider getDuoVideoIntentProvider(String number, boolean isNonContact) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return PreCall.getIntent(
                        context,
                        new CallIntentBuilder(number, CallInitiationType.Type.CALL_LOG)
                                .setIsDuoCall(true)
                                .setIsVideoCall(true));
            }

            @Override
            public void logInteraction(Context context) {
                Logger.get(context)
                        .logImpression(DialerImpression.Type.LIGHTBRINGER_VIDEO_REQUESTED_FROM_CALL_LOG);
                if (isNonContact) {
                    Logger.get(context)
                            .logImpression(
                                    DialerImpression.Type.LIGHTBRINGER_NON_CONTACT_VIDEO_REQUESTED_FROM_CALL_LOG);
                }
            }
        };
    }

    public static IntentProvider getInstallDuoIntentProvider() {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return DuoComponent.get(context).getDuo().getInstallDuoIntent().orNull();
            }

            @Override
            public void logInteraction(Context context) {
                Logger.get(context).logImpression(DialerImpression.Type.DUO_CALL_LOG_SET_UP_INSTALL);
            }
        };
    }

    public static IntentProvider getSetUpDuoIntentProvider() {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return DuoComponent.get(context).getDuo().getActivateIntent().orNull();
            }

            @Override
            public void logInteraction(Context context) {
                Logger.get(context).logImpression(DialerImpression.Type.DUO_CALL_LOG_SET_UP_ACTIVATE);
            }
        };
    }

    public static IntentProvider getDuoInviteIntentProvider(String number) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return DuoComponent.get(context).getDuo().getInviteIntent(number).orNull();
            }

            @Override
            public void logInteraction(Context context) {
                Logger.get(context).logImpression(DialerImpression.Type.DUO_CALL_LOG_INVITE);
            }
        };
    }

    public static IntentProvider getReturnVoicemailCallIntentProvider(
            @Nullable PhoneAccountHandle phoneAccountHandle) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return PreCall.getIntent(
                        context,
                        CallIntentBuilder.forVoicemail(phoneAccountHandle, CallInitiationType.Type.CALL_LOG));
            }
        };
    }

    public static IntentProvider getSendSmsIntentProvider(final String number) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return IntentUtil.getSendSmsIntent(number);
            }
        };
    }

    /**
     * Retrieves the call details intent provider for an entry in the call log.
     *
     * @param callDetailsEntries        The call details of the other calls grouped together with the call.
     * @param contact                   The contact with which this call details intent pertains to.
     * @param canReportCallerId         Whether reporting a caller ID is supported.
     * @param canSupportAssistedDialing Whether assisted dialing is supported.
     * @return The call details intent provider.
     */
    public static IntentProvider getCallDetailIntentProvider(
            CallDetailsEntries callDetailsEntries,
            DialerContact contact,
            boolean canReportCallerId,
            boolean canSupportAssistedDialing) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                return OldCallDetailsActivity.newInstance(
                        context, callDetailsEntries, contact, canReportCallerId, canSupportAssistedDialing);
            }
        };
    }

    /**
     * Retrieves an add contact intent for the given contact and phone call details.
     */
    public static IntentProvider getAddContactIntentProvider(
            final Uri lookupUri,
            final CharSequence name,
            final CharSequence number,
            final int numberType,
            final boolean isNewContact) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                Contact contactToSave = null;

                if (lookupUri != null) {
                    contactToSave = ContactLoader.parseEncodedContactEntity(lookupUri);
                }

                if (contactToSave != null) {
                    // Populate the intent with contact information stored in the lookup URI.
                    // Note: This code mirrors code in Contacts/QuickContactsActivity.
                    final Intent intent;
                    if (isNewContact) {
                        intent = IntentUtil.getNewContactIntent();
                    } else {
                        intent = IntentUtil.getAddToExistingContactIntent();
                    }

                    ArrayList<ContentValues> values = contactToSave.getContentValues();
                    // Only pre-fill the name field if the provided display name is an nickname
                    // or better (e.g. structured name, nickname)
                    if (contactToSave.getDisplayNameSource()
                            >= ContactsContract.DisplayNameSources.NICKNAME) {
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, contactToSave.getDisplayName());
                    } else if (contactToSave.getDisplayNameSource()
                            == ContactsContract.DisplayNameSources.ORGANIZATION) {
                        // This is probably an organization. Instead of copying the organization
                        // name into a name entry, copy it into the organization entry. This
                        // way we will still consider the contact an organization.
                        final ContentValues organization = new ContentValues();
                        organization.put(
                                ContactsContract.CommonDataKinds.Organization.COMPANY,
                                contactToSave.getDisplayName());
                        organization.put(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
                        values.add(organization);
                    }

                    // Last time used and times used are aggregated values from the usage stat
                    // table. They need to be removed from data values so the SQL table can insert
                    // properly
                    for (ContentValues value : values) {
                        value.remove(ContactsContract.Data.LAST_TIME_USED);
                        value.remove(ContactsContract.Data.TIMES_USED);
                    }

                    intent.putExtra(ContactsContract.Intents.Insert.DATA, values);

                    return intent;
                } else {
                    // If no lookup uri is provided, rely on the available phone number and name.
                    if (isNewContact) {
                        return IntentUtil.getNewContactIntent(name, number, numberType);
                    } else {
                        return IntentUtil.getAddToExistingContactIntent(name, number, numberType);
                    }
                }
            }
        };
    }

    public abstract Intent getIntent(Context context);

    public void logInteraction(Context context) {
    }
}
