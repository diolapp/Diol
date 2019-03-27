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
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;

import app.diol.R;
import app.diol.dialer.util.DialerUtils;
import app.diol.dialer.util.IntentUtil;

/**
 * ViewHolder for {@link ContactsFragment} to display add contact row.
 */
final class AddContactViewHolder extends ViewHolder implements OnClickListener {

    private final Context context;

    AddContactViewHolder(View view) {
        super(view);
        view.setOnClickListener(this);
        context = view.getContext();
    }

    @Override
    public void onClick(View v) {
        DialerUtils.startActivityWithErrorToast(
                context, IntentUtil.getNewContactIntent(), R.string.add_contact_not_available);
    }
}
