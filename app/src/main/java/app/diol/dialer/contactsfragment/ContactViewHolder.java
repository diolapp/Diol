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

package app.diol.dialer.contactsfragment;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.contactsfragment.ContactsFragment.OnContactSelectedListener;
import app.diol.dialer.logging.InteractionEvent;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.widget.BidiTextView;

/**
 * View holder for a contact.
 */
final class ContactViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final TextView header;
    private final BidiTextView name;
    private final QuickContactBadge photo;
    private final Context context;
    private final OnContactSelectedListener onContactSelectedListener;

    private String headerText;
    private Uri contactUri;
    private long contactId;

    ContactViewHolder(View itemView, OnContactSelectedListener onContactSelectedListener) {
        super(itemView);
        this.onContactSelectedListener = Assert.isNotNull(onContactSelectedListener);
        context = itemView.getContext();
        itemView.findViewById(R.id.click_target).setOnClickListener(this);
        header = itemView.findViewById(R.id.header);
        name = itemView.findViewById(R.id.contact_name);
        photo = itemView.findViewById(R.id.photo);
    }

    /**
     * Binds the ViewHolder with relevant data.
     *
     * @param headerText  populates the header view.
     * @param displayName populates the name view.
     * @param contactUri  to be shown by the contact card on photo click.
     * @param showHeader  if header view should be shown {@code True}, {@code False} otherwise.
     */
    public void bind(
            String headerText, String displayName, Uri contactUri, long contactId, boolean showHeader) {
        Assert.checkArgument(!TextUtils.isEmpty(displayName));
        this.contactUri = contactUri;
        this.contactId = contactId;
        this.headerText = headerText;

        name.setText(displayName);
        header.setText(headerText);
        header.setVisibility(showHeader ? View.VISIBLE : View.INVISIBLE);

        Logger.get(context)
                .logQuickContactOnTouch(
                        photo, InteractionEvent.Type.OPEN_QUICK_CONTACT_FROM_CONTACTS_FRAGMENT_BADGE, true);
    }

    public QuickContactBadge getPhoto() {
        return photo;
    }

    public String getHeader() {
        return headerText;
    }

    public TextView getHeaderView() {
        return header;
    }

    @Override
    public void onClick(View v) {
        onContactSelectedListener.onContactSelected(photo, contactUri, contactId);
    }
}
