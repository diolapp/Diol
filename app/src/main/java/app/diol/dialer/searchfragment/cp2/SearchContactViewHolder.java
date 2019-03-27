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

package app.diol.dialer.searchfragment.cp2;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.QuickContactBadge;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.contactphoto.ContactPhotoManager;
import app.diol.dialer.dialercontact.DialerContact;
import app.diol.dialer.duo.DuoComponent;
import app.diol.dialer.enrichedcall.EnrichedCallCapabilities;
import app.diol.dialer.enrichedcall.EnrichedCallComponent;
import app.diol.dialer.enrichedcall.EnrichedCallManager;
import app.diol.dialer.lettertile.LetterTileDrawable;
import app.diol.dialer.searchfragment.common.Projections;
import app.diol.dialer.searchfragment.common.QueryBoldingUtil;
import app.diol.dialer.searchfragment.common.RowClickListener;
import app.diol.dialer.searchfragment.common.SearchCursor;
import app.diol.dialer.widget.BidiTextView;

/**
 * ViewHolder for a contact row.
 */
public final class SearchContactViewHolder extends ViewHolder implements OnClickListener {

    private final RowClickListener listener;
    private final QuickContactBadge photo;
    private final BidiTextView nameOrNumberView;
    private final BidiTextView numberView;
    private final ImageView callToActionView;
    private final Context context;
    private int position;
    private String number;
    private DialerContact dialerContact;
    private @CallToAction
    int currentAction;
    public SearchContactViewHolder(View view, RowClickListener listener) {
        super(view);
        this.listener = listener;
        view.setOnClickListener(this);
        photo = view.findViewById(R.id.photo);
        nameOrNumberView = view.findViewById(R.id.primary);
        numberView = view.findViewById(R.id.secondary);
        callToActionView = view.findViewById(R.id.call_to_action);
        context = view.getContext();
    }

    private static Uri getContactUri(Cursor cursor) {
        long contactId = cursor.getLong(Projections.ID);
        String lookupKey = cursor.getString(Projections.LOOKUP_KEY);
        return Contacts.getLookupUri(contactId, lookupKey);
    }

    // TODO(calderwoodra): handle CNAP and cequint types.
    // TODO(calderwoodra): unify this into a utility method with CallLogAdapter#getNumberType
    private static String getLabel(Resources resources, Cursor cursor) {
        int numberType = cursor.getInt(Projections.PHONE_TYPE);
        String numberLabel = cursor.getString(Projections.PHONE_LABEL);

        // Returns empty label instead of "custom" if the custom label is empty.
        if (numberType == Phone.TYPE_CUSTOM && TextUtils.isEmpty(numberLabel)) {
            return "";
        }
        return (String) Phone.getTypeLabel(resources, numberType, numberLabel);
    }

    private static @CallToAction
    int getCallToAction(
            Context context, SearchCursor cursor, String query) {
        int carrierPresence = cursor.getInt(Projections.CARRIER_PRESENCE);
        String number = cursor.getString(Projections.PHONE_NUMBER);
        if ((carrierPresence & Phone.CARRIER_PRESENCE_VT_CAPABLE) == 1) {
            return CallToAction.VIDEO_CALL;
        }

        if (DuoComponent.get(context).getDuo().isReachable(context, number)) {
            return CallToAction.DUO_CALL;
        }

        EnrichedCallManager manager = EnrichedCallComponent.get(context).getEnrichedCallManager();
        EnrichedCallCapabilities capabilities = manager.getCapabilities(number);
        if (capabilities != null && capabilities.isCallComposerCapable()) {
            return CallToAction.SHARE_AND_CALL;
        } else if (shouldRequestCapabilities(cursor, capabilities, query)) {
            manager.requestCapabilities(number);
        }
        return CallToAction.NONE;
    }

    /**
     * An RPC is initiated for each number we request capabilities for, so to limit the network load
     * and latency on slow networks, we only want to request capabilities for potential contacts the
     * user is interested in calling. The requirements are that:
     *
     * <ul>
     * <li>The search query must be 3 or more characters; OR
     * <li>There must be 4 or fewer contacts listed in the cursor.
     * </ul>
     */
    private static boolean shouldRequestCapabilities(
            SearchCursor cursor,
            @Nullable EnrichedCallCapabilities capabilities,
            @Nullable String query) {
        if (capabilities != null) {
            return false;
        }

        if (query != null && query.length() >= 3) {
            return true;
        }

        // TODO(calderwoodra): implement SearchCursor#getHeaderCount
        if (cursor.getCount() <= 5) { // 4 contacts + 1 header row element
            return true;
        }
        return false;
    }

    private static DialerContact getDialerContact(Context context, Cursor cursor) {
        DialerContact.Builder contact = DialerContact.newBuilder();
        String displayName = cursor.getString(Projections.DISPLAY_NAME);
        String number = cursor.getString(Projections.PHONE_NUMBER);
        Uri contactUri =
                Contacts.getLookupUri(
                        cursor.getLong(Projections.CONTACT_ID), cursor.getString(Projections.LOOKUP_KEY));

        contact
                .setNumber(number)
                .setPhotoId(cursor.getLong(Projections.PHOTO_ID))
                .setContactType(LetterTileDrawable.TYPE_DEFAULT)
                .setNameOrNumber(displayName)
                .setNumberLabel(
                        Phone.getTypeLabel(
                                context.getResources(),
                                cursor.getInt(Projections.PHONE_TYPE),
                                cursor.getString(Projections.PHONE_LABEL))
                                .toString());

        String photoUri = cursor.getString(Projections.PHOTO_URI);
        if (photoUri != null) {
            contact.setPhotoUri(photoUri);
        }

        if (contactUri != null) {
            contact.setContactUri(contactUri.toString());
        }

        if (!TextUtils.isEmpty(displayName)) {
            contact.setDisplayNumber(number);
        }

        return contact.build();
    }

    /**
     * Binds the ViewHolder with a cursor from {@link SearchContactsCursorLoader} with the data found
     * at the cursors set position.
     */
    public void bind(SearchCursor cursor, String query) {
        dialerContact = getDialerContact(context, cursor);
        position = cursor.getPosition();
        number = cursor.getString(Projections.PHONE_NUMBER);
        String name = cursor.getString(Projections.DISPLAY_NAME);
        String label = getLabel(context.getResources(), cursor);
        String secondaryInfo =
                TextUtils.isEmpty(label)
                        ? number
                        : context.getString(
                        R.string.call_subject_type_and_number,
                        label,
                        number);

        nameOrNumberView.setText(QueryBoldingUtil.getNameWithQueryBolded(query, name, context));
        numberView.setText(QueryBoldingUtil.getNumberWithQueryBolded(query, secondaryInfo));
        setCallToAction(cursor, query);

        if (shouldShowPhoto(cursor)) {
            nameOrNumberView.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);
            String photoUri = cursor.getString(Projections.PHOTO_URI);
            ContactPhotoManager.getInstance(context)
                    .loadDialerThumbnailOrPhoto(
                            photo,
                            getContactUri(cursor),
                            cursor.getLong(Projections.PHOTO_ID),
                            photoUri == null ? null : Uri.parse(photoUri),
                            name,
                            LetterTileDrawable.TYPE_DEFAULT);
        } else {
            nameOrNumberView.setVisibility(View.GONE);
            photo.setVisibility(View.INVISIBLE);
        }
    }

    // Show the contact photo next to only the first number if a contact has multiple numbers
    private boolean shouldShowPhoto(SearchCursor cursor) {
        int currentPosition = cursor.getPosition();
        String currentLookupKey = cursor.getString(Projections.LOOKUP_KEY);
        cursor.moveToPosition(currentPosition - 1);

        if (!cursor.isHeader() && !cursor.isBeforeFirst()) {
            String previousLookupKey = cursor.getString(Projections.LOOKUP_KEY);
            cursor.moveToPosition(currentPosition);
            return !currentLookupKey.equals(previousLookupKey);
        }
        cursor.moveToPosition(currentPosition);
        return true;
    }

    private void setCallToAction(SearchCursor cursor, String query) {
        currentAction = getCallToAction(context, cursor, query);
        switch (currentAction) {
            case CallToAction.NONE:
                callToActionView.setVisibility(View.GONE);
                callToActionView.setOnClickListener(null);
                break;
            case CallToAction.SHARE_AND_CALL:
                callToActionView.setVisibility(View.VISIBLE);
                callToActionView.setImageDrawable(
                        context.getDrawable(R.drawable.ic_phone_attach));
                callToActionView.setContentDescription(
                        context.getString(R.string.description_search_call_and_share));
                callToActionView.setOnClickListener(this);
                break;
            case CallToAction.DUO_CALL:
            case CallToAction.VIDEO_CALL:
                callToActionView.setVisibility(View.VISIBLE);
                callToActionView.setImageDrawable(
                        context.getDrawable(R.drawable.quantum_ic_videocam_vd_white_24));
                callToActionView.setContentDescription(
                        context.getString(R.string.description_search_video_call));
                callToActionView.setOnClickListener(this);
                break;
            default:
                throw Assert.createIllegalStateFailException(
                        "Invalid Call to action type: " + currentAction);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == callToActionView) {
            switch (currentAction) {
                case CallToAction.SHARE_AND_CALL:
                    listener.openCallAndShare(dialerContact);
                    break;
                case CallToAction.VIDEO_CALL:
                    listener.placeVideoCall(number, position);
                    break;
                case CallToAction.DUO_CALL:
                    listener.placeDuoCall(number);
                    break;
                case CallToAction.NONE:
                default:
                    throw Assert.createIllegalStateFailException(
                            "Invalid Call to action type: " + currentAction);
            }
        } else {
            listener.placeVoiceCall(number, position);
        }
    }

    /**
     * IntDef for the different types of actions that can be shown.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            CallToAction.NONE,
            CallToAction.VIDEO_CALL,
            CallToAction.DUO_CALL,
            CallToAction.SHARE_AND_CALL
    })
    @interface CallToAction {
        int NONE = 0;
        int VIDEO_CALL = 1;
        int DUO_CALL = 2;
        int SHARE_AND_CALL = 3;
    }
}
