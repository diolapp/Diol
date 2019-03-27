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

package app.diol.dialer.calldetails;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import app.diol.dialer.calldetails.CallDetailsEntryViewHolder.CallDetailsEntryListener;
import app.diol.dialer.calldetails.CallDetailsFooterViewHolder.DeleteCallDetailsListener;
import app.diol.dialer.calldetails.CallDetailsHeaderViewHolder.CallDetailsHeaderListener;
import app.diol.dialer.dialercontact.DialerContact;
import app.diol.dialer.glidephotomanager.PhotoInfo;
import app.diol.dialer.lettertile.LetterTileDrawable;

/**
 * A {@link RecyclerView.Adapter} for {@link OldCallDetailsActivity}.
 *
 * <p>See {@link CallDetailsAdapterCommon} for logic shared between this adapter and {@link
 * CallDetailsAdapter}.
 */
final class OldCallDetailsAdapter extends CallDetailsAdapterCommon {

    /**
     * Contains info to be shown in the header.
     */
    private final DialerContact contact;

    OldCallDetailsAdapter(
            Context context,
            DialerContact contact,
            CallDetailsEntries callDetailsEntries,
            CallDetailsEntryListener callDetailsEntryListener,
            CallDetailsHeaderListener callDetailsHeaderListener,
            CallDetailsFooterViewHolder.ReportCallIdListener reportCallIdListener,
            DeleteCallDetailsListener deleteCallDetailsListener) {
        super(
                context,
                callDetailsEntries,
                callDetailsEntryListener,
                callDetailsHeaderListener,
                reportCallIdListener,
                deleteCallDetailsListener);
        this.contact = contact;
    }

    @Override
    protected CallDetailsHeaderViewHolder createCallDetailsHeaderViewHolder(
            View container, CallDetailsHeaderListener callDetailsHeaderListener) {
        return new CallDetailsHeaderViewHolder(
                container, contact.getNumber(), contact.getPostDialDigits(), callDetailsHeaderListener);
    }

    @Override
    protected void bindCallDetailsHeaderViewHolder(
            CallDetailsHeaderViewHolder callDetailsHeaderViewHolder, int position) {
        callDetailsHeaderViewHolder.updateContactInfo(contact, getCallbackAction());
        callDetailsHeaderViewHolder.updateAssistedDialingInfo(
                getCallDetailsEntries().getEntries(position));
    }

    @Override
    protected String getNumber() {
        return contact.getNumber();
    }

    @Override
    protected String getPrimaryText() {
        return contact.getNameOrNumber();
    }

    @Override
    protected PhotoInfo getPhotoInfo() {
        PhotoInfo.Builder builder =
                PhotoInfo.newBuilder()
                        .setPhotoUri(contact.getPhotoUri())
                        .setPhotoId(contact.getPhotoId())
                        .setName(contact.getNameOrNumber())
                        .setLookupUri(contact.getContactUri());
        switch (contact.getContactType()) {
            case LetterTileDrawable.TYPE_VOICEMAIL:
                builder.setIsVoicemail(true);
                break;
            case LetterTileDrawable.TYPE_BUSINESS:
                builder.setIsBusiness(true);
                break;
            case LetterTileDrawable.TYPE_SPAM:
                builder.setIsSpam(true);
                break;
            default: // fall out
        }
        return builder.build();
    }
}
