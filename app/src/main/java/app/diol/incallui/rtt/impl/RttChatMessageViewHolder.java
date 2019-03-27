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

package app.diol.incallui.rtt.impl;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import app.diol.R;
import app.diol.incallui.rtt.protocol.RttChatMessage;

/**
 * ViewHolder class for RTT chat message bubble.
 */
public class RttChatMessageViewHolder extends ViewHolder {

    private final TextView messageTextView;
    private final Resources resources;
    private final ImageView avatarImageView;
    private final View container;

    RttChatMessageViewHolder(View view) {
        super(view);
        container = view.findViewById(R.id.rtt_chat_message_container);
        messageTextView = view.findViewById(R.id.rtt_chat_message);
        avatarImageView = view.findViewById(R.id.rtt_chat_avatar);
        resources = view.getResources();
    }

    void setMessage(RttChatMessage message, boolean isSameGroup, Drawable imageDrawable) {
        messageTextView.setText(message.getContent());
        LinearLayout.LayoutParams params = (LayoutParams) container.getLayoutParams();
        params.gravity = message.isRemote ? Gravity.START : Gravity.END;
        params.topMargin =
                isSameGroup
                        ? resources.getDimensionPixelSize(R.dimen.rtt_same_group_message_margin_top)
                        : resources.getDimensionPixelSize(R.dimen.rtt_message_margin_top);
        container.setLayoutParams(params);
        messageTextView.setEnabled(message.isRemote);
        if (message.isRemote) {
            if (isSameGroup) {
                avatarImageView.setVisibility(View.INVISIBLE);
            } else {
                avatarImageView.setVisibility(View.VISIBLE);
                avatarImageView.setImageDrawable(imageDrawable);
            }
        } else {
            avatarImageView.setVisibility(View.GONE);
        }
    }
}
